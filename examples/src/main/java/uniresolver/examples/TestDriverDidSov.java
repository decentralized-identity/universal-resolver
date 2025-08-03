package uniresolver.examples;

import foundation.identity.did.DID;
import uniresolver.driver.did.sov.DidSovDriver;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveResult;

import java.util.HashMap;
import java.util.Map;

public class TestDriverDidSov {

	public static void main(String[] args) throws Exception {

		DidSovDriver driver = new DidSovDriver();
		driver.setLibIndyPath("./sovrin/lib/libindy.so");
		driver.setPoolConfigs("_;./sovrin/mainnet.txn");
		driver.setPoolVersions("_;2");

		Map<String, Object> resolveOptions = new HashMap<>();
		resolveOptions.put("accept", "application/did");

		ResolveResult resolveResult;
		resolveResult = driver.resolve(DID.fromString("did:sov:WRfXPg8dantKVubE3HX8pw"), resolveOptions);
		System.out.println(resolveResult.toJson());
		resolveResult = driver.resolveRepresentation(DID.fromString("did:sov:WRfXPg8dantKVubE3HX8pw"), resolveOptions);
		System.out.println(resolveResult.toJson());
	}
}
