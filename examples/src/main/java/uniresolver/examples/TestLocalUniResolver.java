package uniresolver.examples;
import uniresolver.driver.did.btcr.DidBtcrDriver;
import uniresolver.driver.did.btcr.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniresolver.driver.did.sov.DidSovDriver;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.ResolutionResult;

public class TestLocalUniResolver {

	public static void main(String[] args) throws Exception {

		LocalUniResolver uniResolver = LocalUniResolver.getDefault();
		uniResolver.getDriver(DidSovDriver.class).setLibIndyPath("./sovrin/lib/");
		uniResolver.getDriver(DidSovDriver.class).setPoolConfigName("live");
		uniResolver.getDriver(DidSovDriver.class).setPoolGenesisTxn("live.txn");
		uniResolver.getDriver(DidBtcrDriver.class).setBitcoinConnection(BlockcypherAPIBitcoinConnection.get());

		ResolutionResult resolutionResult1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw");
		System.out.println(resolutionResult1.toJson());

		ResolutionResult resolutionResult2 = uniResolver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl");
		System.out.println(resolutionResult2.toJson());

		ResolutionResult resolutionResult3 = uniResolver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0");
		System.out.println(resolutionResult3.toJson());
	}
}
