package uniresolver.examples;
import uniresolver.driver.did.sov.DidSovDriver;
import uniresolver.result.ResolveResult;

public class TestDriverDidSov {

	public static void main(String[] args) throws Exception {

		DidSovDriver driver = new DidSovDriver();
		driver.setLibIndyPath("./sovrin/lib/");
		driver.setPoolConfigs(";./sovrin/live.txn");

		ResolveResult ResolveResult = driver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw");
		System.out.println(ResolveResult.toJson());
	}
}
