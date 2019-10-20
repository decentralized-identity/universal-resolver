package uniresolver.driver.did.erc725.ethereumconnection;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import uniresolver.driver.did.erc725.contract.ERC725Contract;

public class JsonRPCEthereumConnection extends AbstractEthereumConnection implements EthereumConnection {

	private static Logger log = LoggerFactory.getLogger(JsonRPCEthereumConnection.class);

	private Web3j web3j;
	private Credentials credentials;

	public JsonRPCEthereumConnection(Web3j web4j) {

		this.web3j = web4j;
		this.credentials = createCredentials();
	}

	public JsonRPCEthereumConnection(String rpcUrl, String username, String password) {

		this(createWeb3j(rpcUrl, username, password));
	}

	public JsonRPCEthereumConnection() {

		this((Web3j) null);
	}

	@Override
	public Map<BigInteger, Map<String, byte[]>> getKeysByType(String address) throws IOException {

		// load ERC725 contract

		ERC725Contract erc725Contract = ERC725Contract.load("0x" + address, this.getWeb3j(), this.getCredentials(), null, null);

		// get keys

		Map<String, byte[]> managementPublicKeys;
		Map<String, byte[]> actionPublicKeys;
		Map<String, byte[]> claimPublicKeys;
		Map<String, byte[]> encryptionPublicKeys;

		try {

			List<String> managementAddresses = addressesToStrings((List<Address>) erc725Contract.getKeysByType(EthereumConnection.KEY_TYPE_MANAGEMENT).send());
			List<String> actionAddresses = addressesToStrings((List<Address>) erc725Contract.getKeysByType(EthereumConnection.KEY_TYPE_ACTION).send());
			List<String> claimAddresses = addressesToStrings((List<Address>) erc725Contract.getKeysByType(EthereumConnection.KEY_TYPE_CLAIM).send());
			List<String> encryptionAddresses = addressesToStrings((List<Address>) erc725Contract.getKeysByType(EthereumConnection.KEY_TYPE_ENCRYPTION).send());

			managementPublicKeys = this.getPublicKeysByAddresses(managementAddresses);
			actionPublicKeys = this.getPublicKeysByAddresses(actionAddresses);
			claimPublicKeys = this.getPublicKeysByAddresses(claimAddresses);
			encryptionPublicKeys = this.getPublicKeysByAddresses(encryptionAddresses);
		} catch (Exception ex) {

			throw new IOException("Cannot look up keys: " + ex.getMessage(), ex);
		}

		Map<BigInteger, Map<String, byte[]>> keysByType = new HashMap<BigInteger, Map<String, byte[]>> ();

		keysByType.put(KEY_TYPE_MANAGEMENT, managementPublicKeys);
		keysByType.put(KEY_TYPE_ACTION, actionPublicKeys);
		keysByType.put(KEY_TYPE_CLAIM, claimPublicKeys);
		keysByType.put(KEY_TYPE_ENCRYPTION, encryptionPublicKeys);

		// done

		return keysByType;
	}

	@Override
	public Map<String, String> getTransactionHashesByAddresses(List<String> addresses) throws IOException {

		// find transaction hashes

		EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, addresses);
		List<LogResult> logResults = this.getWeb3j().ethGetLogs(filter).send().getLogs();

		Map<String, String> transactionHashesByAddresses = new HashMap<String, String> ();

		for (LogResult logResult : logResults) {

			String address = ((EthLog.LogObject) logResult).getAddress();
			String transactionHash = ((EthLog.LogObject) logResult).getTransactionHash();

			if (log.isDebugEnabled()) log.debug("Found transaction " + transactionHash + " for address " + address);
			if (! transactionHashesByAddresses.containsKey(address)) transactionHashesByAddresses.put(address, transactionHash);
		}

		// done

		return transactionHashesByAddresses;
	}

	@Override
	public Map<String, byte[]> getPublicKeysByAddresses(Map<String, String> transactionHashesByAddress) throws IOException, DecoderException, SignatureException {

		// find keys

		Map<String, byte[]> keysByAddress = new HashMap<String, byte[]> ();

		for (Entry<String, String> transactionHashByAddress : transactionHashesByAddress.entrySet()) {

			String address = transactionHashByAddress.getKey();
			String transactionHash = transactionHashByAddress.getValue();

			Transaction transaction = this.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction().get();

			if (transaction == null) {

				if (log.isWarnEnabled()) log.warn("Found no transaction " + transactionHash);
				continue;
			}

			SignatureData signatureData = signatureDataFromTransaction(transaction);
			RawTransaction rawTransaction = rawTransactionFromTransaction(transaction);
			String encodedRawTransaction = Hex.encodeHexString(TransactionEncoder.encode(rawTransaction));

			byte[] key = Sign.signedMessageToKey(Hex.decodeHex(encodedRawTransaction.toCharArray()), signatureData).toByteArray();
			if (log.isDebugEnabled()) log.debug("Found key " + Hex.encodeHexString(key) + " for transaction " + transactionHash);

			keysByAddress.put(address, key);
		}

		// done

		return keysByAddress;
	}

	/*
	 * Helper methods
	 */

	private static Web3j createWeb3j(String url, String username, String password) {

		OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

		if (username != null && password != null) {

			httpClientBuilder.authenticator(new Authenticator() {

				public okhttp3.Request authenticate(Route route, okhttp3.Response response) throws IOException {
					String credential = okhttp3.Credentials.basic(username, password);
					return response.request().newBuilder().header("Authorization", credential).build();
				}
			});
		}

		if (log.isDebugEnabled()) {

			HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(log::debug);
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			httpClientBuilder.addInterceptor(httpLoggingInterceptor);
		}

		OkHttpClient httpClient = httpClientBuilder.build();
		HttpService httpService = new HttpService(url, httpClient, false);

		return Web3j.build(httpService);
	}

	private static Credentials createCredentials() {

		try {

			return Credentials.create(Keys.createEcKeyPair());
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static List<String> addressesToStrings(List<Address> addresses) {

		if (addresses == null) return null;

		List<String> strings = new ArrayList<String> ();
		for (Address address : addresses) strings.add(address.getValue());

		return strings;
	}

	private static SignatureData signatureDataFromTransaction(Transaction transaction) throws DecoderException {

		return new SignatureData((byte) transaction.getV(), decodeHex(transaction.getR()), decodeHex(transaction.getS()));
	}

	private static RawTransaction rawTransactionFromTransaction(Transaction transaction) {

		return RawTransaction.createTransaction(transaction.getNonce(), transaction.getGasPrice(), transaction.getGas(), transaction.getTo(), transaction.getValue(), transaction.getInput());
	}

	private static byte[] decodeHex(String hexString) throws DecoderException {

		hexString = hexString.substring(2);
		if (hexString.length() % 2 != 0) hexString = '0' + hexString;
		return Hex.decodeHex(hexString.toCharArray());
	}

	/*
	 * Getters and setters
	 */

	public Web3j getWeb3j() {

		return this.web3j;
	}

	public void setWeb3j(Web3j web3j) {

		this.web3j = web3j;
	}

	public void setRpcUrl(String rpcUrl) {

		this.setWeb3j(Web3j.build(new HttpService(rpcUrl)));
	}

	public Credentials getCredentials() {

		return this.credentials;
	}

	public void setCredentials(Credentials credentials) {

		this.credentials = credentials;
	}
}

