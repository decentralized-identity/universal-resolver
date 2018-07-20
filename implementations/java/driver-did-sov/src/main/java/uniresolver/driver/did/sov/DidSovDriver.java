package uniresolver.driver.did.sov;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.CreatePoolLedgerConfigJSONParameter;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.OpenPoolLedgerJSONParameter;
import org.hyperledger.indy.sdk.pool.PoolLedgerConfigExistsException;
import org.hyperledger.indy.sdk.signus.Signus;
import org.hyperledger.indy.sdk.signus.SignusJSONParameters.CreateAndStoreMyDidJSONParameter;
import org.hyperledger.indy.sdk.signus.SignusResults.CreateAndStoreMyDidResult;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.hyperledger.indy.sdk.wallet.WalletExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import uniresolver.ResolutionException;
import uniresolver.did.Authentication;
import uniresolver.did.DIDDocument;
import uniresolver.did.Encryption;
import uniresolver.did.PublicKey;
import uniresolver.did.Service;
import uniresolver.driver.Driver;
import uniresolver.result.ResolutionResult;

public class DidSovDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidSovDriver.class);

	public static final Pattern DID_SOV_PATTERN = Pattern.compile("^did:sov:(\\S*)$");

	public static final String[] DIDDOCUMENT_PUBLICKEY_TYPES = new String[] { "Ed25519VerificationKey2018" };
	public static final String[] DIDDOCUMENT_AUTHENTICATION_TYPES = new String[] { "Ed25519SignatureAuthentication2018" };

	private Map<String, Object> properties;

	private static final Gson gson = new Gson();

	private String libIndyPath;
	private String poolConfigName;
	private String poolGenesisTxn;
	private String walletName;

	private Pool pool = null;
	private Wallet wallet = null;
	private String submitterDid = null;

	public DidSovDriver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidSovDriver() {

		this.setProperties(getPropertiesFromEnvironment());
	}

	private static Map<String, Object> getPropertiesFromEnvironment() {

		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

		Map<String, Object> properties = new HashMap<String, Object> ();

		try {

			String env_libIndyPath = System.getenv("uniresolver_driver_did_sov_libIndyPath");
			String env_poolConfigName = System.getenv("uniresolver_driver_did_sov_poolConfigName");
			String env_poolGenesisTxn = System.getenv("uniresolver_driver_did_sov_poolGenesisTxn");
			String env_walletName = System.getenv("uniresolver_driver_did_sov_walletName");

			if (env_libIndyPath != null) properties.put("libIndyPath", env_libIndyPath);
			if (env_poolConfigName != null) properties.put("poolConfigName", env_poolConfigName);
			if (env_poolGenesisTxn != null) properties.put("poolGenesisTxn", env_poolGenesisTxn);
			if (env_walletName != null) properties.put("walletName", env_walletName);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
	}

	private void configureFromProperties() {

		if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

		try {

			String prop_libIndyPath = (String) this.getProperties().get("libIndyPath");
			String prop_poolConfigName = (String) this.getProperties().get("poolConfigName");
			String prop_poolGenesisTxn = (String) this.getProperties().get("poolGenesisTxn");
			String prop_walletName = (String) this.getProperties().get("walletName");

			if (prop_libIndyPath != null) this.setLibIndyPath(prop_libIndyPath);
			if (prop_poolConfigName != null) this.setPoolConfigName(prop_poolConfigName);
			if (prop_poolGenesisTxn != null) this.setPoolGenesisTxn(prop_poolGenesisTxn);
			if (prop_walletName != null) this.setWalletName(prop_walletName);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public ResolutionResult resolve(String identifier) throws ResolutionException {

		// open pool

		if (this.getPool() == null || this.getWallet() == null || this.getSubmitterDid() == null) this.openIndy();

		// parse identifier

		Matcher matcher = DID_SOV_PATTERN.matcher(identifier);
		if (! matcher.matches()) return null;

		String targetDid = matcher.group(1);

		// send GET_NYM request

		String getNymResponse;

		try {

			String getNymRequest = Ledger.buildGetNymRequest(this.getSubmitterDid(), targetDid).get();
			getNymResponse = Ledger.signAndSubmitRequest(this.getPool(), this.getWallet(), this.getSubmitterDid(), getNymRequest).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new ResolutionException("Cannot send GET_NYM request: " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("GET_NYM for " + targetDid + ": " + getNymResponse);

		// GET_NYM response data

		JsonObject jsonGetNymResponse = gson.fromJson(getNymResponse, JsonObject.class);
		JsonObject jsonGetNymResult = jsonGetNymResponse == null ? null : jsonGetNymResponse.getAsJsonObject("result");
		JsonElement jsonGetNymData = jsonGetNymResult == null ? null : jsonGetNymResult.get("data");
		JsonObject jsonGetNymDataContent = (jsonGetNymData == null || jsonGetNymData instanceof JsonNull) ? null : gson.fromJson(jsonGetNymData.getAsString(), JsonObject.class);

		if (jsonGetNymDataContent == null) return null;

		// send GET_ATTR request

		String getAttrResponse;

		try {

			String getAttrRequest = Ledger.buildGetAttribRequest(this.getSubmitterDid(), targetDid, "endpoint").get();
			getAttrResponse = Ledger.signAndSubmitRequest(this.getPool(), this.getWallet(), this.getSubmitterDid(), getAttrRequest).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new ResolutionException("Cannot send GET_NYM request: " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("GET_ATTR for " + targetDid + ": " + getAttrResponse);

		// GET_ATTR response data

		JsonObject jsonGetAttrResponse = gson.fromJson(getAttrResponse, JsonObject.class);
		JsonObject jsonGetAttrResult = jsonGetAttrResponse == null ? null : jsonGetAttrResponse.getAsJsonObject("result");
		JsonElement jsonGetAttrData = jsonGetAttrResult == null ? null : jsonGetAttrResult.get("data");
		JsonObject jsonGetAttrDataContent = (jsonGetAttrData == null || jsonGetAttrData instanceof JsonNull) ? null : gson.fromJson(jsonGetAttrData.getAsString(), JsonObject.class);

		// DID DOCUMENT id

		String id = identifier;

		// DID DOCUMENT publicKeys

		JsonPrimitive jsonGetNymVerkey = jsonGetNymDataContent == null ? null : jsonGetNymDataContent.getAsJsonPrimitive("verkey");

		String verkey = jsonGetNymVerkey == null ? null : jsonGetNymVerkey.getAsString();

		int keyNum = 0;
		List<PublicKey> publicKeys;
		List<Authentication> authentications;
		List<Encryption> encryptions;

		String keyId = id + "#key-" + (++keyNum);

		PublicKey publicKey = PublicKey.build(keyId, DIDDOCUMENT_PUBLICKEY_TYPES, null, verkey, null, null);
		publicKeys = Collections.singletonList(publicKey);

		Authentication authentication = Authentication.build(null, DIDDOCUMENT_AUTHENTICATION_TYPES, keyId);
		authentications = Collections.singletonList(authentication);

		encryptions = Collections.emptyList();

		// DID DOCUMENT services

		JsonObject jsonGetAttrEndpoint = jsonGetAttrDataContent == null ? null : jsonGetAttrDataContent.getAsJsonObject("endpoint");

		List<Service> services = new ArrayList<Service> ();

		if (jsonGetAttrEndpoint != null) {

			for (Map.Entry<String, JsonElement> jsonService : jsonGetAttrEndpoint.entrySet()) {

				JsonPrimitive jsonGetAttrEndpointValue = jsonGetAttrEndpoint == null ? null : jsonGetAttrEndpoint.getAsJsonPrimitive(jsonService.getKey());
				String value = jsonGetAttrEndpointValue == null ? null : jsonGetAttrEndpointValue.getAsString();

				Service service = Service.build(jsonService.getKey(), null, value);

				services.add(service);
			}
		}

		// create DID DOCUMENT

		DIDDocument didDocument = DIDDocument.build(id, publicKeys, authentications, encryptions, services);

		// create DRIVER METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		methodMetadata.put("nymResponse", gson.fromJson(jsonGetNymResponse, Map.class));
		methodMetadata.put("attrResponse", gson.fromJson(jsonGetAttrResponse, Map.class));

		// create RESOLUTION RESULT

		ResolutionResult resolutionResult = ResolutionResult.build(didDocument, null, methodMetadata);

		// done

		return resolutionResult;
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
	}

	private void openIndy() throws ResolutionException {

		// initialize libindy

		if ((! LibIndy.isInitialized()) && this.getLibIndyPath() != null) {

			if (log.isInfoEnabled()) log.info("Initializing libindy: " + this.getLibIndyPath() + " (" + new File(this.getLibIndyPath()).getAbsolutePath() + ")");
			LibIndy.init(this.getLibIndyPath());
		}

		// create pool config

		try {

			CreatePoolLedgerConfigJSONParameter createPoolLedgerConfigJSONParameter = new CreatePoolLedgerConfigJSONParameter(this.getPoolGenesisTxn());
			Pool.createPoolLedgerConfig(this.getPoolConfigName(), createPoolLedgerConfigJSONParameter.toJson()).get();
			if (log.isInfoEnabled()) log.info("Pool config " + this.getPoolConfigName() + " successfully created.");
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			IndyException iex = null;
			if (ex instanceof IndyException) iex = (IndyException) ex;
			if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
			if (iex instanceof PoolLedgerConfigExistsException) {

				if (log.isInfoEnabled()) log.info("Pool config " + this.getPoolConfigName() + " has already been created.");
			} else {

				throw new ResolutionException("Cannot create pool config " + this.getPoolConfigName() + ": " + ex.getMessage(), ex);
			}
		}

		// create wallet

		try {

			Wallet.createWallet(this.getPoolConfigName(), this.getWalletName(), "default", null, null).get();
			if (log.isInfoEnabled()) log.info("Wallet " + this.getWalletName() + " successfully created.");
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			IndyException iex = null;
			if (ex instanceof IndyException) iex = (IndyException) ex;
			if (ex instanceof ExecutionException && ex.getCause() instanceof IndyException) iex = (IndyException) ex.getCause();
			if (iex instanceof WalletExistsException) {

				if (log.isInfoEnabled()) log.info("Wallet " + this.getWalletName() + " has already been created.");
			} else {

				throw new ResolutionException("Cannot create wallet " + this.getWalletName() + ": " + ex.getMessage(), ex);
			}
		}

		// open pool

		try {

			OpenPoolLedgerJSONParameter openPoolLedgerJSONParameter = new OpenPoolLedgerJSONParameter(Boolean.TRUE, null, null);
			this.pool = Pool.openPoolLedger(this.getPoolConfigName(), openPoolLedgerJSONParameter.toJson()).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new ResolutionException("Cannot open pool " + this.getPoolConfigName() + ": " + ex.getMessage(), ex);
		}

		// open wallet

		try {

			this.wallet = Wallet.openWallet(this.getWalletName(), null, null).get();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new ResolutionException("Cannot open wallet " + this.getWalletName() + ": " + ex.getMessage(), ex);
		}

		// create submitter DID

		try {

			CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameterTrustee = new CreateAndStoreMyDidJSONParameter(null, null, null, null);
			CreateAndStoreMyDidResult createAndStoreMyDidResultTrustee = Signus.createAndStoreMyDid(this.getWallet(), createAndStoreMyDidJSONParameterTrustee.toJson()).get();
			this.submitterDid = createAndStoreMyDidResultTrustee.getDid();
		} catch (IndyException | InterruptedException | ExecutionException ex) {

			throw new ResolutionException("Cannot create submitter DID: " + ex.getMessage(), ex);
		}
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

	public String getLibIndyPath() {

		return this.libIndyPath;
	}

	public void setLibIndyPath(String libIndyPath) {

		this.libIndyPath = libIndyPath;
	}

	public String getPoolConfigName() {

		return this.poolConfigName;
	}

	public void setPoolConfigName(String poolConfigName) {

		this.poolConfigName = poolConfigName;
	}

	public String getPoolGenesisTxn() {

		return this.poolGenesisTxn;
	}

	public void setPoolGenesisTxn(String poolGenesisTxn) {

		this.poolGenesisTxn = poolGenesisTxn;
	}

	public String getWalletName() {

		return this.walletName;
	}

	public void setWalletName(String walletName) {

		this.walletName = walletName;
	}

	public Pool getPool() {

		return this.pool;
	}

	public void setPool(Pool pool) {

		this.pool = pool;
	}

	public Wallet getWallet() {

		return this.wallet;
	}

	public void setWallet(Wallet wallet) {

		this.wallet = wallet;
	}

	public String getSubmitterDid() {

		return this.submitterDid;
	}

	public void setSubmitterDid(String submitterDid) {

		this.submitterDid = submitterDid;
	}
}
