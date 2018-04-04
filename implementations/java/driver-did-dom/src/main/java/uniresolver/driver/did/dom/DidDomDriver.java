package uniresolver.driver.did.dom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import uniresolver.did.Authentication;
import uniresolver.did.PublicKey;
import uniresolver.did.Service;
import uniresolver.driver.Driver;
import uniresolver.result.ResolutionResult;

public class DidDomDriver implements Driver {
    
    public static final Pattern DID_DOM_PATTERN = Pattern.compile("^did:dom:[a-km-zA-HJ-NP-Z1-9]{30,30}$");
    
    private final String DID_DOM_RESOLVER_URL = "https://did-resolver.dominode.com";
    
    public static final HashMap<String, String> DIDDOCUMENT_PUBLICKEY_TYPES = new HashMap<String, String>();
    public static final String[] DIDDOCUMENT_AUTHENTICATION_TYPES = new String[] { "Ed25519SignatureAuthentication2018", "RsaSignatureAuthentication2018", "Secp256k1SignatureAuthentication2018"};

    public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

    private HttpClient httpClient = DEFAULT_HTTP_CLIENT;

    public DidDomDriver() {
        DIDDOCUMENT_PUBLICKEY_TYPES.put("Ed25519VerificationKey2018", "publicKeyBase58");
        DIDDOCUMENT_PUBLICKEY_TYPES.put("RsaVerificationKey2018", "publicKeyPem");
        DIDDOCUMENT_PUBLICKEY_TYPES.put("Secp256k1VerificationKey2018", "publicKeyHex");
    }
    
    @Override
    public ResolutionResult resolve(String identifier) throws ResolutionException {

        // match 
        Matcher matcher = DID_DOM_PATTERN.matcher(identifier);
        if(!matcher.matches()) {
           return null;
        }
        
        // DDO id
        String id = identifier;        

        // fetch DDO 
        String nameUri = this.getDomUrl()+ "/ddo/" + identifier;
        HttpGet httpGet = new HttpGet(nameUri);

        List<PublicKey> publicKeys = new ArrayList<PublicKey> ();
        List<Authentication> authentications = new ArrayList<Authentication> ();
        List<Service> services = new ArrayList<Service> ();
        
        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

            if(httpResponse.getStatusLine().getStatusCode() != 200) {
               throw new ResolutionException("Cannot retrieve DDO for `" + identifier + "` from `" + nameUri + ": " + httpResponse.getStatusLine());
            }

            // extract payload
            HttpEntity httpEntity = httpResponse.getEntity();
            String entityString = EntityUtils.toString(httpEntity);
            EntityUtils.consume(httpEntity);

            // got to be JSON 
            JSONObject jo = new JSONObject(entityString);

            if(jo.isNull("publicKey")) {
               // no public keys defined
               throw new ResolutionException("Cannot retrieve public key(s) for `" + identifier + "`");
            }
            else {
                 // extract public_key(s)        
                JSONArray propPublicKey = jo.getJSONArray("publicKey");
                
                int keyNum = 0;
                
                for (Object itemPubKey: propPublicKey) {
                    
                    JSONObject objPubKey = (JSONObject) itemPubKey;
                    
                    String lblPublicKey = "";
                    
                    String typePublicKey[] = new String[1];
                    
                    for (Map.Entry<String, String> entry : DIDDOCUMENT_PUBLICKEY_TYPES.entrySet()) {
                        if (objPubKey.getString("type").equals(entry.getKey())) {
                            typePublicKey[0] = entry.getKey();
                            lblPublicKey = entry.getValue();
                        }
                    }
                                        
                    String pubKeyValue = objPubKey.getString(lblPublicKey);
                    String keyId = objPubKey.getString("id");
                    
                    if (!keyId.contains("#key")) {
                        keyId = id + "#key-" + (++keyNum);
                    }
                    
                    PublicKey publicKey;
                    
                    switch (typePublicKey[0]) {
                        case "Ed25519VerificationKey2018":
                            publicKey = PublicKey.build(keyId, typePublicKey, null, pubKeyValue, null, null);
                            break;
                        case "RsaVerificationKey2018":
                            publicKey = PublicKey.build(keyId, typePublicKey, null, null, null, pubKeyValue);
                            break;
                        case "Secp256k1VerificationKey2018":
                            publicKey = PublicKey.build(keyId, typePublicKey, null, null, pubKeyValue, null);
                            break;
                        default:
                            publicKey = null;
                            break;
                    }
                    
                    if (publicKey != null) {
                        publicKeys.add(publicKey);
                    }
                    
                }
            }

            if(jo.isNull("authentication")) {
               // no authentication(s) defined
               throw new ResolutionException("Cannot retrieve authentication for `" + identifier + "`");
            }
            else {
                 // extract authentication(s)        
                JSONArray propAuthentication = jo.getJSONArray("authentication");
                
                int keyNum = 0;
                
                for (Object itemAuthentication: propAuthentication) {
                 
                    JSONObject objAuthentication = (JSONObject) itemAuthentication;
                    
                    String typeAuthentication[] = new String[1];
                    
                    for (String entry : DIDDOCUMENT_AUTHENTICATION_TYPES) {
                        if (objAuthentication.getString("type").equals(entry)) {
                            typeAuthentication[0] = entry;
                        }
                    }
                    
                    String keyId = objAuthentication.getString("publicKey");
                    
                    if (!keyId.contains("#key")) {
                        keyId = id + "#key-" + (++keyNum);
                    }
                    
                    Authentication authentication = Authentication.build(null, typeAuthentication, keyId);
                    authentications.add(authentication);
                }
                
            }
          
            if (!jo.isNull("service")) {
                
                JSONArray propService = jo.getJSONArray("service");
                
                for (Object itemService: propService) {
                    
                    JSONObject objService = (JSONObject) itemService;
                    
                    Service service = Service.build(objService.getString("type"), null, objService.getString("serviceEndpoint"));
                    services.add(service);
                }           
            }

           
        } catch (IOException ex) {
            throw new ResolutionException("Cannot retrieve DDO info for `" + identifier + "` from `" + nameUri + "`: " + ex.getMessage(), ex);
        } catch (JSONException jex) {
            throw new ResolutionException("Cannot parse JSON response from `" + nameUri + "`: " + jex.getMessage(), jex);
        }
        
        // create DDO
        DIDDocument didDocument = DIDDocument.build(id, publicKeys, authentications, null, services);

        // done
        return ResolutionResult.build(didDocument);
    }

    public Map<String, Object> properties() {

    	Map<String, Object> properties = new HashMap<String, Object> ();
    	properties.put("domUrl", this.getDomUrl());
    	
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

    public String getDomUrl() { 

       return this.DID_DOM_RESOLVER_URL;
    }

}
