package uniresolver.driver.did.btcr;

import java.io.IOException;
import java.util.ArrayList;
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

import com.github.jsonldjava.utils.JsonUtils;

import did.Authentication;
import did.DIDDocument;
import did.PublicKey;
import did.Service;
import info.weboftrust.btctxlookup.ChainAndLocationData;
import info.weboftrust.btctxlookup.ChainAndTxid;
import info.weboftrust.btctxlookup.DidBtcrData;
import info.weboftrust.btctxlookup.bitcoinconnection.BTCDRPCBitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoindRPCBitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BitcoinjSPVBitcoinConnection;
import info.weboftrust.btctxlookup.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

public class DidBtcrDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidBtcrDriver.class);

	public static final Pattern DID_BTCR_PATTERN = Pattern.compile("^did:btcr:(\\S*)$");

	public static final String[] DIDDOCUMENT_PUBLICKEY_TYPES = new String[] { "EcdsaSecp256k1VerificationKey2019" };
	public static final String[] DIDDOCUMENT_AUTHENTICATION_TYPES = new String[] { "EcdsaSecp256k1SignatureAuthentication2019" };

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
			} else if ("btcd".equals(prop_bitcoinConnection)) {

				this.setBitcoinConnection(new BTCDRPCBitcoinConnection());

				String prop_rpcUrlMainnet = (String) this.getProperties().get("rpcUrlMainnet");
				String prop_rpcUrlTestnet = (String) this.getProperties().get("rpcUrlTestnet");

				if (prop_rpcUrlMainnet != null) ((BTCDRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlMainnet(prop_rpcUrlMainnet);
				if (prop_rpcUrlTestnet != null) ((BTCDRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlTestnet(prop_rpcUrlTestnet);
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
	public ResolveResult resolve(String identifier) throws ResolutionException {

		// parse identifier

		Matcher matcher = DID_BTCR_PATTERN.matcher(identifier);
		if (! matcher.matches()) return null;

		String targetDid = matcher.group(1);

		// determine txref

		String txref = targetDid;

		// retrieve btcr data

		ChainAndLocationData initialChainAndLocationData;
		ChainAndLocationData chainAndLocationData;
		ChainAndTxid initialChainAndTxid;
		ChainAndTxid chainAndTxid;
		DidBtcrData btcrData = null;
		List<ChainAndTxid> spentInChainAndTxids = new ArrayList<ChainAndTxid> ();

		try {

			chainAndLocationData = ChainAndLocationData.txrefDecode(txref);

			if (chainAndLocationData.getLocationData().getTxoIndex() == 0 && chainAndLocationData.isExtended()) {

				String correctTxref = ChainAndLocationData.txrefEncode(chainAndLocationData);
				throw new ResolutionException("Extended txref form not allowed if txoIndex == 0. You probably want to use " + correctTxref + " instead.");
			}

			chainAndTxid = this.getBitcoinConnection().lookupChainAndTxid(chainAndLocationData);

			initialChainAndTxid = chainAndTxid;
			initialChainAndLocationData = chainAndLocationData;

			while (true) {

				btcrData = this.getBitcoinConnection().getDidBtcrData(chainAndTxid);
				if (btcrData == null) break;

				// check if we need to follow the tip

				if (btcrData.getSpentInChainAndTxid() == null) {

					break;
				} else {

					spentInChainAndTxids.add(btcrData.getSpentInChainAndTxid());
					chainAndTxid = btcrData.getSpentInChainAndTxid();
					chainAndLocationData = this.getBitcoinConnection().lookupChainAndLocationData(chainAndTxid);
				}
			}
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve BTCR data for " + txref + ": " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Retrieved BTCR data for " + txref + " ("+ chainAndTxid + " on chain " + chainAndLocationData.getChain() + "): " + btcrData);

		// retrieve DID DOCUMENT CONTINUATION

		DIDDocument didDocumentContinuation = null;

		if (btcrData != null && btcrData.getContinuationUri() != null) {

			HttpGet httpGet = new HttpGet(btcrData.getContinuationUri());

			try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

				if (httpResponse.getStatusLine().getStatusCode() > 200) throw new ResolutionException("Cannot retrieve DID DOCUMENT CONTINUATION for " + txref + " from " + btcrData.getContinuationUri() + ": " + httpResponse.getStatusLine());

				HttpEntity httpEntity = httpResponse.getEntity();

				Map<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(EntityUtils.toString(httpEntity));
				if (jsonLdObject.containsKey("didDocument")) jsonLdObject = (LinkedHashMap<String, Object>) jsonLdObject.get("didDocument");

				didDocumentContinuation = DIDDocument.fromJson(jsonLdObject);
				EntityUtils.consume(httpEntity);
			} catch (IOException ex) {

				throw new ResolutionException("Cannot retrieve DID DOCUMENT CONTINUATION for " + txref + " from " + btcrData.getContinuationUri() + ": " + ex.getMessage(), ex);
			}

			if (log.isInfoEnabled()) log.info("Retrieved DID DOCUMENT CONTINUATION for " + txref + " (" + btcrData.getContinuationUri() + "): " + didDocumentContinuation);
		}

		// DID DOCUMENT id

		String id = identifier;

		// DID DOCUMENT publicKeys

		int keyNum = 0;
		List<PublicKey> publicKeys = new ArrayList<PublicKey> ();
		List<Authentication> authentications = new ArrayList<Authentication> ();

		if (btcrData != null) {

			String keyId = id + "#key-" + (++keyNum);

			PublicKey publicKey = PublicKey.build(keyId, DIDDOCUMENT_PUBLICKEY_TYPES, null, null, btcrData.getInputScriptPubKey(), null);
			publicKeys.add(publicKey);

			Authentication authentication = Authentication.build(null, DIDDOCUMENT_AUTHENTICATION_TYPES, keyId);
			authentications.add(authentication);
		}

		if (didDocumentContinuation != null) {

			if (didDocumentContinuation.getPublicKeys() != null) publicKeys.addAll(didDocumentContinuation.getPublicKeys());
			if (didDocumentContinuation.getAuthentications() != null) authentications.addAll(didDocumentContinuation.getAuthentications());
		}

		// DID DOCUMENT services

		List<Service> services;

		if (didDocumentContinuation != null) {

			services = didDocumentContinuation.getServices();
		} else {

			services = Collections.emptyList();
		}

		// create DID DOCUMENT

		DIDDocument didDocument = DIDDocument.build(id, publicKeys, authentications, services);

		// revoked?

		if ((! spentInChainAndTxids.isEmpty()) && didDocumentContinuation == null) {

			didDocument = null;
		}

		// create METHOD METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		if (btcrData != null) methodMetadata.put("inputScriptPubKey", btcrData.getInputScriptPubKey());
		if (btcrData != null) methodMetadata.put("continuationUri", btcrData.getContinuationUri());
		if (didDocumentContinuation != null) methodMetadata.put("continuation", didDocumentContinuation);
		if (chainAndLocationData != null) methodMetadata.put("chain", chainAndLocationData.getChain());
		if (initialChainAndLocationData != null) methodMetadata.put("initialBlockHeight", initialChainAndLocationData.getLocationData().getBlockHeight());
		if (initialChainAndLocationData != null) methodMetadata.put("initialTransactionPosition", initialChainAndLocationData.getLocationData().getTransactionPosition());
		if (initialChainAndLocationData != null) methodMetadata.put("initialTxoIndex", initialChainAndLocationData.getLocationData().getTxoIndex());
		if (initialChainAndTxid != null) methodMetadata.put("initialTxid", initialChainAndTxid);
		if (chainAndLocationData != null) methodMetadata.put("blockHeight", chainAndLocationData.getLocationData().getBlockHeight());
		if (chainAndLocationData != null) methodMetadata.put("transactionPosition", chainAndLocationData.getLocationData().getTransactionPosition());
		if (chainAndLocationData != null) methodMetadata.put("txoIndex", chainAndLocationData.getLocationData().getTxoIndex());
		if (chainAndTxid != null) methodMetadata.put("txid", chainAndTxid);
		if (spentInChainAndTxids != null) methodMetadata.put("spentInChainAndTxids", spentInChainAndTxids);

		// create RESOLVE RESULT

		ResolveResult resolveResult = ResolveResult.build(didDocument, null, methodMetadata);

		// done

		return resolveResult;
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
