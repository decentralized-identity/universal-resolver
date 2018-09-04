package uniresolver.driver.did.erc725.ethereumconnection;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;

public interface EthereumConnection {

	public static final String NETWORK_MAINNET = "mainnet";
	public static final String NETWORK_ROPSTEN = "ropsten";
	public static final String NETWORK_RINKEBY = "rinkeby";
	public static final String NETWORK_KOVAN = "kovan";

	public static final BigInteger KEY_TYPE_MANAGEMENT = BigInteger.valueOf(1);
	public static final BigInteger KEY_TYPE_ACTION = BigInteger.valueOf(2);
	public static final BigInteger KEY_TYPE_CLAIM = BigInteger.valueOf(3);
	public static final BigInteger KEY_TYPE_ENCRYPTION = BigInteger.valueOf(4);

	public ERC725Keys getKeys(String network, String address) throws IOException;
	public Map<String, byte[]> getPublicKeysByAddresses(String network, List<String> addresses) throws Exception;

	public static class ERC725Keys {

		public ERC725Keys(Collection<String> managementAddresses, Collection<String> actionAddresses, Collection<String> claimAddresses, Collection<String> encryptionAddresses, Collection<byte[]> managementKeys, Collection<byte[]> actionKeys, Collection<byte[]> claimKeys, Collection<byte[]> encryptionKeys) {

			this.managementAddresses = managementAddresses;
			this.actionAddresses = actionAddresses;
			this.claimAddresses = claimAddresses;
			this.encryptionAddresses = encryptionAddresses;
			this.managementKeys = managementKeys;
			this.actionKeys = actionKeys;
			this.claimKeys = claimKeys;
			this.encryptionKeys = encryptionKeys;
		}

		private Collection<String> managementAddresses;
		private Collection<String> actionAddresses;
		private Collection<String> claimAddresses;
		private Collection<String> encryptionAddresses;
		private Collection<byte[]> managementKeys;
		private Collection<byte[]> actionKeys;
		private Collection<byte[]> claimKeys;
		private Collection<byte[]> encryptionKeys;

		public Collection<String> getManagementAddresses() {

			return this.managementAddresses;
		}

		public Collection<String> getActionAddresses() {

			return this.actionAddresses;
		}

		public Collection<String> getClaimAddresses() {

			return this.claimAddresses;
		}

		public Collection<String> getEncryptionAddresses() {

			return this.encryptionAddresses;
		}

		public Collection<byte[]> getManagementKeys() {

			return this.managementKeys;
		}

		public Collection<byte[]> getActionKeys() {

			return this.actionKeys;
		}

		public Collection<byte[]> getClaimKeys() {

			return this.claimKeys;
		}

		public Collection<byte[]> getEncryptionKeys() {

			return this.encryptionKeys;
		}

		public Map<String, Object> toJsonMap() {

			List<String> managementKeys = new ArrayList<String> ();
			List<String> actionKeys = new ArrayList<String> ();
			List<String> claimKeys = new ArrayList<String> ();
			List<String> encryptionKeys = new ArrayList<String> ();

			for (byte[] managementKey : this.managementKeys) managementKeys.add("0x" + Hex.encodeHexString(managementKey).toUpperCase());
			for (byte[] actionKey : this.actionKeys) actionKeys.add("0x" + Hex.encodeHexString(actionKey).toUpperCase());
			for (byte[] claimKey : this.claimKeys) claimKeys.add("0x" + Hex.encodeHexString(claimKey).toUpperCase());
			for (byte[] encryptionKey : this.encryptionKeys) encryptionKeys.add("0x" + Hex.encodeHexString(encryptionKey).toUpperCase());

			Map<String, Object> jsonMap = new HashMap<String, Object> ();
			jsonMap.put("managementKeys", managementKeys);
			jsonMap.put("actionKeys", actionKeys);
			jsonMap.put("claimKeys", claimKeys);
			jsonMap.put("encryptionKeys", encryptionKeys);

			jsonMap.put("managementAddresses", this.managementAddresses);
			jsonMap.put("actionAddresses", this.actionAddresses);
			jsonMap.put("claimAddresses", this.claimAddresses);
			jsonMap.put("encryptionAddresses", this.encryptionAddresses);

			return jsonMap;
		}
	}
}
