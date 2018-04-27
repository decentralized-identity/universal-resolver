package uniresolver.driver.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import uniresolver.ResolutionException;
import uniresolver.did.DIDDocument;
import uniresolver.driver.Driver;
import uniresolver.result.ResolutionResult;

public class HttpDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(HttpDriver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_RESOLVE_URI = null;
	public static final URI DEFAULT_PROPERTIES_URI = null;
	public static final Pattern DEFAULT_PATTERN = null;
	public static final boolean DEFAULT_RAW_IDENTIFIER = false;
	public static final boolean DEFAULT_RAW_DID_DOCUMENT = false;

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI resolveUri = DEFAULT_RESOLVE_URI;
	private URI propertiesUri = DEFAULT_PROPERTIES_URI;
	private Pattern pattern = DEFAULT_PATTERN;
	private boolean rawIdentifier = DEFAULT_RAW_IDENTIFIER;
	private boolean rawDidDocument = DEFAULT_RAW_DID_DOCUMENT;

	public HttpDriver() {

	}

	@Override
	public ResolutionResult resolve(String identifier) throws ResolutionException {

		if (this.getPattern() == null || this.getResolveUri() == null) return null;

		// match identifier

		String matchedIdentifier = null;

		if (this.getPattern() != null) {

			Matcher matcher = this.getPattern().matcher(identifier);

			if (! matcher.matches()) {

				if (log.isDebugEnabled()) log.debug("Skipping identifier " + identifier + " - does not match pattern " + this.getPattern());
				return null;
			} else {

				if (log.isDebugEnabled()) log.debug("Identifier " + identifier + " matches pattern " + this.getPattern() + " with " + matcher.groupCount() + " groups");
			}

			if (matcher.groupCount() > 0) {

				identifier = "";
				for (int i=1; i<=matcher.groupCount(); i++) if (matcher.group(i) != null) identifier += matcher.group(i);
			}
		}

		if (matchedIdentifier == null) matchedIdentifier = identifier;

		if (log.isDebugEnabled()) log.debug("Matched identifier: " + matchedIdentifier);

		// encode identifier

		String encodedIdentifier;

		try {

			encodedIdentifier = this.isRawIdentifier() ? matchedIdentifier : URLEncoder.encode(matchedIdentifier, "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new ResolutionException(ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Encoded identifier: " + encodedIdentifier);

		// prepare HTTP request

		String uriString = this.getResolveUri().toString();

		if (uriString.contains("$1")) {

			uriString = uriString.replace("$1", encodedIdentifier);
		} else {

			if (! uriString.endsWith("/")) uriString += "/";
			uriString += encodedIdentifier;
		}

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", this.isRawDidDocument() ? DIDDocument.MIME_TYPE : ResolutionResult.MIME_TYPE);

		// execute HTTP request

		ResolutionResult resolutionResult;

		if (log.isDebugEnabled()) log.debug("Request for identifier " + identifier + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve RESOLUTION RESULT for " + identifier + " from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			if (this.isRawDidDocument()) {

				resolutionResult = ResolutionResult.build(DIDDocument.fromJson(httpBody));
			} else {

				resolutionResult = ResolutionResult.fromJson(httpBody);
			}
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve RESOLUTION RESULT for " + identifier + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved RESOLUTION RESULT for " + identifier + " (" + uriString + "): " + resolutionResult);

		// done

		return resolutionResult;
	}

	@Override
	public Map<String, Object> properties() throws ResolutionException {

		// prepare properties

		Map<String, Object> httpProperties = new HashMap<String, Object> ();

		httpProperties.put("driverUri", this.getResolveUri().toString());
		httpProperties.put("pattern", this.getPattern().toString());
		httpProperties.put("rawIdentifier", Boolean.toString(this.isRawIdentifier()));
		httpProperties.put("rawDidDocument", Boolean.toString(this.isRawDidDocument()));

		Map<String, Object> properties = new HashMap<String, Object> ();
		properties.put("http", httpProperties);

		// remote properties

		Map<String, Object> remoteProperties = this.remoteProperties();
		if (remoteProperties != null) properties.putAll(remoteProperties);

		// done

		return properties;
	}

	public Map<String, Object> remoteProperties() throws ResolutionException {

		if (this.getPropertiesUri() == null) return null;

		// prepare HTTP request

		String uriString = this.getPropertiesUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", Driver.PROPERTIES_MIME_TYPE);

		// execute HTTP request

		Map<String, Object> properties;

		if (log.isDebugEnabled()) log.debug("Request to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve DRIVER PROPERTIES from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			properties = (Map<String, Object>) objectMapper.readValue(httpBody, Map.class);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve DRIVER PROPERTIES from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DRIVER PROPERTIES (" + uriString + "): " + properties);

		// done

		return properties;
	}

	/*
	 * Getters and setters
	 */

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}

	public URI getResolveUri() {

		return this.resolveUri;
	}

	public void setResolveUri(URI driverResolveUri) {

		this.resolveUri = driverResolveUri;
	}

	public void setDriverResolveUri(String driverResolveUri) {

		this.resolveUri = URI.create(driverResolveUri);
	}

	public URI getPropertiesUri() {

		return this.propertiesUri;
	}

	public void setPropertiesUri(URI driverPropertiesUri) {

		this.propertiesUri = driverPropertiesUri;
	}

	public void setDriverPropertiesUri(String driverPropertiesUri) {

		this.propertiesUri = URI.create(driverPropertiesUri);
	}

	public Pattern getPattern() {

		return this.pattern;
	}

	public void setPattern(Pattern pattern) {

		this.pattern = pattern;
	}

	public void setPattern(String pattern) {

		this.pattern = Pattern.compile(pattern);
	}

	public boolean isRawIdentifier() {

		return this.rawIdentifier;
	}

	public void setRawIdentifier(boolean rawIdentifier) {

		this.rawIdentifier = rawIdentifier;
	}

	public boolean isRawDidDocument() {

		return this.rawDidDocument;
	}

	public void setRawDidDocument(boolean rawDidDocument) {

		this.rawDidDocument = rawDidDocument;
	}
}
