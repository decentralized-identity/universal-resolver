package uniresolver.driver.did.btcr;

import java.io.IOException;
import java.util.Collections;
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
import info.weboftrust.txrefconversion.TxrefConverter;
import info.weboftrust.txrefconversion.TxrefConverter.Chain;
import info.weboftrust.txrefconversion.TxrefConverter.ChainAndTxid;
import uniresolver.ResolutionException;
import uniresolver.ddo.DDO;
import uniresolver.ddo.DDO.Owner;
import uniresolver.driver.Driver;
import uniresolver.driver.did.btcr.bitcoinconnection.BlockcypherAPIExtendedBitcoinConnection;
import uniresolver.driver.did.btcr.bitcoinconnection.ExtendedBitcoinConnection;
import uniresolver.driver.did.btcr.bitcoinconnection.ExtendedBitcoinConnection.BtcrData;

public class DidBtcrDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidBtcrDriver.class);

	public static final Pattern DID_BTCR_PATTERN = Pattern.compile("^did:btcr:(\\S*)$");

	public static final String[] DDO_OWNER_TYPES = new String[] { "CryptographicKey", "EdDsaSAPublicKey" };
	public static final String DDO_CURVE = "secp256k1";

	public static final ExtendedBitcoinConnection DEFAULT_EXTENDED_BITCOIN_CONNECTION = BlockcypherAPIExtendedBitcoinConnection.get();
	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();

	private ExtendedBitcoinConnection extendedBitcoinConnection = DEFAULT_EXTENDED_BITCOIN_CONNECTION;
	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;

	public DidBtcrDriver() {

	}

	@Override
	public DDO resolve(String identifier) throws ResolutionException {

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

			TxrefConverter txrefConverter = new TxrefConverter(this.getExtendedBitcoinConnection());

			ChainAndTxid chainAndTxid = txrefConverter.txrefToTxid(txref);
			chain = chainAndTxid.getChain();
			txid = chainAndTxid.getTxid();

			btcrData = this.getExtendedBitcoinConnection().getBtcrData(chain, txid);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve BTCR data for " + txref + ": " + ex.getMessage());
		}

		if (log.isInfoEnabled()) log.info("Retrieved BTCR data for " + txref + " ("+ txid + " on chain " + chain + "): " + btcrData);

		// retrieve more DDO

		HttpGet httpGet = new HttpGet(btcrData.getMoreDdoUri());
		DDO moreDdo = null;

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			if (httpResponse.getStatusLine().getStatusCode() > 200) throw new ResolutionException("Cannot retrieve more DDO for " + txref + " from " + btcrData.getMoreDdoUri() + ": " + httpResponse.getStatusLine());

			HttpEntity httpEntity = httpResponse.getEntity();

			moreDdo = DDO.fromString(EntityUtils.toString(httpEntity));
			EntityUtils.consume(httpEntity);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve more DDO for " + txref + " from " + btcrData.getMoreDdoUri() + ": " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Retrieved more DDO for " + txref + " (" + btcrData.getMoreDdoUri() + "): " + moreDdo);

		// DDO id

		String id = identifier;

		// DDO owners

		Owner owner = Owner.build(identifier, DDO_OWNER_TYPES, DDO_CURVE, null, btcrData.getInputScriptPubKey());

		List<DDO.Owner> owners = Collections.singletonList(owner);

		// DDO controls

		List<DDO.Control> controls = Collections.emptyList();

		// DDO services

		Map<String, String> services = moreDdo.getServices();

		// create DDO

		DDO ddo = DDO.build(id, owners, controls, services);

		// done

		return ddo;
	}

	/*
	 * Getters and setters
	 */

	public ExtendedBitcoinConnection getExtendedBitcoinConnection() {

		return this.extendedBitcoinConnection;
	}

	public void setExtendedBitcoinConnection(ExtendedBitcoinConnection extendedBitcoinConnection) {

		this.extendedBitcoinConnection = extendedBitcoinConnection;
	}

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}
}
