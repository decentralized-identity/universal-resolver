package uniresolver.examples;

import uniresolver.driver.did.sov.DidSovDriver;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.ResolveResult;

import java.util.HashMap;
import java.util.Map;

public class TestLocalUniResolver {

	public static void main(String[] args) throws Exception {

		LocalUniResolver uniResolver = new LocalUniResolver();
		uniResolver.getDrivers().add(new DidSovDriver());
		uniResolver.getDriver(DidSovDriver.class).setLibIndyPath("./sovrin/lib/libindy.so");
		uniResolver.getDriver(DidSovDriver.class).setPoolConfigs("_;./sovrin/mainnet.txn");
		uniResolver.getDriver(DidSovDriver.class).setPoolVersions("_;2");

		Map<String, Object> resolveOptions = new HashMap<>();
		resolveOptions.put("accept", "application/did");

		ResolveResult resolveResult;
		resolveResult = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw", resolveOptions);
		System.out.println(resolveResult.toJson());
		resolveResult = uniResolver.resolveRepresentation("did:sov:WRfXPg8dantKVubE3HX8pw", resolveOptions);
		System.out.println(resolveResult.toJson());
	}
}
