package uniresolver.examples;
import uniresolver.client.ClientUniResolver;
import uniresolver.result.ResolutionResult;

public class TestClientUniResolver {

	public static void main(String[] args) throws Exception {

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolveUri("https://uniresolver.danubetech.com/1.0/identifiers/");

		ResolutionResult resolutionResult1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw");
		System.out.println(resolutionResult1.toJson());

		ResolutionResult resolutionResult2 = uniResolver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl");
		System.out.println(resolutionResult2.toJson());
	}
}
