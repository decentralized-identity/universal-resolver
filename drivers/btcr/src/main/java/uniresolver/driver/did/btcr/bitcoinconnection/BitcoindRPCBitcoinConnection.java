package uniresolver.driver.did.btcr.bitcoinconnection;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.weboftrust.txrefconversion.Chain;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.In;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.Out;

public class BitcoindRPCBitcoinConnection extends info.weboftrust.txrefconversion.bitcoinconnection.BitcoindRPCBitcoinConnection implements BitcoinConnection {

	private static Logger log = LoggerFactory.getLogger(BitcoindRPCBitcoinConnection.class);

	private static final BitcoindRPCBitcoinConnection instance = new BitcoindRPCBitcoinConnection();

	private static final Pattern patternAsmInputScriptPubKey = Pattern.compile("^[^\\s]+ ([0-9a-fA-F]+)$");
	private static final Pattern patternAsmContinuationUri = Pattern.compile("^OP_RETURN ([0-9a-fA-F]+)$");

	public BitcoindRPCBitcoinConnection(URL rpcUrlMainnet, URL rpcUrlTestnet) {

		super(rpcUrlMainnet, rpcUrlTestnet);
	}

	public BitcoindRPCBitcoinConnection() {

		super();
	}

	public static BitcoindRPCBitcoinConnection get() {

		return instance;
	}

	@Override
	public BtcrData getBtcrData(Chain chain, String txid) {

		// retrieve transaction data

		BitcoindRpcClient bitcoindRpcClient = chain == Chain.MAINNET ? this.bitcoindRpcClientMainnet : this.bitcoindRpcClientTestnet;

		RawTransaction rawTransaction = bitcoindRpcClient.getRawTransaction(txid);
		if (rawTransaction == null) return null;

		// find input script pub key

		String inputScriptPubKey = null;

		List<In> vIn = rawTransaction.vIn();
		if (vIn == null || vIn.size() < 1) return null;

		for (In in : vIn) {

			if (in.scriptSig() != null && in.scriptSig().get("asm") != null) {

				Matcher matcher = patternAsmInputScriptPubKey.matcher((String) in.scriptSig().get("asm"));

				if (log.isDebugEnabled()) log.debug("IN: " + in.scriptSig().get("asm") + " (MATCHES: " + matcher.matches() + ")");
				
				if (matcher.matches() && matcher.groupCount() == 1) {

					if (log.isDebugEnabled()) log.debug("inputScriptPubKey: " + matcher.group(1));

					inputScriptPubKey = matcher.group(1);
					break;
				}
			}
		}

		if (inputScriptPubKey == null) return null;
		if (inputScriptPubKey.length() > 66) inputScriptPubKey = inputScriptPubKey.substring(inputScriptPubKey.length() - 66);

		// find DID DOCUMENT CONTINUATION URI

		URI continuationUri = null;

		List<Out> vOut = rawTransaction.vOut();
		if (vOut == null || vOut.size() < 1) return null;

		for (Out out : vOut) {

			if (out.scriptPubKey() != null && out.scriptPubKey().asm() != null) {

				Matcher matcher = patternAsmContinuationUri.matcher(out.scriptPubKey().asm());

				if (log.isDebugEnabled()) log.debug("OUT: " + out.scriptPubKey().asm() + " (MATCHES: " + matcher.matches() + ")");

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
		}

		// done

		return new BtcrData(null, inputScriptPubKey, continuationUri);
	}
}
