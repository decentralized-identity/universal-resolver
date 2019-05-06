package uniresolver.driver.did.btcr.bitcoinconnection;

import java.io.IOException;
import java.net.URI;

import info.weboftrust.txrefconversion.ChainAndTxid;

public interface BitcoinConnection extends info.weboftrust.txrefconversion.bitcoinconnection.BitcoinConnection {

	public BtcrData getBtcrData(ChainAndTxid chainAndTxid) throws IOException;

	public static class BtcrData {

		private ChainAndTxid spentInChainAndTxid;
		private String inputScriptPubKey;
		private URI continuationUri;

		public BtcrData(ChainAndTxid spentInChainAndTxid, String inputScriptPubKey, URI continuationUri) { this.spentInChainAndTxid = spentInChainAndTxid; this.inputScriptPubKey = inputScriptPubKey; this.continuationUri = continuationUri; }

		public ChainAndTxid getSpentInChainAndTxid() { return this.spentInChainAndTxid; }
		public String getInputScriptPubKey() { return this.inputScriptPubKey; }
		public URI getContinuationUri() { return this.continuationUri; }

		@Override
		public String toString() { return "BtcrData [spentInChainAndTxid=" + spentInChainAndTxid + ", inputScriptPubKey=" + inputScriptPubKey + ", continuationUri=" + continuationUri + "]"; }
	}
}
