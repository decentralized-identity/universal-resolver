package uniresolver.driver.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.DIDURL;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;
import uniresolver.util.HttpBindingClientUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpDriver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(HttpDriver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final int HTTP_CLIENT_TIMEOUT = 60;

	private HttpClient httpClient;
	private Pattern pattern = null;
	private URI resolveUri = null;
	private URI propertiesUri = null;
	private boolean supportsDereference = false;
	private String acceptHeaderValue = null;
	private List<String> testIdentifiers = Collections.emptyList();
	private Map<String, Object> traits = Collections.emptyMap();

	public HttpDriver() {
		this.httpClient = buildDefaultHttpClient();
	}

	private static HttpClient buildDefaultHttpClient() {
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(HTTP_CLIENT_TIMEOUT * 1000)
				.setConnectionRequestTimeout(HTTP_CLIENT_TIMEOUT * 1000)
				.setSocketTimeout(HTTP_CLIENT_TIMEOUT * 1000)
				.build();
		return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
	}

	@Override
	public ResolveResult resolve(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {

		if (this.getPattern() == null || this.getResolveUri() == null) return null;

		// match string

		StringBuilder matchedString = null;

		if (this.getPattern() != null) {

			Matcher matcher = this.getPattern().matcher(did.getDidString());

			if (! matcher.matches()) {
				if (log.isDebugEnabled()) log.debug("Skipping identifier " + did + " - does not match pattern " + this.getPattern());
				return null;
			} else {
				if (log.isDebugEnabled()) log.debug("Identifier " + did + " matches pattern " + this.getPattern() + " with " + matcher.groupCount() + " groups");
			}

			if (matcher.groupCount() > 0) {

				matchedString = new StringBuilder();
				for (int i=1; i<=matcher.groupCount(); i++) if (matcher.group(i) != null) matchedString.append(matcher.group(i));
			}
		}

		if (matchedString == null) matchedString = new StringBuilder(did.getDidString());
		if (log.isDebugEnabled()) log.debug("Matched string: " + matchedString);

		// set HTTP URI

		String uriString = this.getResolveUri().toString();

		if (uriString.contains("$1")) {

			uriString = uriString.replace("$1", matchedString.toString());
		} else if (uriString.contains("$2")) {

			uriString = uriString.replace("$2", URLEncoder.encode(matchedString.toString(), StandardCharsets.UTF_8));
		} else {

			if (! uriString.endsWith("/")) uriString += "/";
			uriString += matchedString;
		}

		// set Accept header

		String accept = (String) resolutionOptions.get("accept");
		if (this.getAcceptHeaderValue() != null) accept = this.getAcceptHeaderValue();
		if (accept == null) throw new ResolutionException("No 'accept' provided in 'resolutionOptions' for resolve(), or in driver configuration.");

		List<String> acceptMediaTypes = Arrays.asList(ResolveResult.MEDIA_TYPE, accept);
		String acceptMediaTypesString = String.join(",", acceptMediaTypes);

		if (log.isDebugEnabled()) log.debug("Setting Accept: header to " + acceptMediaTypesString);

		// prepare HTTP request

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", acceptMediaTypesString);

		// execute HTTP request and read response

		ResolveResult resolveResult = null;

		if (log.isDebugEnabled()) log.debug("Driver request for DID " + did + " to " + uriString + " with Accept: header " + acceptMediaTypesString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			// execute HTTP request

			HttpEntity httpEntity = httpResponse.getEntity();
			int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
			String httpStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
			ContentType httpContentType = ContentType.get(httpResponse.getEntity());
			Charset httpCharset = (httpContentType != null && httpContentType.getCharset() != null) ? httpContentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;

			if (log.isDebugEnabled()) log.debug("Driver response HTTP status from " + uriString + ": " + httpStatusCode + " " + httpStatusMessage);
			if (log.isDebugEnabled()) log.debug("Driver response HTTP content type from " + uriString + ": " + httpContentType + " / " + httpCharset);

			// read result

			byte[] httpBodyBytes = EntityUtils.toByteArray(httpEntity);
			String httpBodyString = new String(httpBodyBytes, httpCharset);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Driver response HTTP body from " + uriString + ": " + httpBodyString);

			if (httpContentType != null && (ResolveResult.isMediaType(httpContentType) || HttpBindingClientUtil.isResolveResultHttpContent(httpBodyString))) {
				resolveResult = HttpBindingClientUtil.fromHttpBodyResolveResult(httpBodyString);
			}

			if (httpStatusCode == 404 && resolveResult == null) {
				throw new ResolutionException(ResolutionException.ERROR_NOTFOUND, httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (httpStatusCode == 406 && resolveResult == null) {
				throw new ResolutionException(ResolutionException.ERROR_REPRESENTATIONNOTSUPPORTED, httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (httpStatusCode != 200 && resolveResult == null) {
				throw new ResolutionException(ResolutionException.ERROR_INTERNALERROR, "Driver cannot retrieve result for " + did + ": " + httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (resolveResult != null && resolveResult.isErrorResult()) {
				if (log.isWarnEnabled()) log.warn("Driver received RESOLVE result: " + resolveResult.getError() + " -> " + resolveResult.getErrorMessage());
				throw ResolutionException.fromResolveResult(resolveResult);
			}

			if (resolveResult == null) {
				resolveResult = HttpBindingClientUtil.fromHttpBodyDidDocument(httpBodyBytes, httpContentType);
			}
		} catch (ResolutionException ex) {

			throw ex;
		} catch (Exception ex) {

			throw new ResolutionException("Driver cannot retrieve resolve result for " + did + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Driver retrieved resolve result for " + did + " (" + uriString + "): " + resolveResult);

		// done

		return resolveResult;
	}

	@Override
	public DereferenceResult dereference(DIDURL didUrl, Map<String, Object> dereferenceOptions) throws DereferencingException, ResolutionException {

		if (this.getPattern() == null || this.getResolveUri() == null) return null;

		// match string

		StringBuilder matchedString = null;

		if (this.getPattern() != null) {

			Matcher matcher = this.getPattern().matcher(didUrl.getDid().getDidString());

			if (! matcher.matches()) {
				if (log.isDebugEnabled()) log.debug("Skipping identifier " + didUrl.getDid() + " - does not match pattern " + this.getPattern());
				return null;
			} else {
				if (log.isDebugEnabled()) log.debug("Identifier " + didUrl.getDid() + " matches pattern " + this.getPattern() + " with " + matcher.groupCount() + " groups");
			}

			if (matcher.groupCount() > 0) {

				matchedString = new StringBuilder();
				for (int i=1; i<=matcher.groupCount(); i++) if (matcher.group(i) != null) matchedString.append(matcher.group(i));
			}
		}

		if (matchedString == null) matchedString = new StringBuilder(didUrl.getDid().getDidString());
		if (log.isDebugEnabled()) log.debug("Matched string: " + matchedString);

		// set HTTP URI

		String uriString = this.getResolveUri().toString();

		if (uriString.contains("$1")) {

			uriString = uriString.replace("$1", didUrl.toString());
		} else if (uriString.contains("$2")) {

			uriString = uriString.replace("$2", URLEncoder.encode(didUrl.toString(), StandardCharsets.UTF_8));
		} else {

			if (! uriString.endsWith("/")) uriString += "/";
			uriString += didUrl.toString();
		}

		// set Accept header

		String accept = (String) dereferenceOptions.get("accept");
		if (accept == null) throw new ResolutionException("No 'accept' provided in 'dereferenceOptions' for dereference().");

		List<String> acceptMediaTypes = Arrays.asList(DereferenceResult.MEDIA_TYPE, accept);
		String acceptMediaTypesString = String.join(",", acceptMediaTypes);

		if (log.isDebugEnabled()) log.debug("Setting Accept: header to " + acceptMediaTypesString);

		// prepare HTTP request

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", acceptMediaTypesString);

		// execute HTTP request and read response

		DereferenceResult dereferenceResult = null;

		if (log.isDebugEnabled()) log.debug("Driver request for DID URL " + didUrl + " to " + uriString + " with Accept: header " + acceptMediaTypesString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			// execute HTTP request

			HttpEntity httpEntity = httpResponse.getEntity();
			int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
			String httpStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
			ContentType httpContentType = ContentType.get(httpResponse.getEntity());
			Charset httpCharset = (httpContentType != null && httpContentType.getCharset() != null) ? httpContentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;

			if (log.isDebugEnabled()) log.debug("Driver response HTTP status from " + uriString + ": " + httpStatusCode + " " + httpStatusMessage);
			if (log.isDebugEnabled()) log.debug("Driver response HTTP content type from " + uriString + ": " + httpContentType + " / " + httpCharset);

			// read result

			byte[] httpBodyBytes = EntityUtils.toByteArray(httpEntity);
			String httpBodyString = new String(httpBodyBytes, httpCharset);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Driver response HTTP body from " + uriString + ": " + httpBodyString);

			if (httpContentType != null && (DereferenceResult.isMediaType(httpContentType) || HttpBindingClientUtil.isDereferenceResultHttpContent(httpBodyString))) {
				dereferenceResult = HttpBindingClientUtil.fromHttpBodyDereferenceResult(httpBodyString);
			}

			if (httpStatusCode == 404 && dereferenceResult == null) {
				throw new DereferencingException(DereferencingException.ERROR_NOTFOUND, httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (httpStatusCode == 406 && dereferenceResult == null) {
				throw new DereferencingException(DereferencingException.ERROR_CONTENTTYPENOTSUPPORTED, httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (httpStatusCode != 200 && dereferenceResult == null) {
				throw new DereferencingException(DereferencingException.ERROR_INTERNALERROR, "Driver cannot retrieve result for " + didUrl + ": " + httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (dereferenceResult != null && dereferenceResult.isErrorResult()) {
				if (log.isWarnEnabled()) log.warn(dereferenceResult.getError() + " -> " + dereferenceResult.getErrorMessage());
				throw DereferencingException.fromDereferenceResult(dereferenceResult);
			}

			if (dereferenceResult == null) {
				dereferenceResult = HttpBindingClientUtil.fromHttpBodyContent(httpBodyBytes, httpContentType);
			}
		} catch (DereferencingException ex) {

			throw ex;
		} catch (Exception ex) {

			throw new DereferencingException("Driver cannot retrieve dereference result for " + didUrl + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Driver retrieved dereference result for " + didUrl + " (" + uriString + "): " + dereferenceResult);

		// done

		return dereferenceResult;
	}

	@Override
	public Map<String, Object> properties() throws ResolutionException {

		// prepare properties

		Map<String, Object> httpProperties = getHttpProperties();

		Map<String, Object> properties = new HashMap<>();
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

	private Map<String, Object> getHttpProperties() {
		Map<String, Object> httpProperties = new HashMap<>();

		if (this.getResolveUri() != null) httpProperties.put("resolveUri", this.getResolveUri().toString());
		if (this.getPropertiesUri() != null) httpProperties.put("propertiesUri", this.getPropertiesUri().toString());
		return httpProperties;
	}

	public Map<String, Object> remoteProperties() throws ResolutionException {

		if (this.getPropertiesUri() == null) return null;

		// prepare HTTP request

		String uriString = this.getPropertiesUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", Driver.PROPERTIES_MEDIA_TYPE);

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

			properties = (Map<String, Object>) objectMapper.readValue(httpBody, LinkedHashMap.class);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve DRIVER PROPERTIES from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DRIVER PROPERTIES (" + uriString + "): " + properties);

		// done

		return properties;
	}

	@Override
	public List<String> testIdentifiers() {
		return this.getTestIdentifiers();
	}

	@Override
	public Map<String, Object> traits() {
		return this.getTraits();
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

	public Pattern getPattern() {
		return this.pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = Pattern.compile(pattern);
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

	public boolean getSupportsDereference() {
		return this.supportsDereference;
	}

	public void setSupportsDereference(boolean supportsDereference) {
		this.supportsDereference = supportsDereference;
	}

	public String getAcceptHeaderValue() {
		return this.acceptHeaderValue;
	}

	public void setAcceptHeaderValue(String acceptHeaderValue) {
		this.acceptHeaderValue = acceptHeaderValue;
	}

	public List<String> getTestIdentifiers() {
		return this.testIdentifiers;
	}

	public void setTestIdentifiers(List<String> testIdentifiers) {
		this.testIdentifiers = testIdentifiers;
	}

	public Map<String, Object> getTraits() {
		return traits;
	}

	public void setTraits(Map<String, Object> traits) {
		this.traits = traits;
	}
}
