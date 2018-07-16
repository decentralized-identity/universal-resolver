package uniresolver.driver.did.btcr.bitcoinconnection;

import java.io.IOException;
import java.net.URI;

import info.weboftrust.txrefconversion.Chain;

public interface BitcoinConnection extends info.weboftrust.txrefconversion.bitcoinconnection.BitcoinConnection {

	public BtcrData getBtcrData(Chain chain, String txid) throws IOException;

	public static class BtcrData {

		private String spentInTxid;
		private String inputScriptPubKey;
		private URI fragmentUri;

		public BtcrData(String spentInTxid, String inputScriptPubKey, URI fragmentUri) { this.spentInTxid = spentInTxid; this.inputScriptPubKey = inputScriptPubKey; this.fragmentUri = fragmentUri; }

		public String getSpentInTxid() { return this.spentInTxid; }
		public String getInputScriptPubKey() { return this.inputScriptPubKey; }
		public URI getFragmentUri() { return this.fragmentUri; }

		@Override
		public String toString() { return "BtcrData [spentInTxid=" + spentInTxid + ", inputScriptPubKey=" + inputScriptPubKey + ", fragmentUri=" + fragmentUri + "]"; }
	}
}
