package uniresolver.examples;
import uniresolver.driver.did.stack.DidStackDriver;
import uniresolver.result.ResolutionResult;

public class TestDriverDidStack {

	public static void main(String[] args) throws Exception {

		DidStackDriver driver = new DidStackDriver();

		ResolutionResult resolutionResult = driver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0");
		System.out.println(resolutionResult.toJson());
	}
}
