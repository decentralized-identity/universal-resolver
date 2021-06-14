package uniresolver.examples;
import foundation.identity.did.DID;
import info.weboftrust.btctxlookup.bitcoinconnection.BlockcypherAPIBitcoinConnection;
import uniresolver.driver.did.btcr.DidBtcrDriver;
import uniresolver.result.ResolveResult;

import java.util.HashMap;
import java.util.Map;

public class TestDriverDidBtcr {

	public static void main(String[] args) throws Exception {

		DidBtcrDriver driver = new DidBtcrDriver();
		driver.setBitcoinConnectionTestnet(BlockcypherAPIBitcoinConnection.get());

		Map<String, Object> resolveOptions = new HashMap<>();
		ResolveResult resolveResult = driver.resolve(DID.fromString("did:btcr:xz35-jznz-q6mr-7q6"), resolveOptions);
		System.out.println(resolveResult.toJson());
	}
}
