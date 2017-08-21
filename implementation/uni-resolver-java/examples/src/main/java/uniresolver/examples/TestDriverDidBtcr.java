package uniresolver.examples;
import uniresolver.ddo.DDO;
import uniresolver.driver.did.btcr.DidBtcrDriver;
import uniresolver.driver.did.btcr.bitcoinconnection.BlockcypherAPIExtendedBitcoinConnection;

public class TestDriverDidBtcr {

	public static void main(String[] args) throws Exception {

		DidBtcrDriver driver = new DidBtcrDriver();
		driver.setExtendedBitcoinConnection(BlockcypherAPIExtendedBitcoinConnection.get());

		DDO ddo = driver.resolve("did:btcr:txtest1-xkrn-xzcr-qqlv-j6sl");
		System.out.println(ddo.serialize());
	}
}
