package uniresolver.examples;
import foundation.identity.did.DID;
import info.weboftrust.btctxlookup.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniresolver.driver.did.btc1.DidBtcrDriver;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveResult;

import java.util.HashMap;
import java.util.Map;

public class TestDriverDidBtcr {

	public static void main(String[] args) throws Exception {

		Map<String, Object> driverProperties = new HashMap<>();
		driverProperties.put("bitcoinConnection", "blockcypherapi");
		DidBtcrDriver driver = new DidBtcrDriver(driverProperties);

		Map<String, Object> resolveOptions = new HashMap<>();
		ResolveResult resolveResult = driver.resolve(DID.fromString("did:btcr:x705-jznz-q3nl-srs"), resolveOptions);
		System.out.println(resolveResult.toJson());
	}
}
