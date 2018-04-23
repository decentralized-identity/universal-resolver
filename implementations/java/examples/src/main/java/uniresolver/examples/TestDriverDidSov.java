package uniresolver.examples;
import uniresolver.driver.did.sov.DidSovDriver;
import uniresolver.result.ResolutionResult;

public class TestDriverDidSov {

	public static void main(String[] args) throws Exception {

		DidSovDriver driver = new DidSovDriver();
		driver.setLibIndyPath("./sovrin/lib/");
		driver.setPoolConfigName("live");
		driver.setPoolGenesisTxn("live.txn");

		ResolutionResult resolutionResult = driver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw");
		System.out.println(resolutionResult.toJson());
	}
}
