package uniresolver.driver.did.work;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import did.DIDDocument;
import did.PublicKey;
import did.Service;
import did.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

public class DIDWorkDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DIDWorkDriver.class);

	private final Pattern DID_WORK_PATTERN = Pattern.compile("^did:work:.+");

	private String WORK_DOMAIN;
	private String API_KEY;

	public DIDWorkDriver() {
		this.getPropertiesFromEnvironment();
	}

	private void getPropertiesFromEnvironment() {
		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());
		try {
			this.setWorkDomain(System.getenv("uniresolver_driver_did_work_domain"));
			this.setApiKey(System.getenv("uniresolver_driver_did_work_apikey"));
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
	private JSONObject getDocumentFromWork(String identifier) throws Exception {
		HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpGet httpGet = new HttpGet(WORK_DOMAIN + "/v1/did/" + identifier);
		httpGet.setHeader("x-api-key", API_KEY);

		CloseableHttpResponse httpResponse = (CloseableHttpResponse) client.execute(httpGet);
		if(httpResponse.getStatusLine().getStatusCode() != 200)
			throw new Exception("Cannot retrieve DIDDocument for DID " + identifier);

		HttpEntity httpEntity = httpResponse.getEntity();
		String entityString = EntityUtils.toString(httpEntity);
		EntityUtils.consume(httpEntity);

		return new JSONObject(entityString);
	}

	private static PublicKey buildPublicKey(String id, String[] type, String publicKey, String controller) {
		PublicKey pubKey = PublicKey.build(id, type, null, publicKey, null, null);
		pubKey.setJsonLdObjectKeyValue("controller", controller);
		return pubKey;
	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("Resolving identifier " + identifier);

		// match
		Matcher matcher = DID_WORK_PATTERN.matcher(identifier);
		if (!matcher.find()) {
			return null;
		}

		// fetch document from Workday Credentialing Platform
		JSONObject returnedDocument;
		try {
			returnedDocument = getDocumentFromWork(identifier);
		} catch (Exception e) {
			throw new ResolutionException(e);
		}

		// get the pubkeys
		List<PublicKey> pubKeys = new ArrayList<>();
		try {
			JSONArray pubKeysJSON = returnedDocument.getJSONArray("publicKey");
			for (int i = 0; i < pubKeysJSON.length(); i ++) {
				JSONObject pubKey = pubKeysJSON.getJSONObject(i);
				String controller = null;
				if (pubKey.has("controller")) {
					controller = pubKey.getString("controller");
				}
				PublicKey publicKey = buildPublicKey(pubKey.getString("id"), new String[] { pubKey.getString("type") }, pubKey.getString("publicKeyBase58"), controller);
				pubKeys.add(publicKey);

			}
		} catch (JSONException e) {
			throw new ResolutionException(e);
		}

		// get the service endpoints
		List<Service> serviceEndpoints = new ArrayList<>();
		if (!returnedDocument.isNull("service")) {
			try {
				JSONArray serviceEndpointsJSON = returnedDocument.getJSONArray("service");
				for (int i = 0; i < serviceEndpointsJSON.length(); i++) {
					JSONObject serviceEndpoint = serviceEndpointsJSON.getJSONObject(i);
					Service service = Service.build(serviceEndpoint.getString("type"), serviceEndpoint.getString("serviceEndpoint"));
					serviceEndpoints.add(service);

				}
			} catch (JSONException e) {
				throw new ResolutionException(e);
			}
		}

		// get the authentication keys
		List<Authentication> authenticationKeys = new ArrayList<>();
		if (!returnedDocument.isNull("authentication")) {
			try {
				JSONArray authenticationKeysJSON = returnedDocument.getJSONArray("authentication");
				for (int i = 0; i < authenticationKeysJSON.length(); i++) {
					Object authenticationKeyObj = authenticationKeysJSON.get(i);
					JSONObject authenticationKey = (JSONObject) authenticationKeyObj;
					Authentication authentication = Authentication.build(authenticationKey.getString("id"), new String[]{authenticationKey.getString("types")}, authenticationKey.getString("publicKey"));
					authenticationKeys.add(authentication);
				}
			} catch (JSONException e) {
				throw new ResolutionException(e);
			}
		}

		// todo: get the proof value
		// did-common-java doesn't seem to support proof yet
		// consider making a PR

		// create DDO
		DIDDocument didDocument = DIDDocument.build(identifier, pubKeys, authenticationKeys, serviceEndpoints);

		// done
		return ResolveResult.build(didDocument);
	}

	@Override
	public Map<String, Object> properties() {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("workEndpoint", WORK_DOMAIN);
		return props;
	}

	public void setWorkDomain(String workDomain) {
		this.WORK_DOMAIN = workDomain;
	}

	public void setApiKey(String apiKey) {
		this.API_KEY = apiKey;
	}
}