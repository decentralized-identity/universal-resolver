package uniresolver.examples;
import uniresolver.client.ClientUniResolver;
import uniresolver.did.DIDDocument;

public class TestClientUniResolver {

	public static void main(String[] args) throws Exception {

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolverUri("https://uniresolver.danubetech.com/1.0/identifiers/");

		DIDDocument ddo1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw");
		System.out.println(ddo1.serialize());

		DIDDocument ddo2 = uniResolver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl");
		System.out.println(ddo2.serialize());
	}
}
