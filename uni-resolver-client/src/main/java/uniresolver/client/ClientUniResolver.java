package uniresolver.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;
import uniresolver.util.HttpBindingClientUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

public class ClientUniResolver implements UniResolver {

	private static Logger log = LoggerFactory.getLogger(ClientUniResolver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private HttpClient httpClient = HttpClients.createDefault();
	private URI resolveUri = URI.create("http://localhost:8080/1.0/identifiers");
	private URI propertiesUri = URI.create("http://localhost:8080/1.0/properties");
	private URI methodsUri = URI.create("http://localhost:8080/1.0/methods");
	private URI testIdentifiersUri = URI.create("http://localhost:8080/1.0/testIdentifiers");

	public ClientUniResolver() {

	}

	public static ClientUniResolver create(URI baseUri) {

		if (! baseUri.toString().endsWith("/")) baseUri = URI.create(baseUri.toString() + "/");

		ClientUniResolver clientUniResolver = new ClientUniResolver();
		clientUniResolver.setResolveUri(URI.create(baseUri.toString() + "identifiers"));
		clientUniResolver.setPropertiesUri(URI.create(baseUri.toString() + "properties"));
		clientUniResolver.setMethodsUri(URI.create(baseUri.toString() + "methods"));
		clientUniResolver.setTestIdentifiersUri(URI.create(baseUri.toString() + "testIdentifiers"));

		return clientUniResolver;
	}

	@Override
	public ResolveRepresentationResult resolveRepresentation(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {

		if (log.isDebugEnabled()) log.debug("resolveRepresentation(" + didString + ")  with options: " + resolutionOptions);

		if (didString == null) throw new NullPointerException();
		if (resolutionOptions == null) resolutionOptions = new HashMap<>();

		// set HTTP URI

		String uriString = this.getResolveUri().toString();

		if (! uriString.endsWith("/")) uriString += "/";
		uriString += didString;

		// set Accept header

		String accept = (String) resolutionOptions.get("accept");
		if (accept == null) throw new ResolutionException("No 'accept' provided in 'resolutionOptions' for resolveRepresentation().");

		List<String> acceptMediaTypes = Arrays.asList(ResolveResult.MEDIA_TYPE, accept);
		String acceptMediaTypesString = String.join(",", acceptMediaTypes);

		if (log.isDebugEnabled()) log.debug("Setting Accept: header to " + acceptMediaTypesString);

		// prepare HTTP request

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", acceptMediaTypesString);

		// execute HTTP request and read response

		ResolveRepresentationResult resolveRepresentationResult = null;

		if (log.isDebugEnabled()) log.debug("Request for DID " + didString + " to " + uriString + " with Accept: header " + acceptMediaTypesString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			// execute HTTP request

			HttpEntity httpEntity = httpResponse.getEntity();
			int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
			String httpStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
			ContentType httpContentType = ContentType.get(httpResponse.getEntity());
			Charset httpCharset = (httpContentType != null && httpContentType.getCharset() != null) ? httpContentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;

			if (log.isDebugEnabled()) log.debug("Response HTTP status from " + uriString + ": " + httpStatusCode + " " + httpStatusMessage);
			if (log.isDebugEnabled()) log.debug("Response HTTP content type from " + uriString + ": " + httpContentType + " / " + httpCharset);

			// read result

			byte[] httpBodyBytes = EntityUtils.toByteArray(httpEntity);
			String httpBodyString = new String(httpBodyBytes, httpCharset);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response HTTP body from " + uriString + ": " + httpBodyString);

			if ((httpContentType != null && ResolveResult.isResolveResultMediaType(httpContentType)) || HttpBindingClientUtil.isResolveResultHttpContent(httpBodyString)) {
				resolveRepresentationResult = HttpBindingClientUtil.fromHttpBodyResolveRepresentationResult(httpBodyString, httpContentType);
			}

			if (httpStatusCode == 404 && resolveRepresentationResult == null) {
				throw new ResolutionException(ResolutionException.ERROR_NOTFOUND, httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (httpStatusCode == 406 && resolveRepresentationResult == null) {
				throw new ResolutionException(ResolutionException.ERROR_REPRESENTATIONNOTSUPPORTED, httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (httpStatusCode != 200 && resolveRepresentationResult == null) {
				throw new ResolutionException(ResolutionException.ERROR_INTERNALERROR, "Cannot retrieve RESOLVE result for " + didString + ": " + httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (resolveRepresentationResult != null && resolveRepresentationResult.isErrorResult()) {
				if (log.isWarnEnabled()) log.warn(resolveRepresentationResult.getError() + " -> " + resolveRepresentationResult.getErrorMessage());
				throw new ResolutionException(resolveRepresentationResult);
			}

			if (resolveRepresentationResult == null) {
				resolveRepresentationResult = HttpBindingClientUtil.fromHttpBodyDidDocument(httpBodyBytes, httpContentType);
			}
		} catch (ResolutionException ex) {

			throw ex;
		} catch (Exception ex) {

			throw new ResolutionException("Cannot retrieve RESOLVE result for " + didString + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved RESOLVE result for " + didString + " (" + uriString + "): " + resolveRepresentationResult);

		// done

		return resolveRepresentationResult.toResolveRepresentationResult(accept);
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

				if (log.isWarnEnabled()) log.warn("Cannot retrieve PROPERTIES from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			properties = (Map<String, Map<String, Object>>) objectMapper.readValue(httpBody, LinkedHashMap.class);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve PROPERTIES from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved PROPERTIES (" + uriString + "): " + properties);

		// done

		return properties;
	}

	@Override
	public Set<String> methods() throws ResolutionException {

		// prepare HTTP request

		String uriString = this.getMethodsUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", UniResolver.METHODS_MIME_TYPE);

		// execute HTTP request

		Set<String> methods;

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

				if (log.isWarnEnabled()) log.warn("Cannot retrieve METHODS from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			methods = (Set<String>) objectMapper.readValue(httpBody, LinkedHashSet.class);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve METHODS from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved METHODS (" + uriString + "): " + methods);

		// done

		return methods;
	}

	@Override
	public Map<String, List<String>> testIdentifiers() throws ResolutionException {

		// prepare HTTP request

		String uriString = this.getTestIdentifiersUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", UniResolver.TEST_IDENTIFIER_MIME_TYPE);

		// execute HTTP request

		Map<String, List<String>> testIdentifiers;

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

				if (log.isWarnEnabled()) log.warn("Cannot retrieve TEST IDENTIFIERS from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			testIdentifiers = (Map<String, List<String>>) objectMapper.readValue(httpBody, LinkedHashMap.class);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve TEST IDENTIFIERS from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved TEST IDENTIFIERS (" + uriString + "): " + testIdentifiers);

		// done

		return testIdentifiers;
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

	public URI getMethodsUri() {

		return this.methodsUri;
	}

	public void setMethodsUri(URI methodsUri) {

		this.methodsUri = methodsUri;
	}

	public URI getTestIdentifiersUri() {

		return this.testIdentifiersUri;
	}

	public void setTestIdentifiersUri(URI testIdentifiersUri) {

		this.testIdentifiersUri = testIdentifiersUri;
	}
}
