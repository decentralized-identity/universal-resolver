package uniresolver.driver.did.ccp;

import did.Authentication;
import did.DIDDocument;
import did.PublicKey;
import did.Service;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DidCcpDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidCcpDriver.class);

	public static final Pattern DID_CCP_PATTERN = Pattern.compile("^did:ccp:([123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{25,34})$");

	public static final String[] DIDDOCUMENT_PUBLICKEY_TYPES = new String[] { "Secp256k1" };

	public static final String[] DIDDOCUMENT_AUTHENTICATION_TYPES = new String[] { "Secp256k1" };

	public static final String DEFAULT_CCP_URL = "https://did.baidu.com";

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

	private String ccpUrl = DEFAULT_CCP_URL;

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;

	public DidCcpDriver() {
	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {
		// match
		Matcher matcher = DID_CCP_PATTERN.matcher(identifier);
		if(!matcher.matches()) {
			return null;
		}

		// fetch data from CCP
		String resolveUrl = this.getCCPUrl() + "/v1/did/resolve/" + identifier;
		HttpGet httpGet = new HttpGet(resolveUrl);

		// find the DDO
		JSONObject didDocumentDO;
		try {
			CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet);
			if(httpResponse.getStatusLine().getStatusCode() != 200) {
				throw new ResolutionException("Cannot retrieve DDO for `" + identifier + "` from `" + this.getCCPUrl() + ": " + httpResponse.getStatusLine());
			}

			// extract payload
			HttpEntity httpEntity = httpResponse.getEntity();
			String entityString = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			// get DDO
			JSONObject jo = new JSONObject(entityString);
			didDocumentDO = jo.getJSONObject("content").getJSONObject("didDocument");
		} catch (IOException ex) {
			throw new ResolutionException("Cannot retrieve DDO info for `" + identifier + "` from `" + this.getCCPUrl() + "`: " + ex.getMessage(), ex);
		} catch (JSONException jex) {
			throw new ResolutionException("Cannot parse JSON response from `" + this.getCCPUrl() + "`: " + jex.getMessage(), jex);
		}

		// DDO id

		// DDO publicKeys
		List<PublicKey> publicKeys = new ArrayList<>();
		// index 0 is auth key
		JSONObject firstKeyJO = didDocumentDO.getJSONArray("publicKey").getJSONObject(0);
		publicKeys.add(PublicKey.build(
				firstKeyJO.getString("id"),
				DIDDOCUMENT_PUBLICKEY_TYPES,
				null,
				null,
				firstKeyJO.getString("publicKeyHex"),
				null));
		// index 1 is recovery key
		JSONObject secondKeyJO = didDocumentDO.getJSONArray("publicKey").getJSONObject(1);
		publicKeys.add(PublicKey.build(
				secondKeyJO.getString("id"),
				DIDDOCUMENT_PUBLICKEY_TYPES,
				null,
				null,
				secondKeyJO.getString("publicKeyHex"),
				null));

		// DDO Authentications
		List<Authentication> authentications = new ArrayList<>();
		authentications.add(Authentication.build(
				null,
				DIDDOCUMENT_AUTHENTICATION_TYPES,
				firstKeyJO.getString("id")));

		// DDO services
		List<Service> services = new ArrayList<>();
		JSONArray serviceJA = didDocumentDO.getJSONArray("service");
		for (int i = 0; i < serviceJA.length(); i++) {
			JSONObject service = serviceJA.getJSONObject(i);
			services.add(Service.build(service.getString("type"), service.getString("serviceEndpoint")));
		}

		// create DDO
		DIDDocument didDocument = DIDDocument.build(identifier, publicKeys, authentications, services);

		// create Method METADATA
		Map<String, Object> methodMetadata = new LinkedHashMap<>();
		methodMetadata.put("version", didDocumentDO.getInt("version"));
		methodMetadata.put("proof", didDocumentDO.getJSONObject("proof").toMap());
		methodMetadata.put("created", didDocumentDO.getString("created"));
		methodMetadata.put("updated", didDocumentDO.getString("updated"));

		// create RESOLVE RESULT
		ResolveResult resolveResult = ResolveResult.build(didDocument, null, methodMetadata);

		// done
		return resolveResult;
	}

	@Override
	public Map<String, Object> properties() {

		Map<String, Object> properties = new HashMap<>();
		properties.put("ccpUrl", this.getCCPUrl());

		return properties;
	}

	/*
	 * Getters and setters
	 */

	public String getCCPUrl() {

		return this.ccpUrl;
	}

	public void setCCPUrl(String ccpUrl) {
		this.ccpUrl = ccpUrl;
	}

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}
}
