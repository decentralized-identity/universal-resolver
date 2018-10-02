package uniresolver.driver.did.btcr.bitcoinconnection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import info.weboftrust.txrefconversion.Chain;

public class BTCDRPCBitcoinConnection extends info.weboftrust.txrefconversion.bitcoinconnection.BTCDRPCBitcoinConnection implements BitcoinConnection {

	private static Logger log = LoggerFactory.getLogger(BTCDRPCBitcoinConnection.class);

	private static final BTCDRPCBitcoinConnection instance = new BTCDRPCBitcoinConnection();

	private static final Pattern patternAsmInputScriptPubKey = Pattern.compile("^[^\\s]+ ([0-9a-fA-F]+)$");
	private static final Pattern patternAsmFragmentUri = Pattern.compile("^OP_RETURN ([0-9a-fA-F]+)$");

	public BTCDRPCBitcoinConnection(URL rpcUrlMainnet, URL rpcUrlTestnet) {

		super(rpcUrlMainnet, rpcUrlTestnet);
	}

	public BTCDRPCBitcoinConnection() {

		super();
	}

	public static BTCDRPCBitcoinConnection get() {

		return instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public BtcrData getBtcrData(Chain chain, String txid) throws IOException {

		JsonRpcHttpClient btcdRpcClient = chain == Chain.MAINNET ? this.btcdRpcClientMainnet : this.btcdRpcClientTestnet;

		// retrieve transaction data

		LinkedHashMap<String, Object> getrawtransaction_result;

		try {

			getrawtransaction_result = btcdRpcClient.invoke("getrawtransaction", new Object[] { txid, 1 }, LinkedHashMap.class);
		} catch (IOException ex) {

			throw ex;
		} catch (Throwable ex) {

			throw new IOException("getrawtransaction() exception: " + ex.getMessage(), ex);
		}

		// find input script pub key

		String inputScriptPubKey = null;

		ArrayList<Object> vIn = (ArrayList<Object>) getrawtransaction_result.get("vin");
		if (vIn == null || vIn.size() < 1) return null;

		for (int i=0; i<vIn.size(); i++) {

			LinkedHashMap<String, Object> in = (LinkedHashMap<String, Object>) vIn.get(i);

			LinkedHashMap<String, Object> scriptSig = (LinkedHashMap<String, Object>) in.get("scriptSig");
			if (scriptSig == null) continue;

			String asm = (String) scriptSig.get("asm");
			if (asm == null) continue;

			Matcher matcher = patternAsmInputScriptPubKey.matcher(asm);

			if (log.isDebugEnabled()) log.debug("IN: " + asm + " (MATCHES: " + matcher.matches() + ")");

			if (matcher.matches() && matcher.groupCount() == 1) {

				if (log.isDebugEnabled()) log.debug("inputScriptPubKey: " + matcher.group(1));

				inputScriptPubKey = matcher.group(1);
				break;
			}
		}

		if (inputScriptPubKey == null) return null;
		if (inputScriptPubKey.length() > 66) inputScriptPubKey = inputScriptPubKey.substring(inputScriptPubKey.length() - 66);

		// find DID DOCUMENT FRAGMENT URI

		URI fragmentUri = null;

		ArrayList<Object> vOut = (ArrayList<Object>) getrawtransaction_result.get("vout");
		if (vOut == null || vOut.size() < 1) return null;

		for (int i=0; i<vOut.size(); i++) {

			LinkedHashMap<String, Object> out = (LinkedHashMap<String, Object>) vOut.get(i);

			LinkedHashMap<String, Object> scriptPubKey = (LinkedHashMap<String, Object>) out.get("scriptPubKey");
			if (scriptPubKey == null) continue;

			String asm = (String) scriptPubKey.get("asm");
			if (asm == null) continue;

			Matcher matcher = patternAsmFragmentUri.matcher(asm);

			if (log.isDebugEnabled()) log.debug("OUT: " + asm + " (MATCHES: " + matcher.matches() + ")");

			if (matcher.matches() && matcher.groupCount() == 1) {

				if (log.isDebugEnabled()) log.debug("fragmentUri: " + matcher.group(1));

				try {

					fragmentUri = URI.create(new String(Hex.decodeHex(matcher.group(1).toCharArray()), "UTF-8"));
					break;
				} catch (UnsupportedEncodingException | DecoderException ex) {

					continue;
				}
			}
		}

		// find spent in tx

		String spentInTxid = null;

		spentInTxid: for (int i=0; i<vOut.size(); i++) {

			LinkedHashMap<String, Object> out = (LinkedHashMap<String, Object>) vOut.get(i);

			LinkedHashMap<String, Object> scriptPubKey = (LinkedHashMap<String, Object>) out.get("scriptPubKey");
			if (scriptPubKey == null) continue;

			ArrayList<String> addresses = (ArrayList<String>) scriptPubKey.get("addresses");
			if (addresses == null) continue;

			for (int ii=0; ii<addresses.size(); ii++) {

				String address = addresses.get(ii);
				if (log.isDebugEnabled()) log.debug("SEARCH OUT: address: " + address);

				// find transactions using this address

				ArrayList<Object> searchrawtransactions_result;

				try {

					searchrawtransactions_result = btcdRpcClient.invoke("searchrawtransactions", new Object[] { address, 1 }, ArrayList.class);
				} catch (IOException ex) {

					throw ex;
				} catch (Throwable ex) {

					throw new IOException("searchrawtransactions() exception: " + ex.getMessage(), ex);
				}

				// search transactions to see if they spent the address

				for (int iii=0; iii<searchrawtransactions_result.size(); iii++) {

					LinkedHashMap<String, Object> outTx = (LinkedHashMap<String, Object>) searchrawtransactions_result.get(iii);
					String outTxid = (String) outTx.get("txid");

					if (log.isDebugEnabled()) log.debug("SEARCH OUT: transaction: " + outTxid);

					String outInputScriptPubKey = null;
					String outInTxid = null;
					Integer outInVout = null;

					ArrayList<Object> outTxvIn = (ArrayList<Object>) outTx.get("vin");
					if (outTxvIn == null || outTxvIn.size() < 1) return null;

					for (int iiii=0; iiii<outTxvIn.size(); iiii++) {

						LinkedHashMap<String, Object> outTxIn = (LinkedHashMap<String, Object>) outTxvIn.get(iiii);

						outInTxid = (String) outTxIn.get("txid");
						if (log.isDebugEnabled()) log.debug("SEARCH OUT: outInTxid: " + outInTxid);
						if (outInTxid == null) continue;

						outInVout = (Integer) outTxIn.get("vout");
						if (log.isDebugEnabled()) log.debug("SEARCH OUT: outInVout: " + outInVout);
						if (outInVout == null) continue;

						LinkedHashMap<String, Object> scriptSig = (LinkedHashMap<String, Object>) outTxIn.get("scriptSig");
						if (scriptSig == null) continue;

						String asm = (String) scriptSig.get("asm");
						if (asm == null) continue;

						Matcher matcher = patternAsmInputScriptPubKey.matcher(asm);

						if (log.isDebugEnabled()) log.debug("SEARCH OUT: IN: " + asm + " (MATCHES: " + matcher.matches() + ")");

						if (matcher.matches() && matcher.groupCount() == 1) {

							if (log.isDebugEnabled()) log.debug("SEARCH OUT: outInputScriptPubKey: " + matcher.group(1));

							outInputScriptPubKey = matcher.group(1);
							break;
						}
					}

					if (outInputScriptPubKey == null) continue;
					if (outInputScriptPubKey.length() > 66) outInputScriptPubKey = outInputScriptPubKey.substring(outInputScriptPubKey.length() - 66);

					if (address.equals(pubKeyToAddress(chain, outInputScriptPubKey)) && txid.equals(outInTxid) && i == outInVout.intValue()) {

						spentInTxid = outTxid;
						break spentInTxid;
					}
				}
			}
		}

		// done

		return new BtcrData(spentInTxid, inputScriptPubKey, fragmentUri);
	}

	/*
	 * Helper methods
	 */

	private static String pubKeyToAddress(Chain chain, String pubKey) throws IOException {

		NetworkParameters params = null;

		if (Chain.MAINNET == chain) params = MainNetParams.get();
		if (Chain.TESTNET == chain) params = TestNet3Params.get();
		if (params == null) throw new IllegalArgumentException("Unknown chain " + chain + " for public key " + pubKey);

		ECKey eckey;

		try {

			eckey = ECKey.fromPublicOnly(Hex.decodeHex(pubKey.toCharArray()));
		} catch (DecoderException ex) {

			throw new IOException("Cannot decode public key " + pubKey + ": " + ex.getMessage(), ex);
		}

		return eckey.toAddress(params).toBase58();
	}
}
