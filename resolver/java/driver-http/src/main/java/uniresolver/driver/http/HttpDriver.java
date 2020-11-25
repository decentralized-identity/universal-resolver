package uniresolver.driver.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import foundation.identity.did.DIDDocument;
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
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

public class HttpDriver implements Driver {

	public static final String MIME_TYPES = ResolveResult.MIME_TYPE + "," + DIDDocument.MIME_TYPE_JSON_LD + "," + "application/ld+json";

	private static Logger log = LoggerFactory.getLogger(HttpDriver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_RESOLVE_URI = null;
	public static final URI DEFAULT_PROPERTIES_URI = null;
	public static final Pattern DEFAULT_PATTERN = null;
	public static final String DEFAULT_ENCODE_IDENTIFIER = null;

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI resolveUri = DEFAULT_RESOLVE_URI;
	private URI propertiesUri = DEFAULT_PROPERTIES_URI;
	private Pattern pattern = DEFAULT_PATTERN;
	private String encodeIdentifier = DEFAULT_ENCODE_IDENTIFIER;

	public HttpDriver() {

	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {

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

			if ("url".equals(this.getEncodeIdentifier())) encodedIdentifier = URLEncoder.encode(matchedIdentifier, "UTF-8");
			else encodedIdentifier = matchedIdentifier;
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
		httpGet.addHeader("Accept", MIME_TYPES);

		// execute HTTP request

		ResolveResult resolveResult;

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

				if (log.isWarnEnabled()) log.warn("Cannot retrieve RESOLVE RESULT for " + identifier + " from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			try {

				resolveResult = ResolveResult.fromJson(httpBody);
			} catch (Exception ex) {

				if (log.isWarnEnabled()) log.warn("No RESOLVE RESULT. Maybe DID DOCUMENT: " + httpBody + " (" + ex.getMessage());
				resolveResult = ResolveResult.build(DIDDocument.fromJson(httpBody));
			}
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve RESOLVE RESULT for " + identifier + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved RESOLVE RESULT for " + identifier + " (" + uriString + "): " + resolveResult);

		// done

		return resolveResult;
	}

	@Override
	public Map<String, Object> properties() throws ResolutionException {

		// prepare properties

		Map<String, Object> httpProperties = new HashMap<String, Object> ();

		if (this.getResolveUri() != null) httpProperties.put("resolveUri", this.getResolveUri().toString());
		if (this.getPropertiesUri() != null) httpProperties.put("propertiesUri", this.getPropertiesUri().toString());
		if (this.getPattern() != null) httpProperties.put("pattern", this.getPattern().toString());
		if (this.getEncodeIdentifier() != null) httpProperties.put("encodeIdentifier", this.getEncodeIdentifier());

		Map<String, Object> properties = new HashMap<String, Object> ();
		properties.put("http", httpProperties);

		// remote properties

		try {

			Map<String, Object> remoteProperties = this.remoteProperties();
			if (remoteProperties != null) properties.putAll(remoteProperties);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Cannot retrieve remote properties: " + ex.getMessage(), ex);
			properties.put("remotePropertiesException", ex.getMessage());
		}

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

	public void setResolveUri(URI resolveUri) {

		this.resolveUri = resolveUri;
	}

	public void setResolveUri(String resolveUri) {

		this.resolveUri = URI.create(resolveUri);
	}

	public URI getPropertiesUri() {

		return this.propertiesUri;
	}

	public void setPropertiesUri(URI propertiesUri) {

		this.propertiesUri = propertiesUri;
	}

	public void setPropertiesUri(String propertiesUri) {

		this.propertiesUri = URI.create(propertiesUri);
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

	public String getEncodeIdentifier() {

		return this.encodeIdentifier;
	}

	public void setEncodeIdentifier(String encodeIdentifier) {

		this.encodeIdentifier = encodeIdentifier;
	}
}
