package uniresolver.driver.did.stack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uniresolver.ResolutionException;
import uniresolver.did.DIDDocument;
import uniresolver.did.PublicKey;
import uniresolver.did.Service;
import uniresolver.driver.Driver;
import uniresolver.result.ResolutionResult;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DidStackDriver implements Driver {

    private static Logger log = LoggerFactory.getLogger(DidStackDriver.class);

    public static final Pattern DID_STACK_PATTERN = Pattern.compile("^did:stack:v0:([123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{25,34})-([0-9]+)$");

    public static final String[] DIDDOCUMENT_PUBLICKEY_TYPES = new String[] { "Secp256k1VerificationKey2018" };

    public static final String DEFAULT_BLOCKSTACK_CORE_URL = "https://core.blockstack.org";
    public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

    private String blockstackCoreUrl = DEFAULT_BLOCKSTACK_CORE_URL;
    private HttpClient httpClient = DEFAULT_HTTP_CLIENT;

    public DidStackDriver() {

    }

    private static boolean isSubdomain(String candidate) {
        return candidate.split("\\.").length == 3;
    }

    private static boolean isDomain(String candidate) {
        return candidate.split("\\.").length == 2;
    }

    private String getNameFromDID(String address, int index, boolean useSubdomains) throws ResolutionException {

        // find names owned by address 
        String addressUri = this.getBlockstackCoreUrl() + "/v1/addresses/bitcoin/" + address;
        HttpGet httpGet = new HttpGet(addressUri);

        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

           if(httpResponse.getStatusLine().getStatusCode() != 200) {
              throw new ResolutionException("Cannot retrieve names owned by address `" + address + "`: " + httpResponse.getStatusLine());
           }

           // extract payload 
           HttpEntity httpEntity = httpResponse.getEntity();
           String entityString = EntityUtils.toString(httpEntity);
           EntityUtils.consume(httpEntity);

           // it had better be JSON 
           JSONObject jo = new JSONObject(entityString);

           // expect "names" list 
           if(!jo.has("names")) {
              throw new ResolutionException("Did not get a list of `names` from " + addressUri);
           }

           JSONArray namesList = jo.getJSONArray("names");
           ArrayList<String> filteredList = new ArrayList<String>();

           for (int i = 0; i < namesList.length(); i++) {
               String currentName = namesList.getString(i);
               if (useSubdomains) {
                   if (DidStackDriver.isSubdomain(currentName))
                       filteredList.add(currentName);
               } else {
                   if (DidStackDriver.isDomain(currentName))
                       filteredList.add(currentName);
               }
           }

           if(filteredList.size() <= index) {
               String type = useSubdomains ? "subdomain" : "domain";
               throw new ResolutionException(
                   "DID " + type + " index `" + index + "` is out-of-bounds for owner `" +
                   address + "`");
           }

           return filteredList.get(index);
        } catch (IOException ex) {
            throw new ResolutionException("Cannot lookup address-to-name list for `" + address + "-" + index + "` from " + addressUri);
        } catch (JSONException jex) {
            throw new ResolutionException("Cannot parse JSON response from `" + addressUri + "`: " + jex.getMessage(), jex);
        }
    }

    public static String encodeChecked(int version, byte[] payload) {
        if (version < 0 || version > 255)
            throw new IllegalArgumentException("Version not in range.");

        // A stringified buffer is:
        // 1 byte version + data bytes + 4 bytes check code (a truncated hash)
        byte[] addressBytes = new byte[1 + payload.length + 4];
        addressBytes[0] = (byte) version;
        System.arraycopy(payload, 0, addressBytes, 1, payload.length);
        byte[] checksum = Sha256Hash.hashTwice(addressBytes, 0, payload.length + 1);
        System.arraycopy(checksum, 0, addressBytes, payload.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    @Override
    public ResolutionResult resolve(String identifier) throws ResolutionException {
        // match 
        Matcher matcher = DID_STACK_PATTERN.matcher(identifier);
        if(!matcher.matches()) {
           return null;
        }

        String didAddress = matcher.group(1);
        String indexStr = matcher.group(2);
        int nameIndex = Integer.parseInt(indexStr);

        byte[] decodedVersionHash = Base58.decodeChecked(didAddress);
        byte version = decodedVersionHash[0];
        byte[] decodedHash = Arrays.copyOfRange(decodedVersionHash, 1, decodedVersionHash.length);
        byte reencodeVersion;
        if (version == 50 || version == 5) {
            reencodeVersion = 5;
        } else if (version == 63 || version == 0) {
            reencodeVersion = 0;
        } else {
            throw new ResolutionException("Invalid address version in " +
                                          didAddress + ". Must be one of [0, 5, 50, 63]");
        }
        boolean useSubdomains = (version == 63 || version == 50);

        String reencodedAddress = DidStackDriver.encodeChecked(reencodeVersion, decodedHash);

        log.debug("Re-encoding " + didAddress + " to " + reencodedAddress);

        String name = this.getNameFromDID(reencodedAddress, nameIndex, useSubdomains);

        // fetch data from Core node
        String nameUri = this.getBlockstackCoreUrl() + "/v1/users/" + name;
        HttpGet httpGet = new HttpGet(nameUri);

        // find the public key!
        String publicKeyHex = null;

        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

           if(httpResponse.getStatusLine().getStatusCode() != 200) {
              throw new ResolutionException("Cannot retrieve DDO for `" + name + "` from `" + nameUri + ": " + httpResponse.getStatusLine());
           }

           // extract payload
           HttpEntity httpEntity = httpResponse.getEntity();
           String entityString = EntityUtils.toString(httpEntity);
           EntityUtils.consume(httpEntity);

           // it had better be JSON 
           JSONObject jo = new JSONObject(entityString);
           
           // it had better start with the name
           if(jo.isNull(name)) {
              // malformed
              throw new ResolutionException("Invalid profile returned: name `" + name + "` not mapped to profile info");
           }

           JSONObject nameInfo = jo.getJSONObject(name);

           if(nameInfo.isNull("public_key")) {
              // no public key defined in the profile
              // (usually means the user has a legacy profile)
              throw new ResolutionException("Cannot retrieve public key for `" + name + "`: profile is in legacy format");
           }
           else {
               // extract public_key
               publicKeyHex = nameInfo.getString("public_key");
           }
            
        } catch (IOException ex) {
            throw new ResolutionException("Cannot retrieve DDO info for `" + name + "` from `" + nameUri + "`: " + ex.getMessage(), ex);
        } catch (JSONException jex) {
            throw new ResolutionException("Cannot parse JSON response from `" + nameUri + "`: " + jex.getMessage(), jex);
        }

        // DDO id
        String id = identifier;

        // DDO publicKeys
        PublicKey publicKey = PublicKey.build(identifier, DIDDOCUMENT_PUBLICKEY_TYPES, null, null, publicKeyHex, null);

        List<PublicKey> publicKeys = Collections.singletonList(publicKey);

        // DDO services
        List<Service> services = new ArrayList<Service>();
        services.add(Service.build("blockstack", null, DEFAULT_BLOCKSTACK_CORE_URL));

        // create DDO
        DIDDocument didDocument = DIDDocument.build(id, publicKeys, null, null, services);

        // done
        return ResolutionResult.build(didDocument);
    }

    public Map<String, Object> properties() {

    	Map<String, Object> properties = new HashMap<String, Object> ();
    	properties.put("blockstackCoreUrl", this.getBlockstackCoreUrl());
    	
    	return properties;
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

    public String getBlockstackCoreUrl() { 

       return this.blockstackCoreUrl;
    }

    public void setBlockstackCoreUrl(String stackUrl) {
        this.blockstackCoreUrl = stackUrl;
    }
}
