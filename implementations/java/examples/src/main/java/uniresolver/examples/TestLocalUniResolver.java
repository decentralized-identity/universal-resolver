package uniresolver.examples;
import uniresolver.ddo.DDO;
import uniresolver.driver.did.btcr.DidBtcrDriver;
import uniresolver.driver.did.btcr.bitcoinconnection.BlockcypherAPIExtendedBitcoinConnection;
import uniresolver.driver.did.sov.DidSovDriver;
import uniresolver.local.LocalUniResolver;

public class TestLocalUniResolver {

	public static void main(String[] args) throws Exception {

		LocalUniResolver uniResolver = LocalUniResolver.getDefault();
		uniResolver.getDriver(DidSovDriver.class).setLibIndyPath("./sovrin/lib/");
		uniResolver.getDriver(DidSovDriver.class).setPoolConfigName("sandbox");
		uniResolver.getDriver(DidSovDriver.class).setPoolGenesisTxn("sandbox.txn");
		uniResolver.getDriver(DidBtcrDriver.class).setExtendedBitcoinConnection(BlockcypherAPIExtendedBitcoinConnection.get());

		DDO ddo1 = uniResolver.resolve("did:sov:AdLi7vX2z1bLyVZaoy18K1");
		System.out.println(ddo1.serialize());

		DDO ddo2 = uniResolver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl");
		System.out.println(ddo2.serialize());
	}
}
