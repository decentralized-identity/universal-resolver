package uniresolver.driver.did.btcr.bitcoinconnection;

import java.net.URI;
import java.net.URL;
import java.util.List;

import info.weboftrust.txrefconversion.TxrefConverter.Chain;
import info.weboftrust.txrefconversion.bitcoinconnection.BitcoindRPCBitcoinConnection;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.In;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.Out;

public class BitcoindRPCExtendedBitcoinConnection extends BitcoindRPCBitcoinConnection implements ExtendedBitcoinConnection {

	private static final BitcoindRPCExtendedBitcoinConnection instance = new BitcoindRPCExtendedBitcoinConnection();

	private final BitcoinJSONRPCClient bitcoindRpcClientMainnet;
	private final BitcoinJSONRPCClient bitcoindRpcClientTestnet;

	public BitcoindRPCExtendedBitcoinConnection(URL rpcUrlMainnet, URL rpcUrlTestnet) {

		this.bitcoindRpcClientMainnet = new BitcoinJSONRPCClient(rpcUrlMainnet);
		this.bitcoindRpcClientTestnet = new BitcoinJSONRPCClient(rpcUrlTestnet);
	}

	private BitcoindRPCExtendedBitcoinConnection() {

		this(BitcoinJSONRPCClient.DEFAULT_JSONRPC_URL, BitcoinJSONRPCClient.DEFAULT_JSONRPC_TESTNET_URL);
	}

	public static BitcoindRPCExtendedBitcoinConnection get() {

		return instance;
	}

	@Override
	public BtcrData getBtcrData(Chain chain, String txid) {

		// retrieve transaction data

		BitcoindRpcClient bitcoindRpcClient = chain == Chain.MAINNET ? bitcoindRpcClientMainnet : bitcoindRpcClientTestnet;

		RawTransaction rawTransaction = bitcoindRpcClient.getRawTransaction(txid);
		if (rawTransaction == null) return null;

		// find input script pub key

		String inputScriptPubKey = null;

		List<In> vIn = rawTransaction.vIn();
		if (vIn == null || vIn.size() < 1) return null;

		for (In in : vIn) {

			if (in.scriptPubKey() != null) {

				inputScriptPubKey = in.scriptPubKey();
				break;
			}
		}

		if (inputScriptPubKey == null) return null;

		// find more DDO URI

		URI moreDdoUri = null;

		List<Out> vOut = rawTransaction.vOut();
		if (vOut == null || vOut.size() < 1) return null;

		for (Out out : vOut) {

			if (out.scriptPubKey() != null) {

				moreDdoUri = URI.create("http://localhost/?" + out.value());
				break;
			}
		}

		if (moreDdoUri == null) return null;

		// done

		return new BtcrData(inputScriptPubKey, moreDdoUri);
	}
}
