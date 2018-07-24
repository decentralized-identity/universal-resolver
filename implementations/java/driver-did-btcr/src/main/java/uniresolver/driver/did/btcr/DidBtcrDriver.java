package uniresolver.driver.did.btcr;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.weboftrust.txrefconversion.Bech32;
import info.weboftrust.txrefconversion.Chain;
import info.weboftrust.txrefconversion.ChainAndTxid;
import info.weboftrust.txrefconversion.TxrefConverter;
import uniresolver.ResolutionException;
import uniresolver.did.Authentication;
import uniresolver.did.DIDDocument;
import uniresolver.did.Encryption;
import uniresolver.did.PublicKey;
import uniresolver.did.Service;
import uniresolver.driver.Driver;
import uniresolver.driver.did.btcr.bitcoinconnection.BitcoinConnection;
import uniresolver.driver.did.btcr.bitcoinconnection.BitcoinConnection.BtcrData;
import uniresolver.driver.did.btcr.bitcoinconnection.BitcoindRPCBitcoinConnection;
import uniresolver.driver.did.btcr.bitcoinconnection.BitcoinjSPVBitcoinConnection;
import uniresolver.driver.did.btcr.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniresolver.result.ResolutionResult;

public class DidBtcrDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidBtcrDriver.class);

	public static final Pattern DID_BTCR_PATTERN = Pattern.compile("^did:btcr:(\\S*)$");

	public static final String[] DIDDOCUMENT_PUBLICKEY_TYPES = new String[] { "Secp256k1VerificationKey2018" };
	public static final String[] DIDDOCUMENT_AUTHENTICATION_TYPES = new String[] { "EdDsaSASignatureAuthentication2018" };

	private Map<String, Object> properties;

	private BitcoinConnection bitcoinConnection;

	private HttpClient httpClient = HttpClients.createDefault();

	public DidBtcrDriver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidBtcrDriver() {

		this.setProperties(getPropertiesFromEnvironment());
	}

	private static Map<String, Object> getPropertiesFromEnvironment() {

		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

		Map<String, Object> properties = new HashMap<String, Object> ();

		try {

			String env_bitcoinConnection = System.getenv("uniresolver_driver_did_btcr_bitcoinConnection");
			String env_rpcUrlMainnet = System.getenv("uniresolver_driver_did_btcr_rpcUrlMainnet");
			String env_rpcUrlTestnet = System.getenv("uniresolver_driver_did_btcr_rpcUrlTestnet");

			if (env_bitcoinConnection != null) properties.put("bitcoinConnection", env_bitcoinConnection);
			if (env_rpcUrlMainnet != null) properties.put("rpcUrlMainnet", env_rpcUrlMainnet);
			if (env_rpcUrlTestnet != null) properties.put("rpcUrlTestnet", env_rpcUrlTestnet);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
	}

	private void configureFromProperties() {

		if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

		try {

			String prop_bitcoinConnection = (String) this.getProperties().get("bitcoinConnection");

			if ("bitcoind".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BitcoindRPCBitcoinConnection());

				String prop_rpcUrlMainnet = (String) this.getProperties().get("rpcUrlMainnet");
				String prop_rpcUrlTestnet = (String) this.getProperties().get("rpcUrlTestnet");

				if (prop_rpcUrlMainnet != null) ((BitcoindRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlMainnet(prop_rpcUrlMainnet);
				if (prop_rpcUrlTestnet != null) ((BitcoindRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlTestnet(prop_rpcUrlTestnet);
			} else if ("bitcoinj".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BitcoinjSPVBitcoinConnection());
			} else  if ("blockcypherapi".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BlockcypherAPIBitcoinConnection());
			}
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public ResolutionResult resolve(String identifier) throws ResolutionException {

		// parse identifier

		Matcher matcher = DID_BTCR_PATTERN.matcher(identifier);
		if (! matcher.matches()) return null;

		String targetDid = matcher.group(1);

		// determine txref

		String txref = null;
		if (targetDid.charAt(0) == TxrefConverter.MAGIC_BTC_MAINNET_BECH32_CHAR) txref = TxrefConverter.TXREF_BECH32_HRP_MAINNET + Bech32.SEPARATOR + "-" + targetDid;
		if (targetDid.charAt(0) == TxrefConverter.MAGIC_BTC_TESTNET_BECH32_CHAR) txref = TxrefConverter.TXREF_BECH32_HRP_TESTNET + Bech32.SEPARATOR + "-" + targetDid;
		if (txref == null) throw new ResolutionException("Invalid magic byte in " + targetDid);

		// retrieve btcr data

		Chain chain;
		String txid;
		BtcrData btcrData;

		try {

			TxrefConverter txrefConverter = new TxrefConverter(this.getBitcoinConnection());

			ChainAndTxid chainAndTxid = txrefConverter.txrefToTxid(txref);
			chain = chainAndTxid.getChain();
			txid = chainAndTxid.getTxid();

			btcrData = this.getBitcoinConnection().getBtcrData(chain, txid);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve BTCR data for " + txref + ": " + ex.getMessage());
		}

		if (log.isInfoEnabled()) log.info("Retrieved BTCR data for " + txref + " ("+ txid + " on chain " + chain + "): " + btcrData);

		// retrieve DID DOCUMENT FRAGEMENT

		DIDDocument didDocumentFragment = null;

		if (btcrData != null && btcrData.getFragmentUri() != null) {

			HttpGet httpGet = new HttpGet(btcrData.getFragmentUri());

			try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

				if (httpResponse.getStatusLine().getStatusCode() > 200) throw new ResolutionException("Cannot retrieve DID DOCUMENT FRAGMENT for " + txref + " from " + btcrData.getFragmentUri() + ": " + httpResponse.getStatusLine());

				HttpEntity httpEntity = httpResponse.getEntity();

				didDocumentFragment = DIDDocument.fromJson(EntityUtils.toString(httpEntity));
				EntityUtils.consume(httpEntity);
			} catch (IOException ex) {

				throw new ResolutionException("Cannot retrieve DID DOCUMENT FRAGMENT for " + txref + " from " + btcrData.getFragmentUri() + ": " + ex.getMessage(), ex);
			}

			if (log.isInfoEnabled()) log.info("Retrieved DID DOCUMENT FRAGMENT for " + txref + " (" + btcrData.getFragmentUri() + "): " + didDocumentFragment);
		}

		// DID DOCUMENT id

		String id = identifier;

		// DID DOCUMENT publicKeys

		int keyNum = 0;
		List<PublicKey> publicKeys;
		List<Authentication> authentications;
		List<Encryption> encryptions;

		if (btcrData != null) {

			String keyId = id + "#key-" + (++keyNum);

			PublicKey publicKey = PublicKey.build(keyId, DIDDOCUMENT_PUBLICKEY_TYPES, null, null, btcrData.getInputScriptPubKey(), null);
			publicKeys = Collections.singletonList(publicKey);

			Authentication authentication = Authentication.build(null, DIDDOCUMENT_AUTHENTICATION_TYPES, keyId);
			authentications = Collections.singletonList(authentication);

			encryptions = Collections.emptyList();
		} else {

			publicKeys = Collections.emptyList();
			authentications = Collections.emptyList();
			encryptions = Collections.emptyList();
		}

		// DID DOCUMENT services

		List<Service> services;

		if (didDocumentFragment != null) {

			services = didDocumentFragment.getServices();
		} else {

			services = Collections.emptyList();
		}

		// create DID DOCUMENT

		DIDDocument didDocument = DIDDocument.build(id, publicKeys, authentications, encryptions, services);

		// create METHOD METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		if (btcrData != null) methodMetadata.put("inputScriptPubKey", btcrData.getInputScriptPubKey());
		if (btcrData != null) methodMetadata.put("fragmentUri", btcrData.getFragmentUri());
		if (didDocumentFragment != null) methodMetadata.put("fragment", didDocumentFragment);
		if (chain != null) methodMetadata.put("chain", chain);
		if (txid != null) methodMetadata.put("txid", txid);

		// create RESOLUTION RESULT

		ResolutionResult resolutionResult = ResolutionResult.build(didDocument, null, methodMetadata);

		// done

		return resolutionResult;
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
	}

	/*
	 * Getters and setters
	 */

	public Map<String, Object> getProperties() {

		return this.properties;
	}

	public void setProperties(Map<String, Object> properties) {

		this.properties = properties;
		this.configureFromProperties();
	}

	public BitcoinConnection getBitcoinConnection() {

		return this.bitcoinConnection;
	}

	public void setBitcoinConnection(BitcoinConnection bitcoinConnection) {

		this.bitcoinConnection = bitcoinConnection;
	}

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}
}
