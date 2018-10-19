package uniresolver.examples;
import uniresolver.driver.did.btcr.DidBtcrDriver;
import uniresolver.driver.did.btcr.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniresolver.result.ResolveResult;

public class TestDriverDidBtcr {

	public static void main(String[] args) throws Exception {

		DidBtcrDriver driver = new DidBtcrDriver();
		driver.setBitcoinConnection(BlockcypherAPIBitcoinConnection.get());

		ResolveResult ResolveResult = driver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl");
		System.out.println(ResolveResult.toJson());
	}
}
