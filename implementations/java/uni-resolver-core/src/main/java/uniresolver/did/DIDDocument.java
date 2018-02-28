package uniresolver.did;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class DIDDocument {

	public static final String MIME_TYPE = "application/json";

	public static final String JSONLD_TERM_ID = "id";
	public static final String JSONLD_TERM_TYPE = "type";
	public static final String JSONLD_TERM_SERVICE = "service";
	public static final String JSONLD_TERM_NAME = "name";
	public static final String JSONLD_TERM_SERVICEENDPOINT = "serviceEndpoint";
	public static final String JSONLD_TERM_PUBLICKEY = "publicKey";
	public static final String JSONLD_TERM_PUBLICKEYBASE64 = "publicKeyBase64";
	public static final String JSONLD_TERM_PUBLICKEYBASE58 = "publicKeyBase58";
	public static final String JSONLD_TERM_PUBLICKEYHEX = "publicKeyHex";
	public static final String JSONLD_TERM_PUBLICKEYPEM = "publicKeyPem";
	public static final String JSONLD_TERM_AUTHENTICATION = "authentication";
	public static final String JSONLD_TERM_ENCRYPTION = "encryption";

	public static final Object JSONLD_CONTEXT;

	private final Map<String, Object> jsonLdObject;

	static {

		try {

			JSONLD_CONTEXT = JsonUtils.fromInputStream(DIDDocument.class.getResourceAsStream("diddocument-context.jsonld"));
		} catch (IOException ex) {

			throw new ExceptionInInitializerError(ex);
		}
	}

	private DIDDocument(Map<String, Object> jsonLdObject) {

		this.jsonLdObject = jsonLdObject;
	}

	private DIDDocument() {

		this(new HashMap<String, Object> ());
	}

	/*
	 * Factory methods
	 */

	public static DIDDocument build(Map<String, Object> jsonLdObject) {

		return new DIDDocument(jsonLdObject);
	}

	public static DIDDocument build(String id, List<PublicKey> publicKeys, List<Authentication> authentications, List<Encryption> encryptions, List<Service> services) {

		// add 'id'

		Map<String, Object> jsonLdObject = new LinkedHashMap<String, Object> ();
		jsonLdObject.put(JSONLD_TERM_ID, id);

		// add 'publicKey'

		if (publicKeys != null) {

			LinkedList<Object> publicKeysJsonLdArray = new LinkedList<Object> ();

			for (PublicKey publicKey : publicKeys) {

				Map<String, Object> publicKeyJsonLdObject = publicKey.getJsonLdObject();

				publicKeysJsonLdArray.add(publicKeyJsonLdObject);
			}

			jsonLdObject.put(JSONLD_TERM_PUBLICKEY, publicKeysJsonLdArray);
		}

		// add 'publicKey'

		if (publicKeys != null) {

			LinkedList<Object> publicKeysJsonLdArray = new LinkedList<Object> ();

			for (PublicKey publicKey : publicKeys) {

				Map<String, Object> publicKeyJsonLdObject = publicKey.getJsonLdObject();

				publicKeysJsonLdArray.add(publicKeyJsonLdObject);
			}

			jsonLdObject.put(JSONLD_TERM_PUBLICKEY, publicKeysJsonLdArray);
		}

		// add 'service'

		if (services != null) {

			LinkedList<Object> servicesJsonLdArray = new LinkedList<Object> ();

			for (Service service : services) {

				Map<String, Object> serviceJsonLdObject = service.getJsonLdObject();

				servicesJsonLdArray.add(serviceJsonLdObject);
			}

			jsonLdObject.put(JSONLD_TERM_SERVICE, servicesJsonLdArray);
		}

		// add 'authentication'

		if (authentications != null && authentications.size() > 0) {

			LinkedList<Object> authenticationsJsonLdArray = new LinkedList<Object> ();

			for (Authentication authentication : authentications) {

				Map<String, Object> authenticationJsonLdObject = authentication.getJsonLdObject();

				authenticationsJsonLdArray.add(authenticationJsonLdObject);
			}

			jsonLdObject.put(JSONLD_TERM_AUTHENTICATION, authenticationsJsonLdArray);
		}

		// add 'encryption'

		if (encryptions != null && encryptions.size() > 0) {

			LinkedList<Object> encryptionsJsonLdArray = new LinkedList<Object> ();

			for (Encryption encryption : encryptions) {

				Map<String, Object> encryptionJsonLdObject = encryption.getJsonLdObject();

				encryptionsJsonLdArray.add(encryptionJsonLdObject);
			}

			jsonLdObject.put(JSONLD_TERM_ENCRYPTION, encryptionsJsonLdArray);
		}

		// done

		return new DIDDocument(jsonLdObject);
	}

	/*
	 * Service selection
	 */

	public Integer[] selectServices(String selectServiceName, String selectServiceType) {

		int i = -1;
		List<Integer> selectedServices = new ArrayList<Integer> ();

		for (Service service : this.getServices()) {

			i++;

			if (selectServiceName != null) {

				if (service.getName() == null) continue;
				if (! service.getName().equals(selectServiceName)) continue;
			}

			if (selectServiceType != null) {

				if (service.getTypes() == null) continue;
				if (! Arrays.asList(service.getTypes()).contains(selectServiceType)) continue;
			}

			selectedServices.add(Integer.valueOf(i));
		}

		return selectedServices.toArray(new Integer[selectedServices.size()]);
	}

	/*
	 * Serialization
	 */

	@SuppressWarnings("unchecked")
	public static DIDDocument fromJson(String jsonString) throws IOException {

		Map<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(jsonString);

		return build(jsonLdObject);
	}

	public static DIDDocument fromJson(InputStream input, String enc) throws IOException {

		return fromJson(IOUtils.toString(input, StandardCharsets.UTF_8));
	}

	public static DIDDocument fromJson(Reader reader) throws IOException {

		return fromJson(IOUtils.toString(reader));
	}

	@SuppressWarnings("unchecked")
	public String toJson() throws IOException, JsonLdError {

		Map<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromInputStream(DIDDocument.class.getResourceAsStream("diddocument-skeleton.jsonld"));
		jsonLdObject.putAll(this.jsonLdObject);

		JsonLdOptions options = new JsonLdOptions();
		Object rdf = JsonLdProcessor.compact(jsonLdObject, JSONLD_CONTEXT, options);
		String result = JsonUtils.toPrettyString(rdf);

		return result;
	}

	/*
	 * Getters and setters
	 */

	@JsonValue
	public Map<String, Object> getJsonLdObject() {

		return this.jsonLdObject;
	}

	@JsonAnySetter
	public void setJsonLdObjectKeyValue(String key, Object value) {

		this.jsonLdObject.put(key, value);
	}

	public String getId() {

		Object entry = this.jsonLdObject.get(JSONLD_TERM_ID);
		if (entry == null) return null;
		if (! (entry instanceof URI)) return null;

		String id = (String) entry;

		return id;
	}

	@SuppressWarnings("unchecked")
	public List<PublicKey> getPublicKeys() {

		Object entry = this.jsonLdObject.get(JSONLD_TERM_PUBLICKEY);
		if (entry == null) return null;
		if (entry instanceof LinkedHashMap<?, ?>) entry = Collections.singletonList(entry);
		if (! (entry instanceof List<?>)) return null;

		List<Object> publicKeysJsonLdArray = (List<Object>) entry;

		List<PublicKey> publicKeys = new ArrayList<PublicKey> ();

		for (Object entry2 : publicKeysJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			LinkedHashMap<String, Object> publicKeyJsonLdObject = (LinkedHashMap<String, Object>) entry2;

			publicKeys.add(PublicKey.build(publicKeyJsonLdObject));
		}

		return publicKeys;
	}

	@SuppressWarnings("unchecked")
	public List<Authentication> getAuthentications() {

		Object entry = this.jsonLdObject.get(JSONLD_TERM_AUTHENTICATION);
		if (entry == null) return null;
		if (entry instanceof LinkedHashMap<?, ?>) entry = Collections.singletonList(entry);
		if (! (entry instanceof List<?>)) return null;

		List<Object> authenticationsJsonLdArray = (List<Object>) entry;

		List<Authentication> authentications = new ArrayList<Authentication> ();

		for (Object entry2 : authenticationsJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			LinkedHashMap<String, Object> authenticationJsonLdObject = (LinkedHashMap<String, Object>) entry2;

			authentications.add(Authentication.build(authenticationJsonLdObject));
		}

		return authentications;
	}

	@SuppressWarnings("unchecked")
	public List<Encryption> getEncryptions() {

		Object entry = this.jsonLdObject.get(JSONLD_TERM_ENCRYPTION);
		if (entry == null) return null;
		if (entry instanceof LinkedHashMap<?, ?>) entry = Collections.singletonList(entry);
		if (! (entry instanceof List<?>)) return null;

		List<Object> encryptionsJsonLdArray = (List<Object>) entry;

		List<Encryption> encryptions = new ArrayList<Encryption> ();

		for (Object entry2 : encryptionsJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			LinkedHashMap<String, Object> encryptionJsonLdObject = (LinkedHashMap<String, Object>) entry2;

			encryptions.add(Encryption.build(encryptionJsonLdObject));
		}

		return encryptions;
	}

	@SuppressWarnings("unchecked")
	public List<Service> getServices() {

		Object entry = this.jsonLdObject.get(JSONLD_TERM_SERVICE);
		if (entry == null) return null;
		if (entry instanceof LinkedHashMap<?, ?>) entry = Collections.singletonList(entry);
		if (! (entry instanceof List<?>)) return null;

		List<Object> servicesJsonLdArray = (List<Object>) entry;

		List<Service> services = new ArrayList<Service> ();

		for (Object entry2 : servicesJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			Map<String, Object> serviceJsonLdObject = (Map<String, Object>) entry2;

			services.add(Service.build(serviceJsonLdObject));
		}

		return services;
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		try {

			return this.toJson();
		} catch (IOException | JsonLdError ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
