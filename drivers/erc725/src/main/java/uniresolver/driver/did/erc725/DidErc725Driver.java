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

import did.Authentication;
import did.DIDDocument;
import did.PublicKey;
import did.Service;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.driver.did.erc725.ethereumconnection.EthereumConnection;
import uniresolver.driver.did.erc725.ethereumconnection.HybridEthereumConnection;
import uniresolver.driver.did.erc725.ethereumconnection.JsonRPCEthereumConnection;
import uniresolver.driver.did.erc725.ethereumconnection.result.ERC725Keys;
import uniresolver.result.ResolveResult;

public class DidErc725Driver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidErc725Driver.class);

	public static final Pattern DID_ERC725_PATTERN = Pattern.compile("^did:erc725:(\\S+)?:(\\S+)$");

	public static final String[] DIDDOCUMENT_PUBLICKEY_TYPES = new String[] { "EcdsaSecp256k1VerificationKey2019" };
	public static final String[] DIDDOCUMENT_AUTHENTICATION_TYPES = new String[] { "EcdsaSecp256k1SignatureAuthentication2019" };

	private Map<String, Object> properties;

	private Map<String, EthereumConnection> ethereumConnections;

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

			String env_ethereumConnections = System.getenv("uniresolver_versiontracker_did_jolo_ethereumConnections");
			String env_rpcUrls = System.getenv("uniresolver_versiontracker_did_jolo_rpcUrls");
			String env_etherscanApis = System.getenv("uniresolver_versiontracker_did_jolo_etherscanApis");

			if (env_ethereumConnections != null) properties.put("ethereumConnections", env_ethereumConnections);
			if (env_rpcUrls != null) properties.put("rpcUrls", env_rpcUrls);
			if (env_etherscanApis != null) properties.put("etherscanApis", env_etherscanApis);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
	}

	private void configureFromProperties() {

		if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

		try {

			// parse ethereumConnections

			String prop_ethereumConnections = (String) this.getProperties().get("ethereumConnections");

			String[] ethereumConnectionStrings = prop_ethereumConnections.split(";");
			Map<String, String> ethereumConnectionStringMap = new HashMap<String, String> ();
			for (int i=0; i<ethereumConnectionStrings.length; i+=2) ethereumConnectionStringMap.put(ethereumConnectionStrings[i], ethereumConnectionStrings[i+1]);

			if (log.isInfoEnabled()) log.info("ethereumConnections: " + ethereumConnectionStringMap);

			// parse rpcUrls

			String prop_rpcUrls = (String) this.getProperties().get("rpcUrls");

			String[] rpcUrlStrings = prop_rpcUrls.split(";");
			Map<String, String> rpcUrlStringMap = new HashMap<String, String> ();
			for (int i=0; i<rpcUrlStrings.length; i+=2) rpcUrlStringMap.put(rpcUrlStrings[i], rpcUrlStrings[i+1]);

			if (log.isInfoEnabled()) log.info("rpcUrls: " + rpcUrlStringMap);

			// parse etherscanApis

			String prop_etherscanApis = (String) this.getProperties().get("etherscanApis");

			String[] etherscanApiStrings = prop_etherscanApis.split(";");
			Map<String, String> etherscanApiStringMap = new HashMap<String, String> ();
			for (int i=0; i<etherscanApiStrings.length; i+=2) etherscanApiStringMap.put(etherscanApiStrings[i], etherscanApiStrings[i+1]);

			if (log.isInfoEnabled()) log.info("etherscanApis: " + etherscanApiStringMap);

			// loop ethereumConnections

			Map<String, EthereumConnection> ethereumConnections = new HashMap<String, EthereumConnection> ();
			this.setEthereumConnections(ethereumConnections);

			for (Map.Entry<String, String> ethereumConnectionString : ethereumConnectionStringMap.entrySet()) {

				EthereumConnection ethereumConnection;

				if ("jsonrpc".equals(ethereumConnectionString.getValue())) {

					String rpcUrl = rpcUrlStringMap.get(ethereumConnectionString.getKey());

					if (rpcUrl == null && log.isWarnEnabled()) log.warn("No rpcUrl for " + ethereumConnectionString.getKey());

					ethereumConnection = new JsonRPCEthereumConnection();
					if (rpcUrl != null) ((JsonRPCEthereumConnection) ethereumConnection).setRpcUrl(rpcUrl);
				} else if ("hybrid".equals(ethereumConnectionString.getValue())) {

					String rpcUrl = rpcUrlStringMap.get(ethereumConnectionString.getKey());
					String etherscanApi = etherscanApiStringMap.get(ethereumConnectionString.getKey());

					if (rpcUrl == null && log.isWarnEnabled()) log.warn("No rpcUrl for " + ethereumConnectionString.getKey());
					if (etherscanApi == null && log.isWarnEnabled()) log.warn("No etherscanApi for " + ethereumConnectionString.getKey());

					ethereumConnection = new HybridEthereumConnection();
					if (rpcUrl != null) ((HybridEthereumConnection) ethereumConnection).setRpcUrl(rpcUrl);
					if (etherscanApi != null) ((HybridEthereumConnection) ethereumConnection).setEtherscanApi(etherscanApi);
				} else {

					if (log.isWarnEnabled()) log.warn("Invalid ethereumConnection: " + ethereumConnectionString.getKey() + ". Ignoring.");
					continue;
				}

				ethereumConnections.put(ethereumConnectionString.getKey(), ethereumConnection);
			}
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {

		// parse identifier

		Matcher matcher = DID_ERC725_PATTERN.matcher(identifier);
		if (! matcher.matches()) return null;

		String network = matcher.groupCount() == 2 ? matcher.group(1) : null;
		String address = matcher.groupCount() == 2 ? matcher.group(2) : matcher.group(1);

		// find ethereum connection

		final EthereumConnection ethereumConnection = this.getEthereumConnections().get(network);
		if (ethereumConnection == null) throw new ResolutionException("No ethereum connection for network: " + network);

		// retrieve keys

		ERC725Keys keys;

		try {

			keys = ethereumConnection.getKeys(address);
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
		} else {

			publicKeys = Collections.emptyList();
			authentications = Collections.emptyList();
		}

		// DID DOCUMENT services

		List<Service> services = Collections.emptyList();

		// create DID DOCUMENT

		DIDDocument didDocument = DIDDocument.build(id, publicKeys, authentications, services);

		// create METHOD METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		if (keys != null) methodMetadata.put("keys", keys.toJsonMap());

		// create RESOLVE RESULT

		ResolveResult resolveResult = ResolveResult.build(didDocument, null, methodMetadata);

		// done

		return resolveResult;
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

	public Map<String, EthereumConnection> getEthereumConnections() {

		return this.ethereumConnections;
	}

	public void setEthereumConnections(Map<String, EthereumConnection> ethereumConnections) {

		this.ethereumConnections = ethereumConnections;
	}
}
