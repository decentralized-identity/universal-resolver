package uniresolver.examples;
import uniresolver.ddo.DDO;
import uniresolver.driver.did.bsk.DidBskDriver;

public class TestDriverDidBsk {

	public static void main(String[] args) throws Exception {

		DidBskDriver driver = new DidBskDriver();

		DDO ddo = driver.resolve("did:bsk:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0");
		System.out.println(ddo.serialize());
	}
}
