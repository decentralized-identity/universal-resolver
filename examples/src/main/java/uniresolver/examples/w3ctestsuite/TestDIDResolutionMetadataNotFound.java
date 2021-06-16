package uniresolver.examples.w3ctestsuite;

import uniresolver.ResolutionException;
import uniresolver.client.ClientUniResolver;
import uniresolver.result.ResolveResult;

import java.util.HashMap;
import java.util.Map;

public class TestDIDResolutionMetadataNotFound {

	public static void main(String[] args) throws Exception {

		String didString = "did:sov:0000000000000000000000";

		Map<String, Object> resolutionOptions = new HashMap<>();
		resolutionOptions.put("accept", "application/did+ld+json");

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolveUri("http://localhost:8080/1.0/identifiers/");

		ResolveResult resolveResult;

		try {

			resolveResult = uniResolver.resolve(didString, resolutionOptions);
		} catch (ResolutionException ex) {

			ex.printStackTrace(System.err);
			resolveResult = ex.getResolveResult();
		}

		System.out.println(TestSuiteUtil.makeTestSuiteReport("notFoundErrorOutcome", "resolve", didString, "did:sov", resolutionOptions, resolveResult));
	}
}
