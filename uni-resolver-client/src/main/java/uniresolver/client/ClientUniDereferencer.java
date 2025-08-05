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
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.UniDereferencer;
import uniresolver.result.DereferenceResult;
import uniresolver.util.HttpBindingClientUtil;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientUniDereferencer implements UniDereferencer {

	private static final Logger log = LoggerFactory.getLogger(ClientUniDereferencer.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_DEREFERENCE_URI = URI.create("http://localhost:8080/1.0/identifiers");

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI dereferenceUri = DEFAULT_DEREFERENCE_URI;

	public ClientUniDereferencer() {

	}

	public static ClientUniDereferencer create(URI baseUri) {

		if (! baseUri.toString().endsWith("/")) baseUri = URI.create(baseUri + "/");

		ClientUniDereferencer clientUniDereferencer = new ClientUniDereferencer();
		clientUniDereferencer.setDereferenceUri(URI.create(baseUri + "identifiers"));

		return clientUniDereferencer;
	}

	@Override
	public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions) throws DereferencingException, ResolutionException {

		if (log.isDebugEnabled()) log.debug("dereference(" + didUrlString + ")  with options: " + dereferenceOptions);

		if (didUrlString == null) throw new NullPointerException();
		if (dereferenceOptions == null) dereferenceOptions = new HashMap<>();

		// set HTTP URI

		String uriString = this.getDereferenceUri().toString();

		if (! uriString.endsWith("/")) uriString += "/";
		uriString += didUrlString;

		// set Accept header

		String accept = (String) dereferenceOptions.get("accept");
		if (accept == null) throw new DereferencingException("No 'accept' provided in 'dereferenceOptions' for dereference().");

		List<String> acceptMediaTypes = Arrays.asList(DereferenceResult.MEDIA_TYPE, accept);
		String acceptMediaTypesString = String.join(",", acceptMediaTypes);

		if (log.isDebugEnabled()) log.debug("Setting Accept: header to " + acceptMediaTypesString);

		// prepare HTTP request

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", acceptMediaTypesString);

		// execute HTTP request and read response

		DereferenceResult dereferenceResult = null;

		if (log.isDebugEnabled()) log.debug("Request for DID " + didUrlString + " to " + uriString + " with Accept: header " + acceptMediaTypesString);

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

			if (httpContentType != null && (HttpBindingClientUtil.isDereferenceResultContentType(httpContentType) || HttpBindingClientUtil.isDereferenceResultHttpContent(httpBodyString))) {
				dereferenceResult = HttpBindingClientUtil.fromHttpBodyDereferenceResult(httpBodyString);
			}

			if (httpStatusCode == 404 && dereferenceResult == null) {
				throw new DereferencingException(DereferencingException.ERROR_NOT_FOUND, httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (httpStatusCode == 406 && dereferenceResult == null) {
				throw new DereferencingException(DereferencingException.ERROR_REPRESENTATION_NOT_SUPPORTED, httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (httpStatusCode != 200 && dereferenceResult == null) {
				throw new DereferencingException(DereferencingException.ERROR_INTERNAL_ERROR, "Cannot retrieve DEREFERENCE result for " + didUrlString + ": " + httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
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
}
