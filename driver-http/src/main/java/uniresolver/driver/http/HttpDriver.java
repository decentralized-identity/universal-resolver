package uniresolver.driver.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
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
import uniresolver.driver.Driver;
import uniresolver.result.ResolveRepresentationResult;
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

	private HttpClient httpClient = HttpClients.createDefault();
	private URI resolveUri = null;
	private URI propertiesUri = null;
	private Pattern pattern = null;
	private List<String> testIdentifiers = Collections.emptyList();

	public HttpDriver() {

	}

	@Override
	public ResolveRepresentationResult resolveRepresentation(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {

		if (this.getPattern() == null || this.getResolveUri() == null) return null;

		// match string

		String matchedString = null;

		if (this.getPattern() != null) {

			Matcher matcher = this.getPattern().matcher(did.getDidString());

			if (! matcher.matches()) {
				if (log.isDebugEnabled()) log.debug("Skipping identifier " + did + " - does not match pattern " + this.getPattern());
				return null;
			} else {
				if (log.isDebugEnabled()) log.debug("Identifier " + did + " matches pattern " + this.getPattern() + " with " + matcher.groupCount() + " groups");
			}

			if (matcher.groupCount() > 0) {

				matchedString = "";
				for (int i=1; i<=matcher.groupCount(); i++) if (matcher.group(i) != null) matchedString += matcher.group(i);
			}
		}

		if (matchedString == null) matchedString = did.getDidString();
		if (log.isDebugEnabled()) log.debug("Matched string: " + matchedString);

		// set HTTP URI

		String uriString = this.getResolveUri().toString();

		if (uriString.contains("$1")) {

			uriString = uriString.replace("$1", matchedString);
		} else if (uriString.contains("$2")) {

			uriString = uriString.replace("$2", URLEncoder.encode(matchedString, StandardCharsets.UTF_8));
		} else {

			if (! uriString.endsWith("/")) uriString += "/";
			uriString += matchedString;
		}

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
				throw new ResolutionException(ResolutionException.ERROR_INTERNALERROR, "Driver cannot retrieve result for " + did + ": " + httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
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

			throw new ResolutionException("Driver cannot retrieve resolve result for " + did + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isInfoEnabled()) log.info("Driver retrieved resolve result for " + did + " (" + uriString + "): " + resolveRepresentationResult);

		// done

		return resolveRepresentationResult;
	}

	@Override
	public Map<String, Object> properties() throws ResolutionException {

		// prepare properties

		Map<String, Object> httpProperties = new HashMap<String, Object> ();

		if (this.getResolveUri() != null) httpProperties.put("resolveUri", this.getResolveUri().toString());
		if (this.getPropertiesUri() != null) httpProperties.put("propertiesUri", this.getPropertiesUri().toString());
		if (this.getPattern() != null) httpProperties.put("pattern", this.getPattern().toString());
		if (this.getTestIdentifiers() != null) httpProperties.put("testIdentifiers", this.getTestIdentifiers());

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

			properties = (Map<String, Object>) objectMapper.readValue(httpBody, LinkedHashMap.class);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve DRIVER PROPERTIES from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DRIVER PROPERTIES (" + uriString + "): " + properties);

		// done

		return properties;
	}

	@Override
	public List<String> testIdentifiers() throws ResolutionException {

		return this.getTestIdentifiers();
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

	public List<String> getTestIdentifiers() {
		return this.testIdentifiers;
	}

	public void setTestIdentifiers(List<String> testIdentifiers) {
		this.testIdentifiers = testIdentifiers;
	}
}
