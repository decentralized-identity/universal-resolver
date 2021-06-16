package uniresolver.examples.w3ctestsuite;

import uniresolver.ResolutionException;
import uniresolver.client.ClientUniResolver;
import uniresolver.result.ResolveResult;

import java.util.HashMap;
import java.util.Map;

public class TestDIDResolutionMetadataInvalidDid {

	public static void main(String[] args) throws Exception {

		String didString = "did:sov:danube:$";

		Map<String, Object> resolutionOptions = new HashMap<>();
		resolutionOptions.put("accept", "application/did+ld+json");

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolveUri("http://localhost:8080/1.0/identifiers/");

		ResolveResult resolveResult;

		try {

			resolveResult = uniResolver.resolve(didString, resolutionOptions);
			System.out.println("Obtained resolve result.");
		} catch (ResolutionException ex) {

			ex.printStackTrace(System.err);
			resolveResult = ex.getResolveResult();
		}

		System.out.println(TestSuiteUtil.makeTestSuiteReport("invalidDidErrorOutcome", "resolve", didString, "did:sov", resolutionOptions, resolveResult));
	}
}
