package uniresolver.examples;
import uniresolver.ddo.DDO;
import uniresolver.driver.did.sov.DidSovDriver;

public class TestDriverDidSov {

	public static void main(String[] args) throws Exception {

		DidSovDriver driver = new DidSovDriver();
		driver.setLibIndyPath("./sovrin/lib/");
		driver.setPoolConfigName("sandbox");
		driver.setPoolGenesisTxn("sandbox.txn");

		DDO ddo = driver.resolve("did:sov:AdLi7vX2z1bLyVZaoy18K1");
		System.out.println(ddo.serialize());
	}
}
