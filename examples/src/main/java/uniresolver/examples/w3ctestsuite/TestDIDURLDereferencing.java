package uniresolver.examples.w3ctestsuite;

import uniresolver.DereferencingException;
import uniresolver.client.ClientUniResolver;
import uniresolver.local.LocalUniDereferencer;
import uniresolver.result.DereferenceResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDIDURLDereferencing {

	static final Map<String, List<Integer>> expectedOutcomes = Map.of(
			"defaultOutcome", List.of(0, 1),
			"invalidDidUrlErrorOutcome", List.of(2),
			"notFoundErrorOutcome", List.of(3, 4)
	);

	static final List<String> function = List.of(
			"dereference",
			"dereference",
			"dereference",
			"dereference",
			"dereference"
	);

	static final List<String> didUrlString = List.of(
			"did:sov:WRfXPg8dantKVubE3HX8pw",
			"did:sov:WRfXPg8dantKVubE3HX8pw#key-1",
			"did:sov:WRfXPg8dantKVubE3HX8pw#key-1#key-2",
			"did:sov:WRfXPg8dantKVubE3HX8pw#key-3",
			"did:sov:0000000000000000000000"
			);

	public static void main(String[] args) throws Exception {

		Map<String, Object> dereferenceOptions = new HashMap<>();
		dereferenceOptions.put("accept", "application/did");

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolveUri("http://localhost:8080/1.0/identifiers/");

		LocalUniDereferencer uniDereferencer = new LocalUniDereferencer();
		uniDereferencer.setUniResolver(uniResolver);

		List<DereferenceResult> dereferenceResults = new ArrayList<>();

		for (int i=0; i<function.size(); i++) {

			DereferenceResult dereferenceResult;

			try {

				dereferenceResult = uniDereferencer.dereference(didUrlString.get(i), dereferenceOptions);
			} catch (DereferencingException ex) {

				//ex.printStackTrace(System.err);
				dereferenceResult = ex.getDereferenceResult();
			}

			dereferenceResults.add(dereferenceResult);
		}

		System.out.println(TestSuiteUtil.makeDidUrlDereferencingTestSuiteReport(expectedOutcomes, function, didUrlString, "did:sov", dereferenceOptions, dereferenceResults));
	}
}
