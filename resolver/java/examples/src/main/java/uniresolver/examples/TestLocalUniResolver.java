package uniresolver.examples;
import info.weboftrust.btctxlookup.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniresolver.driver.did.btcr.DidBtcrDriver;
import uniresolver.driver.did.sov.DidSovDriver;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.ResolveResult;

public class TestLocalUniResolver {

	public static void main(String[] args) throws Exception {

		LocalUniResolver uniResolver = new LocalUniResolver();
		uniResolver.getDrivers().put("btcr", new DidBtcrDriver());
		uniResolver.getDrivers().put("sov", new DidSovDriver());
		uniResolver.getDriver(DidSovDriver.class).setLibIndyPath("./sovrin/lib/");
		uniResolver.getDriver(DidSovDriver.class).setPoolConfigs("_;./sovrin/mainnet.txn;staging;./sovrin/stagingnet.txn;builder;./sovrin/buildernet.txn;danube;./sovrin/danube.txn");
		uniResolver.getDriver(DidSovDriver.class).setPoolVersions("_;2;staging;2;builder;2;danube;2");
		uniResolver.getDriver(DidBtcrDriver.class).setBitcoinConnection(BlockcypherAPIBitcoinConnection.get());

		ResolveResult ResolveResult1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw");
		System.out.println(ResolveResult1.toJson());

		ResolveResult ResolveResult2 = uniResolver.resolve("did:btcr:xz35-jznz-q6mr-7q6");
		System.out.println(ResolveResult2.toJson());

		ResolveResult ResolveResult3 = uniResolver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0");
		System.out.println(ResolveResult3.toJson());
	}
}
