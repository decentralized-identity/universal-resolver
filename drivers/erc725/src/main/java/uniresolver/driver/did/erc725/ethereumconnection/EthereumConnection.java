package uniresolver.driver.did.erc725.ethereumconnection;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import uniresolver.driver.did.erc725.ethereumconnection.result.ERC725Keys;

public interface EthereumConnection {

	public static final BigInteger KEY_TYPE_MANAGEMENT = BigInteger.valueOf(1);
	public static final BigInteger KEY_TYPE_ACTION = BigInteger.valueOf(2);
	public static final BigInteger KEY_TYPE_CLAIM = BigInteger.valueOf(3);
	public static final BigInteger KEY_TYPE_ENCRYPTION = BigInteger.valueOf(4);

	public ERC725Keys getKeys(String address) throws IOException;
	public Map<String, byte[]> getPublicKeysByAddresses(List<String> addresses) throws Exception;
}
