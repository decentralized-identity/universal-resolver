package uniresolver.examples;
import uniresolver.ddo.DDO;
import uniresolver.driver.did.stack.DidStackDriver;

public class TestDriverDidStack {

	public static void main(String[] args) throws Exception {

		DidStackDriver driver = new DidStackDriver();

		DDO ddo = driver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0");
		System.out.println(ddo.serialize());
	}
}
