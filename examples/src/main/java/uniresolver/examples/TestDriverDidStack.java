package uniresolver.examples;
import uniresolver.driver.did.stack.DidStackDriver;
import uniresolver.result.ResolveResult;

public class TestDriverDidStack {

	public static void main(String[] args) throws Exception {

		DidStackDriver driver = new DidStackDriver();

		ResolveResult ResolveResult = driver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0");
		System.out.println(ResolveResult.toJson());
        // Index 4 of the same owner address.
		ResolveResult = driver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-4");
		System.out.println(ResolveResult.toJson());
        // Example of a subdomain did
		ResolveResult = driver.resolve("did:stack:v0:SSXMcDiCZ7yFSQSUj7mWzmDcdwYhq97p2i-0");
		System.out.println(ResolveResult.toJson());
        try {
            // Example of an out-of-bounds index.
            ResolveResult = driver.resolve("did:stack:v0:SSXMcDiCZ7yFSQSUj7mWzmDcdwYhq97p2i-1");
        } catch (Exception ex) {
            System.out.println("Last example correctly fails.");
        }
	}
}
