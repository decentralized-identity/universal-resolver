package uniresolver.examples;

import uniresolver.client.ClientUniResolver;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TestClientUniResolver {

	public static void main(String[] args) throws Exception {

		Map<String, Object> resolveOptions = new HashMap<>();
		resolveOptions.put("accept", "application/did");

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolveUri("http://localhost:8080/1.0/identifiers/");

		ResolveRepresentationResult resolveRepresentationResult = uniResolver.resolveRepresentation("did:sov:WRfXPg8dantKVubE3HX8pw", resolveOptions);
		System.out.println(resolveRepresentationResult.toJson());
		System.out.println(new String(resolveRepresentationResult.getDidDocumentStream(), StandardCharsets.UTF_8));
	}
}
