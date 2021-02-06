package uniresolver.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;

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
import uniresolver.UniResolver;
import uniresolver.result.ResolveResult;

public class ClientUniResolver implements UniResolver {

	private static Logger log = LoggerFactory.getLogger(ClientUniResolver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_RESOLVE_URI = URI.create("http://localhost:8080/1.0/identifiers/");
	public static final URI DEFAULT_PROPERTIES_URI = URI.create("http://localhost:8080/1.0/properties");

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI resolveUri = DEFAULT_RESOLVE_URI;
	private URI propertiesUri = DEFAULT_PROPERTIES_URI;

	public ClientUniResolver() {

	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {

		return this.resolve(identifier, null);
	}

	@Override
	public ResolveResult resolve(String identifier, Map<String, String> options) throws ResolutionException {

		if (identifier == null) throw new NullPointerException();

		// encode identifier

		String encodedIdentifier;

		try {

			encodedIdentifier = URLEncoder.encode(identifier, "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new ResolutionException(ex.getMessage(), ex);
		}

		// prepare HTTP request

		String uriString = this.getResolveUri().toString();
		if (! uriString.endsWith("/")) uriString += "/";
		uriString += encodedIdentifier;

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", ResolveResult.MIME_TYPE);

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

			resolveResult = ResolveResult.fromJson(httpBody);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve RESOLVE RESULT for " + identifier + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved RESOLVE RESULT for " + identifier + " (" + uriString + "): " + resolveResult);

		// done

		return resolveResult;
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws ResolutionException {

		// prepare HTTP request

		String uriString = this.getPropertiesUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", UniResolver.PROPERTIES_MIME_TYPE);

		// execute HTTP request

		Map<String, Map<String, Object>> properties;

		if (log.isDebugEnabled()) log.debug("Request to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (httpResponse.getStatusLine().getStatusCode() == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve DRIVER PROPERTIES from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			properties = (Map<String, Map<String, Object>>) objectMapper.readValue(httpBody, Map.class);
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
}
