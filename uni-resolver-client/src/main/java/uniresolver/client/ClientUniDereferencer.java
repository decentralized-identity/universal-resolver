package uniresolver.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.UniDereferencer;
import uniresolver.result.DereferenceResult;
import uniresolver.util.HttpBindingClientUtil;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientUniDereferencer implements UniDereferencer {

	private static final Logger log = LoggerFactory.getLogger(ClientUniDereferencer.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final Map<String, String> DEFAULT_HTTP_HEADERS = Collections.emptyMap();
	public static final URI DEFAULT_DEREFERENCE_URI = URI.create("http://localhost:8080/1.0/identifiers");

	private static final int HTTP_CLIENT_TIMEOUT = 60;

	private HttpClient httpClient = buildDefaultHttpClient();
	private URI dereferenceUri = DEFAULT_DEREFERENCE_URI;
	private Map<String, String> httpHeaders = DEFAULT_HTTP_HEADERS;
	private boolean supportsOptions = false;
	private boolean supportsPost = false;
	private String acceptHeaderValueDereference = null;

	public ClientUniDereferencer() {
	}

	private static HttpClient buildDefaultHttpClient() {
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(Timeout.ofSeconds(HTTP_CLIENT_TIMEOUT))
				.setConnectionRequestTimeout(Timeout.ofSeconds(HTTP_CLIENT_TIMEOUT))
				.build();
		return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
	}

	public static ClientUniDereferencer create(URI baseUri) {

		if (! baseUri.toString().endsWith("/")) baseUri = URI.create(baseUri + "/");

		ClientUniDereferencer clientUniDereferencer = new ClientUniDereferencer();
		clientUniDereferencer.setDereferenceUri(URI.create(baseUri + "identifiers"));

		return clientUniDereferencer;
	}

	@Override
	public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions) throws DereferencingException, ResolutionException {

		if (log.isDebugEnabled()) log.debug("dereference(" + didUrlString + ") with options: " + dereferenceOptions);

		if (didUrlString == null) throw new NullPointerException();
		if (dereferenceOptions == null) dereferenceOptions = new HashMap<>();

		// set HTTP URI

		StringBuilder uriString = new StringBuilder(this.getDereferenceUri().toString());
		Map<String, Object> optionsForHttp = HttpBindingClientUtil.optionsForHttp(dereferenceOptions);

		if (! uriString.toString().endsWith("/")) uriString.append("/");
		if (this.getSupportsOptions() && ! this.getSupportsPost() && ! optionsForHttp.isEmpty()) {
			uriString.append(URLEncoder.encode(didUrlString, StandardCharsets.UTF_8));
			uriString.append("?");
			uriString.append(HttpBindingClientUtil.httpQueryStringForOptions(optionsForHttp));
		} else {
			uriString.append(didUrlString);
		}

		// set Accept header

		String accept = (String) dereferenceOptions.get("accept");
		if (this.getAcceptHeaderValueDereference() != null) accept = this.getAcceptHeaderValueDereference();
		if (accept == null) throw new ResolutionException("No 'accept' provided in 'dereferenceOptions' for dereference(), or in driver configuration.");

		List<String> acceptMediaTypes = Arrays.asList(DereferenceResult.MEDIA_TYPE, accept);
		String acceptMediaTypesString = String.join(",", acceptMediaTypes);

		if (log.isDebugEnabled()) log.debug("Setting Accept: header to " + acceptMediaTypesString);

		// prepare HTTP request

		HttpUriRequest httpUriRequest;

		if (this.getSupportsOptions() && this.getSupportsPost() && ! optionsForHttp.isEmpty()) {
			httpUriRequest = new HttpPost(uriString.toString());
			if (! optionsForHttp.isEmpty()) {
				try {
					httpUriRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(optionsForHttp), ContentType.APPLICATION_JSON));
				} catch (JsonProcessingException ex) {
					throw new ResolutionException("Driver cannot serialize options " + optionsForHttp + " for " + uriString + ": " + ex.getMessage(), ex);
				}
			}
		} else {
			httpUriRequest = new HttpGet(URI.create(uriString.toString()));
		}
		httpUriRequest.addHeader("Accept", acceptMediaTypesString);
		if (this.getHttpHeaders() != null) this.getHttpHeaders().forEach(httpUriRequest::addHeader);

		// execute HTTP request and read response

		DereferenceResult dereferenceResult = null;

		if (log.isDebugEnabled()) log.debug("Request for DID " + didUrlString + " to " + uriString + " with Accept: header " + acceptMediaTypesString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpUriRequest)) {

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

			if (httpContentType != null && (HttpBindingClientUtil.isDereferenceResultContentType(httpContentType) || HttpBindingClientUtil.isDereferenceResultHttpContent(httpBodyString))) {
				dereferenceResult = HttpBindingClientUtil.fromHttpBodyDereferenceResult(httpBodyString);
			}

			if (httpCode == 404 && dereferenceResult == null) {
				throw new DereferencingException(DereferencingException.ERROR_NOT_FOUND, httpCode + " " + httpReasonPhrase + " (" + httpBodyString + ")");
			}

			if (httpCode == 406 && dereferenceResult == null) {
				throw new DereferencingException(DereferencingException.ERROR_REPRESENTATION_NOT_SUPPORTED, httpCode + " " + httpReasonPhrase + " (" + httpBodyString + ")");
			}

			if (httpCode != 200 && dereferenceResult == null) {
				throw new DereferencingException(DereferencingException.ERROR_INTERNAL_ERROR, "Cannot retrieve DEREFERENCE result for " + didUrlString + ": " + httpCode + " " + httpReasonPhrase + " (" + httpBodyString + ")");
			}

			if (dereferenceResult != null && dereferenceResult.isErrorResult()) {
				if (log.isWarnEnabled()) log.warn("Received DEREFERENCE result: " + dereferenceResult.getErrorType() + " (" + dereferenceResult.getErrorTitle() + ")" + " -> " + dereferenceResult.getErrorDetail());
				throw DereferencingException.fromDereferenceResult(dereferenceResult);
			}

			if (dereferenceResult == null) {
				dereferenceResult = HttpBindingClientUtil.fromHttpBodyContent(httpContentType, httpBodyBytes);
			}
		} catch (DereferencingException ex) {

			throw ex;
		} catch (Exception ex) {

			throw new DereferencingException("Cannot retrieve DEREFERENCE result for " + didUrlString + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DEREFERENCE result for " + didUrlString + " (" + uriString + "): " + dereferenceResult);

		// done

		return dereferenceResult;
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

	public URI getDereferenceUri() {
		return this.dereferenceUri;
	}

	public void setDereferenceUri(URI dereferenceUri) {
		this.dereferenceUri = dereferenceUri;
	}

	public void setDereferenceUri(String dereferenceUri) {
		this.dereferenceUri = URI.create(dereferenceUri);
	}

	public Map<String, String> getHttpHeaders() {
		return this.httpHeaders;
	}

	public void setHttpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	public boolean getSupportsOptions() {
		return supportsOptions;
	}

	public void setSupportsOptions(boolean supportsOptions) {
		this.supportsOptions = supportsOptions;
	}

	public boolean getSupportsPost() {
		return supportsPost;
	}

	public void setSupportsPost(boolean supportsPost) {
		this.supportsPost = supportsPost;
	}

	public String getAcceptHeaderValueDereference() {
		return acceptHeaderValueDereference;
	}

	public void setAcceptHeaderValueDereference(String acceptHeaderValueDereference) {
		this.acceptHeaderValueDereference = acceptHeaderValueDereference;
	}
}
