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
		uniResolver.getDriver(DidSovDriver.class).setPoolConfigName("live");
		uniResolver.getDriver(DidSovDriver.class).setPoolGenesisTxn("live.txn");
		uniResolver.getDriver(DidBtcrDriver.class).setExtendedBitcoinConnection(BlockcypherAPIExtendedBitcoinConnection.get());

		DDO ddo1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw");
		System.out.println(ddo1.serialize());

		DDO ddo2 = uniResolver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl");
		System.out.println(ddo2.serialize());

		DDO ddo3 = uniResolver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0");
		System.out.println(ddo3.serialize());
	}
}
