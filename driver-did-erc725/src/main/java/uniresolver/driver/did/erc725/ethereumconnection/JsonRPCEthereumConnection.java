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

import uniresolver.driver.did.erc725.ERC725Contract;

public class JsonRPCEthereumConnection extends AbstractEthereumConnection implements EthereumConnection {

	private static Logger log = LoggerFactory.getLogger(JsonRPCEthereumConnection.class);

	private static final JsonRPCEthereumConnection instance = new JsonRPCEthereumConnection();

	private Web3j ethereumWeb3jMainnet;
	private Web3j ethereumWeb3jRopsten;
	private Web3j ethereumWeb3jRinkeby;
	private Web3j ethereumWeb3jKovan;
	private Credentials credentials;

	public JsonRPCEthereumConnection(Web3j ethereumWeb3jMainnet, Web3j ethereumWeb3jRopsten, Web3j ethereumWeb3jRinkeby, Web3j ethereumWeb3jKovan) {

		this.ethereumWeb3jMainnet = ethereumWeb3jMainnet;
		this.ethereumWeb3jRopsten = ethereumWeb3jRopsten;
		this.ethereumWeb3jRinkeby = ethereumWeb3jRinkeby;
		this.ethereumWeb3jKovan = ethereumWeb3jKovan;
		this.credentials = createCredentials();
	}

	public JsonRPCEthereumConnection(String ethereumRpcUrlMainnet, String ethereumRpcUrlRopsten, String ethereumRpcUrlRinkeby, String ethereumRpcUrlKovan) {

		this(Web3j.build(new HttpService(ethereumRpcUrlMainnet)), Web3j.build(new HttpService(ethereumRpcUrlRopsten)), Web3j.build(new HttpService(ethereumRpcUrlRinkeby)), Web3j.build(new HttpService(ethereumRpcUrlKovan)));
	}

	public JsonRPCEthereumConnection() {

		this((Web3j) null, (Web3j) null, (Web3j) null, (Web3j) null);
	}

	public static JsonRPCEthereumConnection get() {

		return instance;
	}

	private static Credentials createCredentials() {

		try {

			return Credentials.create(Keys.createEcKeyPair());
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public Map<BigInteger, Map<String, byte[]>> getKeysByType(String network, String address) throws IOException {

		// determine network

		Web3j web3j = this.getWeb3j(network);
		if (web3j == null) throw new IOException("No connection for network '" + network + "'");

		// load ERC725 contract

		ERC725Contract erc725Contract = ERC725Contract.load("0x" + address, web3j, this.getCredentials(), null, null);

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

			managementPublicKeys = this.getPublicKeysByAddresses(network, managementAddresses);
			actionPublicKeys = this.getPublicKeysByAddresses(network, actionAddresses);
			claimPublicKeys = this.getPublicKeysByAddresses(network, claimAddresses);
			encryptionPublicKeys = this.getPublicKeysByAddresses(network, encryptionAddresses);
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
	public Map<String, String> getTransactionHashesByAddresses(String network, List<String> addresses) throws IOException {

		// determine network

		Web3j web3j = this.getWeb3j(network);
		if (web3j == null) throw new IOException("No connection for network '" + network + "'");

		// find transaction hashes

		EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, addresses);
		List<LogResult> logResults = web3j.ethGetLogs(filter).send().getLogs();

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
	public Map<String, byte[]> getPublicKeysByAddresses(String network, Map<String, String> transactionHashesByAddress) throws IOException, DecoderException, SignatureException {

		// determine network

		Web3j web3j = this.getWeb3j(network);
		if (web3j == null) throw new IOException("No connection for network '" + network + "'");

		// find keys

		Map<String, byte[]> keysByAddress = new HashMap<String, byte[]> ();

		for (Entry<String, String> transactionHashByAddress : transactionHashesByAddress.entrySet()) {

			String address = transactionHashByAddress.getKey();
			String transactionHash = transactionHashByAddress.getValue();

			Transaction transaction = web3j.ethGetTransactionByHash(transactionHash).send().getTransaction().get();

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

	private Web3j getWeb3j(String network) {

		Web3j web3j = null;

		if (network == null || NETWORK_MAINNET.equals(network)) web3j = this.ethereumWeb3jMainnet;
		else if (NETWORK_ROPSTEN.equals(network)) web3j = this.ethereumWeb3jRopsten;
		else if (NETWORK_RINKEBY.equals(network)) web3j = this.ethereumWeb3jRinkeby;
		else if (NETWORK_KOVAN.equals(network)) web3j = this.ethereumWeb3jKovan;

		return web3j;
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

	public Web3j getEthereumWeb3jMainnet() {

		return this.ethereumWeb3jMainnet;
	}

	public void setEthereumWeb3jMainnet(Web3j ethereumWeb3jMainnet) {

		this.ethereumWeb3jMainnet = ethereumWeb3jMainnet;
	}

	public void setRpcUrlMainnet(String rpcUrlMainnet) {

		this.setEthereumWeb3jMainnet(Web3j.build(new HttpService(rpcUrlMainnet)));
	}

	public Web3j getEthereumWeb3jRopsten() {

		return this.ethereumWeb3jRopsten;
	}

	public void setEthereumWeb3jRopsten(Web3j ethereumWeb3jRopsten) {

		this.ethereumWeb3jRopsten = ethereumWeb3jRopsten;
	}

	public void setRpcUrlRopsten(String rpcUrlRopsten) {

		this.setEthereumWeb3jRopsten(Web3j.build(new HttpService(rpcUrlRopsten)));
	}

	public Web3j getEthereumWeb3jRinkeby() {

		return this.ethereumWeb3jRinkeby;
	}

	public void setEthereumWeb3jRinkeby(Web3j ethereumWeb3jRinkeby) {

		this.ethereumWeb3jRinkeby = ethereumWeb3jRinkeby;
	}

	public void setRpcUrlRinkeby(String rpcUrlRinkeby) {

		this.setEthereumWeb3jRinkeby(Web3j.build(new HttpService(rpcUrlRinkeby)));
	}

	public Web3j getEthereumWeb3jKovan() {

		return this.ethereumWeb3jKovan;
	}

	public void setEthereumWeb3jKovan(Web3j ethereumWeb3jKovan) {

		this.ethereumWeb3jKovan = ethereumWeb3jKovan;
	}

	public void setRpcUrlKovan(String ethereumRpcUrlKovan) {

		this.setEthereumWeb3jKovan(Web3j.build(new HttpService(ethereumRpcUrlKovan)));
	}

	public Credentials getCredentials() {

		return this.credentials;
	}

	public void setCredentials(Credentials credentials) {

		this.credentials = credentials;
	}
}

