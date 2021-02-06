package uniresolver.examples;
import did.DIDDocument;
import uniresolver.client.ClientUniResolver;

public class TestClientUniResolver {

	public static void main(String[] args) throws Exception {

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolveUri("https://uniresolver.io/1.0/identifiers/");

		DIDDocument didDocument1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw").getDidDocument();
		System.out.println(didDocument1.toJson());

		DIDDocument didDocument2 = uniResolver.resolve("did:btcr:xz35-jzv2-qqs2-9wjt").getDidDocument();
		System.out.println(didDocument2.toJson());

		DIDDocument didDocument3 = uniResolver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0").getDidDocument();
		System.out.println(didDocument3.toJson());
	}
}
