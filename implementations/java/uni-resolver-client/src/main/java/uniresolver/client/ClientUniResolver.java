package uniresolver.client;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

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
import uniresolver.result.ResolutionResult;

public class ClientUniResolver implements UniResolver {

	private static Logger log = LoggerFactory.getLogger(ClientUniResolver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_RESOLVER_URI = URI.create("http://localhost:8080/1.0/identifiers/");

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI resolverUri = DEFAULT_RESOLVER_URI;

	public ClientUniResolver() {

	}

	@Override
	public ResolutionResult resolve(String identifier) throws ResolutionException {

		// prepare HTTP request

		String uriString = this.getResolverUri().toString();
		if (! uriString.endsWith("/")) uriString += "/";
		uriString += "identifiers/";
		uriString += identifier;

		HttpGet httpGet = new HttpGet(URI.create(uriString));

		// execute HTTP request

		ResolutionResult resolutionResult;

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			if (httpResponse.getStatusLine().getStatusCode() == 404) return null;
			if (httpResponse.getStatusLine().getStatusCode() > 200) throw new ResolutionException("Cannot retrieve RESULTION RESULT for " + identifier + " from " + uriString + ": " + httpResponse.getStatusLine());

			HttpEntity httpEntity = httpResponse.getEntity();

			resolutionResult = ResolutionResult.fromJson(EntityUtils.toString(httpEntity));
			EntityUtils.consume(httpEntity);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve RESULTION RESULT for " + identifier + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved RESULTION RESULT for " + identifier + " (" + uriString + "): " + resolutionResult);

		// done

		return resolutionResult;
	}

	@Override
	public Collection<String> getDriverIds() throws ResolutionException {

		// prepare HTTP request

		String uriString = this.getResolverUri().toString();
		if (! uriString.endsWith("/")) uriString += "/";
		uriString += "drivers/";

		HttpGet httpGet = new HttpGet(URI.create(uriString));

		// execute HTTP request

		List<String> driverIds;

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			if (httpResponse.getStatusLine().getStatusCode() == 404) return null;
			if (httpResponse.getStatusLine().getStatusCode() > 200) throw new ResolutionException("Cannot retrieve DRIVER IDS from " + uriString + ": " + httpResponse.getStatusLine());

			HttpEntity httpEntity = httpResponse.getEntity();

			driverIds = ((List<String>) objectMapper.readValue(httpEntity.getContent(), List.class));
			EntityUtils.consume(httpEntity);
		} catch (IOException ex) {

			throw new ResolutionException("Cannot retrieve DRIVER IDS from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DRIVER IDS (" + uriString + "): " + driverIds);

		// done

		return driverIds;
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

	public URI getResolverUri() {

		return this.resolverUri;
	}

	public void setResolverUri(URI resolverUri) {

		this.resolverUri = resolverUri;
	}

	public void setResolverUri(String resolverUri) {

		this.resolverUri = URI.create(resolverUri);
	}
}
