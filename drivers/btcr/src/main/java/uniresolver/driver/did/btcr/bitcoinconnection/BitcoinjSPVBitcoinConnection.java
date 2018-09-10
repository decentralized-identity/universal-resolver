package uniresolver.driver.did.btcr.bitcoinconnection;

import java.io.IOException;

import org.bitcoinj.core.BlockChain;

import info.weboftrust.txrefconversion.Chain;

public class BitcoinjSPVBitcoinConnection extends info.weboftrust.txrefconversion.bitcoinconnection.BitcoinjSPVBitcoinConnection implements BitcoinConnection {

	private static final BitcoinjSPVBitcoinConnection instance = new BitcoinjSPVBitcoinConnection();

	public BitcoinjSPVBitcoinConnection(BlockChain blockChain) {

		super(blockChain);
	}

	public BitcoinjSPVBitcoinConnection() {

		super();
	}

	public static BitcoinjSPVBitcoinConnection get() {

		return instance;
	}

	@Override
	public BtcrData getBtcrData(Chain chain, String txid) throws IOException {

		throw new RuntimeException("Not implemented.");
	}
}
