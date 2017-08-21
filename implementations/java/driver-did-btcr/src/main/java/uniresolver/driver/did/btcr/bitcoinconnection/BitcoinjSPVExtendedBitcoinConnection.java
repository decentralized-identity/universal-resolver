package uniresolver.driver.did.btcr.bitcoinconnection;

import java.io.IOException;

import org.bitcoinj.core.BlockChain;

import info.weboftrust.txrefconversion.TxrefConverter.Chain;
import info.weboftrust.txrefconversion.bitcoinconnection.BitcoinjSPVBitcoinConnection;

public class BitcoinjSPVExtendedBitcoinConnection extends BitcoinjSPVBitcoinConnection implements ExtendedBitcoinConnection {

	private static final BitcoinjSPVExtendedBitcoinConnection instance = new BitcoinjSPVExtendedBitcoinConnection();

	public BitcoinjSPVExtendedBitcoinConnection(BlockChain blockChain) {

		super(blockChain);
	}

	private BitcoinjSPVExtendedBitcoinConnection() {

		super();
	}

	public static BitcoinjSPVExtendedBitcoinConnection get() {

		return instance;
	}

	@Override
	public BtcrData getBtcrData(Chain chain, String txid) throws IOException {

		throw new RuntimeException("Not implemented.");
	}
}
