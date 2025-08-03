package uniresolver.examples.w3ctestsuite;

import uniresolver.ResolutionException;
import uniresolver.client.ClientUniResolver;
import uniresolver.result.ResolveResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestDIDResolution {

	static final Map<String, List<Integer>> expectedOutcomes = Map.of(
			"defaultOutcome", List.of(0, 1),
			"notFoundErrorOutcome", List.of(2),
			"invalidDidErrorOutcome", List.of(3)
	);

	static final List<String> function = List.of(
			"resolve",
			"resolveRepresentation",
			"resolve",
			"resolve"
	);

	static final List<String> didString = List.of(
			"did:sov:WRfXPg8dantKVubE3HX8pw",
			"did:sov:WRfXPg8dantKVubE3HX8pw",
			"did:sov:0000000000000000000000",
			"did:sov:danube:_$::"
	);

	static final List<Map<String, Object>> resolutionOptions = List.of(
			Map.of(),
			Map.of(
					"accept", "application/did"
			),
			Map.of(),
			Map.of()
	);

	public static void main(String[] args) throws Exception {

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolveUri("http://localhost:8080/1.0/identifiers/");

		List<ResolveResult> resolveResults = new ArrayList<>();

		for (int i=0; i<function.size(); i++) {

			ResolveResult resolveResult;

			try {

				if (function.get(i).equals("resolve"))
					resolveResult = uniResolver.resolve(didString.get(i), resolutionOptions.get(i));
				else if (function.get(i).equals("resolveRepresentation"))
					resolveResult = uniResolver.resolveRepresentation(didString.get(i), resolutionOptions.get(i));
				else
					throw new IllegalArgumentException();
			} catch (ResolutionException ex) {

//				ex.printStackTrace(System.err);
				resolveResult = ex.getResolveRepresentationResult();
			}

			resolveResults.add(resolveResult);
		}

		System.out.println(TestSuiteUtil.makeDidResolutionTestSuiteReport(expectedOutcomes, function, didString, "did:sov", resolutionOptions, resolveResults));
	}
}
