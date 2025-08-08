package uniresolver.examples;

import uniresolver.driver.did.sov.DidSovDriver;
import uniresolver.local.LocalUniDereferencer;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.DereferenceResult;

import java.util.HashMap;
import java.util.Map;

public class TestLocalUniDereferencer {

	public static void main(String[] args) throws Exception {

		LocalUniResolver uniResolver = new LocalUniResolver();
		uniResolver.getDrivers().add(new DidSovDriver());
		uniResolver.getDriver(DidSovDriver.class).setLibIndyPath("./sovrin/lib/libindy.so");
		uniResolver.getDriver(DidSovDriver.class).setPoolConfigs("_;./sovrin/mainnet.txn");
		uniResolver.getDriver(DidSovDriver.class).setPoolVersions("_;2");

		LocalUniDereferencer uniDereferencer = new LocalUniDereferencer();
		uniDereferencer.setUniResolver(uniResolver);

		Map<String, Object> dereferenceOptions = new HashMap<>();
		dereferenceOptions.put("accept", "application/did");
		DereferenceResult dereferenceResult = uniDereferencer.dereference("did:sov:WRfXPg8dantKVubE3HX8pw#key-1", dereferenceOptions);
		System.out.println(dereferenceResult.toJson());
	}
}
