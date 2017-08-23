package uniresolver.driver.did.btcr.bitcoinconnection;

import java.io.IOException;
import java.net.URI;

import info.weboftrust.txrefconversion.TxrefConverter.Chain;
import info.weboftrust.txrefconversion.bitcoinconnection.BitcoinConnection;

public interface ExtendedBitcoinConnection extends BitcoinConnection {

	public BtcrData getBtcrData(Chain chain, String txid) throws IOException;

	public static class BtcrData {
		
		private String inputScriptPubKey;
		private URI moreDdoUri;

		public BtcrData(String inputScriptPubKey, URI moreDdoUri) { this.inputScriptPubKey = inputScriptPubKey; this.moreDdoUri = moreDdoUri; }
		public String getInputScriptPubKey() { return this.inputScriptPubKey; }
		public URI getMoreDdoUri() { return this.moreDdoUri; }

		@Override
		public String toString() { return "BtcrData [inputScriptPubKey=" + inputScriptPubKey + ", moreDdoUri=" + moreDdoUri + "]"; }
	}
}
