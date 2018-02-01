package uniresolver.driver.did.btcr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
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
import info.weboftrust.txrefconversion.TxrefConverter;
import info.weboftrust.txrefconversion.TxrefConverter.Chain;
import info.weboftrust.txrefconversion.TxrefConverter.ChainAndTxid;
import uniresolver.ResolutionException;
import uniresolver.did.DIDDocument;
import uniresolver.did.PublicKey;
import uniresolver.did.Service;
import uniresolver.driver.Driver;
import uniresolver.driver.did.btcr.bitcoinconnection.BitcoinConnection;
import uniresolver.driver.did.btcr.bitcoinconnection.BitcoinConnection.BtcrData;
import uniresolver.driver.did.btcr.bitcoinconnection.BitcoindRPCBitcoinConnection;
import uniresolver.driver.did.btcr.bitcoinconnection.BitcoinjSPVBitcoinConnection;
import uniresolver.driver.did.btcr.bitcoinconnection.BlockcypherAPIBitcoinConnection;

public class DidBtcrDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidBtcrDriver.class);

	public static final Pattern DID_BTCR_PATTERN = Pattern.compile("^did:btcr:(\\S*)$");

	public static final String[] DIDDOCUMENT_PUBLICKEY_TYPES = new String[] { "EdDsaSAPublicKeySecp256k1" };

	private BitcoinConnection bitcoinConnection;

	private HttpClient httpClient = HttpClients.createDefault();

	public DidBtcrDriver() {

		try {

			this.configureFromEnvironment();
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private void configureFromEnvironment() throws MalformedURLException {

		if (log.isDebugEnabled()) log.debug("Configuring from environment: " + System.getenv());

		String env_bitcoinConnection = System.getenv("uniresolver_driver_did_btcr_bitcoinConnection");

		if ("bitcoind".equals(env_bitcoinConnection)) {

			this.setBitcoinConnection(new BitcoindRPCBitcoinConnection());

			String env_rpcUrlMainnet = System.getenv("uniresolver_driver_did_btcr_rpcUrlMainnet");
			String env_rpcUrlTestnet = System.getenv("uniresolver_driver_did_btcr_rpcUrlTestnet");

			if (env_rpcUrlMainnet != null) ((BitcoindRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlMainnet(env_rpcUrlMainnet);
			if (env_rpcUrlTestnet != null) ((BitcoindRPCBitcoinConnection) this.getBitcoinConnection()).setRpcUrlTestnet(env_rpcUrlTestnet);
		} else if ("bitcoinj".equals(env_bitcoinConnection)) {

			this.setBitcoinConnection(new BitcoinjSPVBitcoinConnection());
		} else  if ("blockcypherapi".equals(env_bitcoinConnection)) {

			this.setBitcoinConnection(new BlockcypherAPIBitcoinConnection());
		}
	}

	@Override
	public DIDDocument resolve(String identifier) throws ResolutionException {

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

		HttpGet httpGet = new HttpGet(btcrData.getMoreDdoUri());
		DIDDocument didDocumentFragment = null;

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			if (httpResponse.getStatusLine().getStatusCode() > 200) throw new ResolutionException("Cannot retrieve DID DOCUMENT FRAGMENT for " + txref + " from " + btcrData.getMoreDdoUri() + ": " + httpResponse.getStatusLine());

			HttpEntity httpEntity = httpResponse.getEntity();

			didDocumentFragment = DIDDocument.fromString(EntityUtils.toString(httpEntity));
			EntityUtils.consume(httpEntity);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve DID DOCUMENT FRAGMENT for " + txref + " from " + btcrData.getMoreDdoUri() + ": " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Retrieved more DID DOCUMENT FRAGMENT for " + txref + " (" + btcrData.getMoreDdoUri() + "): " + didDocumentFragment);

		// DID DOCUMENT id

		String id = identifier;

		// DID DOCUMENT publicKeys

		PublicKey owner = PublicKey.build(identifier, DIDDOCUMENT_PUBLICKEY_TYPES, null, btcrData.getInputScriptPubKey());

		List<PublicKey> publicKeys = Collections.singletonList(owner);

		// DID DOCUMENT services

		List<Service> services = didDocumentFragment.getServices();

		// create DID DOCUMENT

		DIDDocument didDocument = DIDDocument.build(id, publicKeys, services);

		// done

		return didDocument;
	}

	/*
	 * Getters and setters
	 */

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
