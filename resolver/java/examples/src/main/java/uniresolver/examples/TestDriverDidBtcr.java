package uniresolver.examples;
import info.weboftrust.btctxlookup.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniresolver.driver.did.btcr.DidBtcrDriver;
import uniresolver.result.ResolveResult;

public class TestDriverDidBtcr {

	public static void main(String[] args) throws Exception {

		DidBtcrDriver driver = new DidBtcrDriver();
		driver.setBitcoinConnection(BlockcypherAPIBitcoinConnection.get());

		ResolveResult ResolveResult = driver.resolve("did:btcr:xz35-jznz-q6mr-7q6");
		System.out.println(ResolveResult.toJson());
	}
}
