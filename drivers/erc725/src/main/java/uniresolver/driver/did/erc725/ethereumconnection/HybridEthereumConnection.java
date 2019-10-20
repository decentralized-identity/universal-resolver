package uniresolver.driver.did.erc725.ethereumconnection;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HybridEthereumConnection extends JsonRPCEthereumConnection implements EthereumConnection {

	private static Logger log = LoggerFactory.getLogger(HybridEthereumConnection.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final HybridEthereumConnection instance = new HybridEthereumConnection();

	private String etherscanApi;
	private HttpClient httpClient;

	public HybridEthereumConnection(Web3j web3j, String etherscanApi) {

		super(web3j);

		this.etherscanApi = etherscanApi;
		this.httpClient = HttpClients.createDefault();
	}

	public HybridEthereumConnection(Web3j web3j, Web3j ethereumWeb3jRopsten, Web3j ethereumWeb3jRinkeby, Web3j ethereumWeb3jKovan) {

		this(web3j, null);
	}

	public HybridEthereumConnection() {

		this(null, null);
	}

	public static HybridEthereumConnection get() {

		return instance;
	}

	@Override
	public Map<String, String> getTransactionHashesByAddresses(List<String> addresses) throws IOException {

		// find transaction hashes

		Map<String, String> transactionHashesByAddresses = new HashMap<String, String> ();

		for (String address : addresses) {

			// prepare HTTP request

			String uriString = this.getEtherscanApi();
			uriString += "?module=account&action=txlist&address=";
			uriString += address;

			HttpGet httpGet = new HttpGet(URI.create(uriString));

			// execute HTTP request

			Map<String, Object> transactions;

			if (log.isDebugEnabled()) log.debug("Request for address " + address + " to: " + uriString);

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

					if (log.isWarnEnabled()) log.warn("Cannot retrieve transactions for " + address + " from " + uriString + ": " + httpBody);
					throw new IOException(httpBody);
				}

				transactions = (Map<String, Object>) objectMapper.readValue(httpBody, Map.class);
			} catch (IOException ex) {

				throw new IOException("Cannot retrieve transactions for " + address + " from " + uriString + ": " + ex.getMessage(), ex);
			}

			if (log.isDebugEnabled()) log.debug("Retrieved transactions for " + address + " (" + uriString + "): " + transactions);

			// find transaction hashes

			List<Map<String, Object>> result = transactions != null ? (List<Map<String, Object>>) transactions.get("result") : null;

			for (Map<String, Object> transaction : result) {

				String from = (String) transaction.get("from");
				String transactionHash = (String) transaction.get("hash");

				if (! address.equalsIgnoreCase(from)) {

					if (log.isDebugEnabled()) log.debug("Ignoring transaction " + transactionHash + " from " + from);
					continue;
				}

				if (log.isDebugEnabled()) log.debug("Found transaction " + transactionHash + " for address " + address);
				if (! transactionHashesByAddresses.containsKey(address)) transactionHashesByAddresses.put(address, transactionHash);
			}
		}

		return transactionHashesByAddresses;
	}

	/*
	 * Getters and setters
	 */

	public String getEtherscanApi() {

		return this.etherscanApi;
	}

	public void setEtherscanApi(String etherscanApi) {

		this.etherscanApi = etherscanApi;
	}

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}
}

