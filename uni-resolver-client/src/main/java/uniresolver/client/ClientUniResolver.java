package uniresolver.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.result.ResolveResult;
import uniresolver.util.HttpBindingClientUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientUniResolver implements UniResolver {

	private static final Logger log = LoggerFactory.getLogger(ClientUniResolver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final Map<String, String> DEFAULT_HTTP_HEADERS = Collections.emptyMap();
	public static final URI DEFAULT_RESOLVE_URI = URI.create("http://localhost:8080/1.0/identifiers");
	public static final URI DEFAULT_PROPERTIES_URI = URI.create("http://localhost:8080/1.0/properties");
	public static final URI DEFAULT_METHODS_URI = URI.create("http://localhost:8080/1.0/methods");
	public static final URI DEFAULT_TEST_IDENTIFIERS_URI = URI.create("http://localhost:8080/1.0/testIdentifiers");
	public static final URI DEFAULT_TRAITS_URI = URI.create("http://localhost:8080/1.0/traits");

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private Map<String, String> httpHeaders = DEFAULT_HTTP_HEADERS;
	private URI resolveUri = DEFAULT_RESOLVE_URI;
	private URI propertiesUri = DEFAULT_PROPERTIES_URI;
	private URI methodsUri = DEFAULT_METHODS_URI;
	private URI testIdentifiersUri = DEFAULT_TEST_IDENTIFIERS_URI;
	private URI traitsUri = DEFAULT_TRAITS_URI;
	private boolean supportsOptions = false;
	private String acceptHeaderValue = null;

	public ClientUniResolver() {

	}

	public static ClientUniResolver create(URI baseUri) {

		if (! baseUri.toString().endsWith("/")) baseUri = URI.create(baseUri + "/");

		ClientUniResolver clientUniResolver = new ClientUniResolver();
		clientUniResolver.setResolveUri(URI.create(baseUri + "identifiers"));
		clientUniResolver.setPropertiesUri(URI.create(baseUri + "properties"));
		clientUniResolver.setMethodsUri(URI.create(baseUri + "methods"));
		clientUniResolver.setTestIdentifiersUri(URI.create(baseUri + "testIdentifiers"));
		clientUniResolver.setTraitsUri(URI.create(baseUri + "traits"));

		return clientUniResolver;
	}

	@Override
	public ResolveResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {

		if (log.isDebugEnabled()) log.debug("resolve(" + didString + ") with options: " + resolutionOptions);

		if (didString == null) throw new NullPointerException();
		if (resolutionOptions == null) resolutionOptions = new HashMap<>();

		// set HTTP URI

		StringBuilder uriString = new StringBuilder(this.getResolveUri().toString());

		if (! uriString.toString().endsWith("/")) uriString.append("/");
		Map<String, Object> optionsForHttp;
		if (this.getSupportsOptions() && ! (optionsForHttp = HttpBindingClientUtil.optionsForHttp(resolutionOptions)).isEmpty()) {
			uriString.append(URLEncoder.encode(didString, StandardCharsets.UTF_8));
			uriString.append("?");
			uriString.append(HttpBindingClientUtil.httpQueryStringForOptions(optionsForHttp));
		} else {
			uriString.append(didString);
		}

		// set Accept header

		String accept = (String) resolutionOptions.get("accept");
		if (this.getAcceptHeaderValue() != null) accept = this.getAcceptHeaderValue();
		if (accept == null) throw new ResolutionException("No 'accept' provided in 'resolutionOptions' for resolve(), or in driver configuration.");

        List<String> acceptMediaTypes = accept.isBlank() ? Collections.singletonList(ResolveResult.MEDIA_TYPE) : Arrays.asList(ResolveResult.MEDIA_TYPE, accept);
		String acceptMediaTypesString = String.join(",", acceptMediaTypes);

		if (log.isDebugEnabled()) log.debug("Setting Accept: header to " + acceptMediaTypesString);

		// prepare HTTP request

		HttpGet httpGet = new HttpGet(URI.create(uriString.toString()));
		httpGet.addHeader("Accept", acceptMediaTypesString);
		if (this.getHttpHeaders() != null) this.getHttpHeaders().forEach(httpGet::addHeader);

		// execute HTTP request and read response

		ResolveResult resolveResult = null;

		if (log.isDebugEnabled()) log.debug("Request for DID " + didString + " to " + uriString + " with Accept: header " + acceptMediaTypesString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			// execute HTTP request

			HttpEntity httpEntity = httpResponse.getEntity();
			int httpCode = httpResponse.getCode();
			String httpReasonPhrase = httpResponse.getReasonPhrase();
			ContentType httpContentType = ContentType.parse(httpResponse.getEntity().getContentType());
			Charset httpCharset = (httpContentType != null && httpContentType.getCharset() != null) ? httpContentType.getCharset() : StandardCharsets.ISO_8859_1;

			if (log.isDebugEnabled()) log.debug("Response HTTP status from " + uriString + ": " + httpCode + " " + httpReasonPhrase);
			if (log.isDebugEnabled()) log.debug("Response HTTP content type from " + uriString + ": " + httpContentType + " / " + httpCharset);

			// read result

			byte[] httpBodyBytes = EntityUtils.toByteArray(httpEntity);
			String httpBodyString = new String(httpBodyBytes, httpCharset);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response HTTP body from " + uriString + ": " + httpBodyString);

			if (httpContentType != null && (HttpBindingClientUtil.isResolveResultContentType(httpContentType) || HttpBindingClientUtil.isResolveResultHttpContent(httpBodyString))) {
				resolveResult = HttpBindingClientUtil.fromHttpBodyResolveResult(httpBodyString);
			}

			if (httpCode == 404 && resolveResult == null) {
				throw new ResolutionException(ResolutionException.ERROR_NOT_FOUND, httpCode + " " + httpReasonPhrase + " (" + httpBodyString + ")");
			}

			if (httpCode == 406 && resolveResult == null) {
				throw new ResolutionException(ResolutionException.ERROR_REPRESENTATION_NOT_SUPPORTED, httpCode + " " + httpReasonPhrase + " (" + httpBodyString + ")");
			}

			if (httpCode != 200 && resolveResult == null) {
				throw new ResolutionException(ResolutionException.ERROR_INTERNAL_ERROR, "Cannot retrieve RESOLVE result for " + didString + ": " + httpCode + " " + httpReasonPhrase + " (" + httpBodyString + ")");
			}

			if (resolveResult != null && resolveResult.isErrorResult()) {
				if (log.isWarnEnabled()) log.warn("Received RESOLVE result: " + resolveResult.getErrorType() + " (" + resolveResult.getErrorTitle() + ")" + " -> " + resolveResult.getErrorDetail());
				throw ResolutionException.fromResolveResult(resolveResult);
			}

			if (resolveResult == null) {
				resolveResult = HttpBindingClientUtil.fromHttpBodyDidDocument(httpContentType, httpBodyBytes);
			}
		} catch (ResolutionException ex) {

			throw ex;
		} catch (Exception ex) {

			throw new ResolutionException("Cannot retrieve RESOLVE result for " + didString + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved RESOLVE result for " + didString + " (" + uriString + "): " + resolveResult);

		// done

		return resolveResult;
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws ResolutionException {

		// prepare HTTP request

		String uriString = this.getPropertiesUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", UniResolver.PROPERTIES_MEDIA_TYPE);

		// execute HTTP request

		Map<String, Map<String, Object>> properties;

		if (log.isDebugEnabled()) log.debug("Request to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			int httpCode = httpResponse.getCode();
			String httpReasonPhrase = httpResponse.getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + httpCode + " " + httpReasonPhrase);

			if (httpCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpCode > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve PROPERTIES from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			properties = (Map<String, Map<String, Object>>) objectMapper.readValue(httpBody, LinkedHashMap.class);
		} catch (IOException | ParseException ex) {

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
		httpGet.addHeader("Accept", UniResolver.METHODS_MEDIA_TYPE);

		// execute HTTP request

		Set<String> methods;

		if (log.isDebugEnabled()) log.debug("Request to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			int httpCode = httpResponse.getCode();
			String httpReasonPhrase = httpResponse.getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + httpCode + " " + httpReasonPhrase);

			if (httpCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpCode > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve METHODS from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			methods = (Set<String>) objectMapper.readValue(httpBody, LinkedHashSet.class);
		} catch (IOException | ParseException ex) {

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
		httpGet.addHeader("Accept", UniResolver.TEST_IDENTIFIER_MEDIA_TYPE);

		// execute HTTP request

		Map<String, List<String>> testIdentifiers;

		if (log.isDebugEnabled()) log.debug("Request to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			int httpCode = httpResponse.getCode();
			String httpReasonPhrase = httpResponse.getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + httpCode + " " + httpReasonPhrase);

			if (httpCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpCode > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve TEST IDENTIFIERS from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			testIdentifiers = (Map<String, List<String>>) objectMapper.readValue(httpBody, LinkedHashMap.class);
		} catch (IOException | ParseException ex) {

			throw new ResolutionException("Cannot retrieve TEST IDENTIFIERS from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved TEST IDENTIFIERS (" + uriString + "): " + testIdentifiers);

		// done

		return testIdentifiers;
	}

	@Override
	public Map<String, Map<String, Object>> traits() throws ResolutionException {

		// prepare HTTP request

		String uriString = this.getTraitsUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", UniResolver.TRAITS_MEDIA_TYPE);

		// execute HTTP request

		Map<String, Map<String, Object>> traits;

		if (log.isDebugEnabled()) log.debug("Request to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			int httpCode = httpResponse.getCode();
			String httpReasonPhrase = httpResponse.getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + httpCode + " " + httpReasonPhrase);

			if (httpCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpCode > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve TRAITS from " + uriString + ": " + httpBody);
				throw new ResolutionException(httpBody);
			}

			traits = (Map<String, Map<String, Object>>) objectMapper.readValue(httpBody, LinkedHashMap.class);
		} catch (IOException | ParseException ex) {

			throw new ResolutionException("Cannot retrieve TRAITS from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved TRAITS (" + uriString + "): " + traits);

		// done

		return traits;
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

	public Map<String, String> getHttpHeaders() {
		return this.httpHeaders;
	}

	public void setHttpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
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

	public URI getTraitsUri() {
		return traitsUri;
	}

	public void setTraitsUri(URI traitsUri) {
		this.traitsUri = traitsUri;
	}

	public boolean getSupportsOptions() {
		return supportsOptions;
	}

	public void setSupportsOptions(boolean supportsOptions) {
		this.supportsOptions = supportsOptions;
	}

	public String getAcceptHeaderValue() {
		return acceptHeaderValue;
	}

	public void setAcceptHeaderValue(String acceptHeaderValue) {
		this.acceptHeaderValue = acceptHeaderValue;
	}
}
