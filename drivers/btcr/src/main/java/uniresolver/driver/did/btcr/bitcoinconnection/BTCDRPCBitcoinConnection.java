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
import info.weboftrust.txrefconversion.ChainAndTxid;

public class BTCDRPCBitcoinConnection extends info.weboftrust.txrefconversion.bitcoinconnection.BTCDRPCBitcoinConnection implements BitcoinConnection {

	private static Logger log = LoggerFactory.getLogger(BTCDRPCBitcoinConnection.class);

	private static final BTCDRPCBitcoinConnection instance = new BTCDRPCBitcoinConnection();

	private static final Pattern patternAsmInputScriptPubKey = Pattern.compile("^[^\\s]+ ([0-9a-fA-F]+)$");
	private static final Pattern patternAsmContinuationUri = Pattern.compile("^OP_RETURN ([0-9a-fA-F]+)$");

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
	public BtcrData getBtcrData(ChainAndTxid chainAndTxid) throws IOException {

		JsonRpcHttpClient btcdRpcClient = chainAndTxid.getChain() == Chain.MAINNET ? this.btcdRpcClientMainnet : this.btcdRpcClientTestnet;

		// retrieve transaction data

		LinkedHashMap<String, Object> getrawtransaction_result;

		try {

			getrawtransaction_result = btcdRpcClient.invoke("getrawtransaction", new Object[] { chainAndTxid.getTxid(), 1 }, LinkedHashMap.class);
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

		// find DID DOCUMENT CONTINUATION URI

		URI continuationUri = null;

		ArrayList<Object> vOut = (ArrayList<Object>) getrawtransaction_result.get("vout");
		if (vOut == null || vOut.size() < 1) return null;

		for (int i=0; i<vOut.size(); i++) {

			LinkedHashMap<String, Object> out = (LinkedHashMap<String, Object>) vOut.get(i);

			LinkedHashMap<String, Object> scriptPubKey = (LinkedHashMap<String, Object>) out.get("scriptPubKey");
			if (scriptPubKey == null) continue;

			String asm = (String) scriptPubKey.get("asm");
			if (asm == null) continue;

			Matcher matcher = patternAsmContinuationUri.matcher(asm);

			if (log.isDebugEnabled()) log.debug("OUT: " + asm + " (MATCHES: " + matcher.matches() + ")");

			if (matcher.matches() && matcher.groupCount() == 1) {

				if (log.isDebugEnabled()) log.debug("continuationUri: " + matcher.group(1));

				try {

					continuationUri = URI.create(new String(Hex.decodeHex(matcher.group(1).toCharArray()), "UTF-8"));
					break;
				} catch (UnsupportedEncodingException | DecoderException ex) {

					continue;
				}
			}
		}

		// find spent in tx

		ChainAndTxid spentInChainAndTxid = null;

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
					int outTxIndex = 0;
					String outInTxid = null;
					Integer outInVout = null;

					ArrayList<Object> outTxvIn = (ArrayList<Object>) outTx.get("vin");
					if (outTxvIn == null || outTxvIn.size() < 1) return null;

					for (; outTxIndex<outTxvIn.size(); outTxIndex++) {

						LinkedHashMap<String, Object> outTxIn = (LinkedHashMap<String, Object>) outTxvIn.get(outTxIndex);

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

					if (address.equals(pubKeyToAddress(chainAndTxid.getChain(), outInputScriptPubKey)) && chainAndTxid.getTxid().equals(outInTxid) && i == outInVout.intValue()) {

						spentInChainAndTxid = new ChainAndTxid(chainAndTxid.getChain(), outTxid, outTxIndex);
						break spentInTxid;
					}
				}
			}
		}

		// done

		return new BtcrData(spentInChainAndTxid, inputScriptPubKey, continuationUri);
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
