package uniresolver.examples;

import uniresolver.client.ClientUniResolver;
import uniresolver.result.ResolveResult;

import java.util.HashMap;
import java.util.Map;

public class TestClientUniResolver {

	public static void main(String[] args) throws Exception {

		Map<String, Object> resolveOptions = new HashMap<>();
		resolveOptions.put("accept", "application/did+ld+json");

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolveUri("http://localhost:8080/1.0/identifiers/");

		ResolveResult resolveResult = uniResolver.resolveRepresentation("did:sov:WRfXPg8dantKVubE3HX8pw", resolveOptions);
		System.out.println(resolveResult.toJson());
	}
}
