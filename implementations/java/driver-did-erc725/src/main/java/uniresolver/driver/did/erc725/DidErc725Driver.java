package uniresolver.driver.did.erc725;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniresolver.ResolutionException;
import uniresolver.did.Authentication;
import uniresolver.did.DIDDocument;
import uniresolver.did.Encryption;
import uniresolver.did.PublicKey;
import uniresolver.did.Service;
import uniresolver.driver.Driver;
import uniresolver.driver.did.erc725.ethereumconnection.EthereumConnection;
import uniresolver.driver.did.erc725.ethereumconnection.EthereumConnection.ERC725Keys;
import uniresolver.driver.did.erc725.ethereumconnection.HybridEthereumConnection;
import uniresolver.driver.did.erc725.ethereumconnection.JsonRPCEthereumConnection;
import uniresolver.result.ResolutionResult;

public class DidErc725Driver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidErc725Driver.class);

	public static final Pattern DID_ERC725_PATTERN = Pattern.compile("^did:erc725:(\\S+)?:(\\S+)$");

	public static final String[] DIDDOCUMENT_PUBLICKEY_TYPES = new String[] { "Secp256k1VerificationKey2018" };
	public static final String[] DIDDOCUMENT_AUTHENTICATION_TYPES = new String[] { "Secp256k1SignatureAuthentication2018" };
	public static final String[] DIDDOCUMENT_ENCRYPTION_TYPES = new String[] { "Secp256k1Encryption2018" };

	private Map<String, Object> properties;

	private EthereumConnection ethereumConnection;

	public DidErc725Driver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidErc725Driver() {

		this.setProperties(getPropertiesFromEnvironment());
	}

	private static Map<String, Object> getPropertiesFromEnvironment() {

		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

		Map<String, Object> properties = new HashMap<String, Object> ();

		try {

			String env_ethereumConnection = System.getenv("uniresolver_driver_did_erc725_ethereumConnection");
			String env_rpcUrlMainnet = System.getenv("uniresolver_driver_did_erc725_rpcUrlMainnet");
			String env_rpcUrlRopsten = System.getenv("uniresolver_driver_did_erc725_rpcUrlRopsten");
			String env_rpcUrlRinkeby = System.getenv("uniresolver_driver_did_erc725_rpcUrlRinkeby");
			String env_rpcUrlKovan = System.getenv("uniresolver_driver_did_erc725_rpcUrlKovan");
			String env_etherscanApiMainnet = System.getenv("uniresolver_driver_did_erc725_etherscanApiMainnet");
			String env_etherscanApiRopsten = System.getenv("uniresolver_driver_did_erc725_etherscanApiRopsten");
			String env_etherscanApiRinkeby = System.getenv("uniresolver_driver_did_erc725_etherscanApiRinkeby");
			String env_etherscanApiKovan = System.getenv("uniresolver_driver_did_erc725_etherscanApiKovan");

			if (env_ethereumConnection != null) properties.put("ethereumConnection", env_ethereumConnection);
			if (env_rpcUrlMainnet != null) properties.put("rpcUrlMainnet", env_rpcUrlMainnet);
			if (env_rpcUrlRopsten != null) properties.put("rpcUrlRopsten", env_rpcUrlRopsten);
			if (env_rpcUrlRinkeby != null) properties.put("rpcUrlRinkeby", env_rpcUrlRinkeby);
			if (env_rpcUrlKovan != null) properties.put("rpcUrlKovan", env_rpcUrlKovan);
			if (env_etherscanApiMainnet != null) properties.put("etherscanApiMainnet", env_etherscanApiMainnet);
			if (env_etherscanApiRopsten != null) properties.put("etherscanApiRopsten", env_etherscanApiRopsten);
			if (env_etherscanApiRinkeby != null) properties.put("etherscanApiRinkeby", env_etherscanApiRinkeby);
			if (env_etherscanApiKovan != null) properties.put("etherscanApiKovan", env_etherscanApiKovan);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
	}

	private void configureFromProperties() {

		if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

		try {

			String prop_ethereumConnection = (String) this.getProperties().get("ethereumConnection");

			if ("jsonrpc".equals(prop_ethereumConnection)) {

				this.setEthereumConnection(new JsonRPCEthereumConnection());

				String prop_rpcUrlMainnet = (String) this.getProperties().get("rpcUrlMainnet");
				String prop_rpcUrlRopsten = (String) this.getProperties().get("rpcUrlRopsten");
				String prop_rpcUrlRinkeby = (String) this.getProperties().get("rpcUrlRinkeby");
				String prop_rpcUrlKovan = (String) this.getProperties().get("rpcUrlKovan");

				if (prop_rpcUrlMainnet != null) ((JsonRPCEthereumConnection) this.getEthereumConnection()).setRpcUrlMainnet(prop_rpcUrlMainnet);
				if (prop_rpcUrlRopsten != null) ((JsonRPCEthereumConnection) this.getEthereumConnection()).setRpcUrlRopsten(prop_rpcUrlRopsten);
				if (prop_rpcUrlRinkeby != null) ((JsonRPCEthereumConnection) this.getEthereumConnection()).setRpcUrlRinkeby(prop_rpcUrlRinkeby);
				if (prop_rpcUrlKovan != null) ((JsonRPCEthereumConnection) this.getEthereumConnection()).setRpcUrlKovan(prop_rpcUrlKovan);
			} else if ("hybrid".equals(prop_ethereumConnection)) {

				this.setEthereumConnection(new HybridEthereumConnection());

				String prop_rpcUrlMainnet = (String) this.getProperties().get("rpcUrlMainnet");
				String prop_rpcUrlRopsten = (String) this.getProperties().get("rpcUrlRopsten");
				String prop_rpcUrlRinkeby = (String) this.getProperties().get("rpcUrlRinkeby");
				String prop_rpcUrlKovan = (String) this.getProperties().get("rpcUrlKovan");
				String prop_etherscanApiMainnet = (String) this.getProperties().get("etherscanApiMainnet");
				String prop_etherscanApiRopsten = (String) this.getProperties().get("etherscanApiRopsten");
				String prop_etherscanApiRinkeby = (String) this.getProperties().get("etherscanApiRinkeby");
				String prop_etherscanApiKovan = (String) this.getProperties().get("etherscanApiKovan");

				if (prop_rpcUrlMainnet != null) ((HybridEthereumConnection) this.getEthereumConnection()).setRpcUrlMainnet(prop_rpcUrlMainnet);
				if (prop_rpcUrlRopsten != null) ((HybridEthereumConnection) this.getEthereumConnection()).setRpcUrlRopsten(prop_rpcUrlRopsten);
				if (prop_rpcUrlRinkeby != null) ((HybridEthereumConnection) this.getEthereumConnection()).setRpcUrlRinkeby(prop_rpcUrlRinkeby);
				if (prop_rpcUrlKovan != null) ((HybridEthereumConnection) this.getEthereumConnection()).setRpcUrlKovan(prop_rpcUrlKovan);
				if (prop_etherscanApiMainnet != null) ((HybridEthereumConnection) this.getEthereumConnection()).setEtherscanApiMainnet(prop_etherscanApiMainnet);
				if (prop_etherscanApiRopsten != null) ((HybridEthereumConnection) this.getEthereumConnection()).setEtherscanApiRopsten(prop_etherscanApiRopsten);
				if (prop_etherscanApiRinkeby != null) ((HybridEthereumConnection) this.getEthereumConnection()).setEtherscanApiRinkeby(prop_etherscanApiRinkeby);
				if (prop_etherscanApiKovan != null) ((HybridEthereumConnection) this.getEthereumConnection()).setEtherscanApiKovan(prop_etherscanApiKovan);
			}
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public ResolutionResult resolve(String identifier) throws ResolutionException {

		// parse identifier

		Matcher matcher = DID_ERC725_PATTERN.matcher(identifier);
		if (! matcher.matches()) return null;

		String network = matcher.groupCount() == 2 ? matcher.group(1) : null;
		String address = matcher.groupCount() == 2 ? matcher.group(2) : matcher.group(1);

		// retrieve keys

		ERC725Keys keys;

		try {

			keys = this.getEthereumConnection().getKeys(network, address);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve keys for address " + address + " on network " + network + ": "+ ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved keys for address " + address + " on network " + network + ": " + keys);

		// DID DOCUMENT id

		String id = identifier;

		// DID DOCUMENT publicKeys

		int keyNum = 0;
		List<PublicKey> publicKeys = new ArrayList<PublicKey> ();
		List<Authentication> authentications = new ArrayList<Authentication> ();
		List<Encryption> encryptions = new ArrayList<Encryption> ();

		if (keys != null) {

			for (byte[] managementKey : keys.getManagementKeys()) {

				String keyId = id + "#key-" + (++keyNum);

				PublicKey publicKey = PublicKey.build(keyId, DIDDOCUMENT_PUBLICKEY_TYPES, null, null, Hex.encodeHexString(managementKey), null);
				publicKeys.add(publicKey);
			}

			for (byte[] actionKey : keys.getActionKeys()) {

				String keyId = id + "#key-" + (++keyNum);

				PublicKey publicKey = PublicKey.build(keyId, DIDDOCUMENT_PUBLICKEY_TYPES, null, null, Hex.encodeHexString(actionKey), null);
				publicKeys.add(publicKey);

				Authentication authentication = Authentication.build(null, DIDDOCUMENT_AUTHENTICATION_TYPES, keyId);
				authentications.add(authentication);
			}

			for (byte[] claimKey : keys.getClaimKeys()) {

				String keyId = id + "#key-" + (++keyNum);

				PublicKey publicKey = PublicKey.build(keyId, DIDDOCUMENT_PUBLICKEY_TYPES, null, null, Hex.encodeHexString(claimKey), null);
				publicKeys.add(publicKey);

				Authentication authentication = Authentication.build(null, DIDDOCUMENT_AUTHENTICATION_TYPES, keyId);
				authentications.add(authentication);
			}

			for (byte[] actionKey : keys.getActionKeys()) {

				String keyId = id + "#key-" + (++keyNum);

				PublicKey publicKey = PublicKey.build(keyId, DIDDOCUMENT_PUBLICKEY_TYPES, null, null, Hex.encodeHexString(actionKey), null);
				publicKeys.add(publicKey);

				Encryption encryption = Encryption.build(null, DIDDOCUMENT_ENCRYPTION_TYPES, keyId);
				encryptions.add(encryption);
			}
		} else {

			publicKeys = Collections.emptyList();
			authentications = Collections.emptyList();
			encryptions = Collections.emptyList();
		}

		// DID DOCUMENT services

		List<Service> services = Collections.emptyList();

		// create DID DOCUMENT

		DIDDocument didDocument = DIDDocument.build(id, publicKeys, authentications, encryptions, services);

		// create METHOD METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		if (keys != null) methodMetadata.put("keys", keys.toJsonMap());

		// create RESOLUTION RESULT

		ResolutionResult resolutionResult = ResolutionResult.build(didDocument, null, methodMetadata);

		// done

		return resolutionResult;
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
	}

	/*
	 * Getters and setters
	 */

	public Map<String, Object> getProperties() {

		return this.properties;
	}

	public void setProperties(Map<String, Object> properties) {

		this.properties = properties;
		this.configureFromProperties();
	}

	public EthereumConnection getEthereumConnection() {

		return this.ethereumConnection;
	}

	public void setEthereumConnection(EthereumConnection ethereumConnection) {

		this.ethereumConnection = ethereumConnection;
	}
}
