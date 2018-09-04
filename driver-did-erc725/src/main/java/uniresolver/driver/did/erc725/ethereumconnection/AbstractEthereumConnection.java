package uniresolver.driver.did.erc725.ethereumconnection;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractEthereumConnection implements EthereumConnection {

	@Override
	public final ERC725Keys getKeys(String network, String address) throws IOException {

		Map<BigInteger, Map<String, byte[]>> keysByType = this.getKeysByType(network, address);

		Map<String, byte[]> managementPublicKeys = keysByType.get(KEY_TYPE_MANAGEMENT);
		Map<String, byte[]> actionPublicKeys = keysByType.get(KEY_TYPE_ACTION);
		Map<String, byte[]> claimPublicKeys = keysByType.get(KEY_TYPE_CLAIM);
		Map<String, byte[]> encryptionPublicKeys = keysByType.get(KEY_TYPE_ENCRYPTION);

		return new ERC725Keys(managementPublicKeys.keySet(), actionPublicKeys.keySet(), claimPublicKeys.keySet(), encryptionPublicKeys.keySet(), managementPublicKeys.values(), actionPublicKeys.values(), claimPublicKeys.values(), encryptionPublicKeys.values());
	}

	public abstract Map<BigInteger, Map<String, byte[]>> getKeysByType(String network, String address) throws IOException;

	@Override
	public final Map<String, byte[]> getPublicKeysByAddresses(String network, List<String> addresses) throws Exception {

		if (addresses == null || addresses.size() < 1) return Collections.emptyMap();

		Map<String, String> transactionHashesByAddresses = this.getTransactionHashesByAddresses(network, addresses);
		Map<String, byte[]> publicKeysByAddresses = this.getPublicKeysByAddresses(network, transactionHashesByAddresses);

		return publicKeysByAddresses;
	}

	public abstract Map<String, String> getTransactionHashesByAddresses(String network, List<String> addresses) throws Exception;
	public abstract Map<String, byte[]> getPublicKeysByAddresses(String network, Map<String, String> transactionHashesByAddress) throws Exception;
}
