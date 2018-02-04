package uniresolver.driver.http;

import java.io.IOException;
import java.net.URI;
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

import uniresolver.ResolutionException;
import uniresolver.did.DIDDocument;
import uniresolver.driver.Driver;
import uniresolver.result.ResolutionResult;

public class HttpDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(HttpDriver.class);

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_DRIVER_URI = URI.create("http://localhost:8080/1.0/dids/");
	public static final Pattern DEFAULT_PATTERN = null;
	public static final boolean DEFAULT_RAW_DID_IDENTIFIER = false;
	public static final boolean DEFAULT_RAW_DID_DOCUMENT = false;

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI driverUri = DEFAULT_DRIVER_URI;
	private Pattern pattern = DEFAULT_PATTERN;
	private boolean rawDidIdentifier = DEFAULT_RAW_DID_IDENTIFIER;
	private boolean rawDidDocument = DEFAULT_RAW_DID_DOCUMENT;

	public HttpDriver() {

	}

	@Override
	public ResolutionResult resolve(String identifier) throws ResolutionException {

		// check pattern

		if (this.getPattern() != null) {

			Matcher matcher = this.getPattern().matcher(identifier);

			if (! matcher.matches()) {

				if (log.isDebugEnabled()) log.debug("Skipping identifier " + identifier + " - does not match pattern " + this.getPattern());
				return null;
			}
		}

		// prepare HTTP request

		if (this.isRawDidIdentifier()) {

			identifier = identifier.substring(identifier.indexOf(":") + 1);
			identifier = identifier.substring(identifier.indexOf(":") + 1);
		}

		String uriString = this.getDriverUri().toString();
		if (! uriString.endsWith("/")) uriString += "/";
		uriString += identifier;

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", "application/ld+json");

		// execute HTTP request

		ResolutionResult resolutionResult;

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

	/*
	 * Getters and setters
	 */

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}

	public URI getDriverUri() {

		return this.driverUri;
	}

	public void setDriverUri(URI driverUri) {

		this.driverUri = driverUri;
	}

	public void setDriverUri(String driverUri) {

		this.driverUri = URI.create(driverUri);
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

	public boolean isRawDidIdentifier() {

		return this.rawDidIdentifier;
	}

	public void setRawDidIdentifier(boolean rawDidIdentifier) {

		this.rawDidIdentifier = rawDidIdentifier;
	}

	public boolean isRawDidDocument() {

		return this.rawDidDocument;
	}

	public void setRawDidDocument(boolean rawDidDocument) {

		this.rawDidDocument = rawDidDocument;
	}
}
