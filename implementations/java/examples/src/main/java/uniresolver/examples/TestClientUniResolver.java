package uniresolver.examples;
import uniresolver.client.ClientUniResolver;
import uniresolver.ddo.DDO;

public class TestClientUniResolver {

	public static void main(String[] args) throws Exception {

		ClientUniResolver uniResolver = new ClientUniResolver();
		uniResolver.setResolverUri("https://uniresolver.danubetech.com/1.0/identifiers/");

		DDO ddo1 = uniResolver.resolve("did:sov:AdLi7vX2z1bLyVZaoy18K1");
		System.out.println(ddo1.serialize());

		DDO ddo2 = uniResolver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl");
		System.out.println(ddo2.serialize());
	}
}
