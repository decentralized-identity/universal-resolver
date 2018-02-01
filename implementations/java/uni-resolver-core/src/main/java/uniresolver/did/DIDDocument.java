package uniresolver.did;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class DIDDocument {

	public static final String MIME_TYPE = "application/ld+json";

	public static final String JSONLD_TERM_ID = "id";
	public static final String JSONLD_TERM_TYPE = "type";
	public static final String JSONLD_TERM_SERVICE = "service";
	public static final String JSONLD_TERM_SERVICEENDPOINT = "serviceEndpoint";
	public static final String JSONLD_TERM_PUBLICKEY = "publicKey";
	public static final String JSONLD_TERM_PUBLICKEYBASE64 = "publicKeyBase64";
	public static final String JSONLD_TERM_PUBLICKEYHEX = "publicKeyHex";

	public static final Object JSONLD_CONTEXT_THIN;

	private final LinkedHashMap<String, Object> didDocumentJsonLdObject;
	private final String jsonString;

	static {

		try {

			JSONLD_CONTEXT_THIN = JsonUtils.fromInputStream(DIDDocument.class.getResourceAsStream("ddo-context-thin.jsonld"));
		} catch (IOException ex) {

			throw new ExceptionInInitializerError(ex);
		}
	}

	private DIDDocument(LinkedHashMap<String, Object> didDocumentJsonLdObject, String jsonString) {

		this.didDocumentJsonLdObject = didDocumentJsonLdObject;
		this.jsonString = jsonString;
	}

	public LinkedHashMap<String, Object> getJsonLdObject() {

		return this.didDocumentJsonLdObject;
	}

	public static DIDDocument build(LinkedHashMap<String, Object> ddoJsonLdObject, String jsonString) {

		return new DIDDocument(ddoJsonLdObject, jsonString);
	}

	public static DIDDocument build(String id, List<PublicKey> publicKeys, List<Service> services) {

		// add 'id'

		LinkedHashMap<String, Object> didDocumentJsonLdObject = new LinkedHashMap<String, Object> ();
		didDocumentJsonLdObject.put(JSONLD_TERM_ID, id);

		// add 'publicKey'

		if (publicKeys != null) {

			LinkedList<Object> publicKeysJsonLdArray = new LinkedList<Object> ();

			for (PublicKey publicKey : publicKeys) {

				LinkedHashMap<String, Object> publicKeyJsonLdObject = publicKey.getJsonLdObject();

				publicKeysJsonLdArray.add(publicKeyJsonLdObject);
			}

			didDocumentJsonLdObject.put(JSONLD_TERM_PUBLICKEY, publicKeysJsonLdArray);
		}

		// add 'service'

		if (services != null) {

			LinkedList<Object> servicesJsonLdArray = new LinkedList<Object> ();

			for (Service service : services) {

				LinkedHashMap<String, Object> serviceJsonLdObject = service.getJsonLdObject();

				servicesJsonLdArray.add(serviceJsonLdObject);
			}

			didDocumentJsonLdObject.put(JSONLD_TERM_SERVICE, servicesJsonLdArray);
		}

		// done

		return new DIDDocument(didDocumentJsonLdObject, null);
	}

	@SuppressWarnings("unchecked")
	public static DIDDocument fromString(String jsonString) throws IOException {

		LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(jsonString);

		return build(jsonLdObject, JsonUtils.toPrettyString(jsonLdObject));
	}

	public static DIDDocument fromInputStream(InputStream input, String enc) throws IOException {

		return fromString(IOUtils.toString(input, StandardCharsets.UTF_8));
	}

	public static DIDDocument fromReader(Reader reader) throws IOException {

		return fromString(IOUtils.toString(reader));
	}

	@SuppressWarnings("unchecked")
	public String serialize() throws IOException, JsonLdError {

		if (this.jsonString != null) return this.jsonString;

		LinkedHashMap<String, Object> didDocumentJsonObject = (LinkedHashMap<String, Object>) JsonUtils.fromInputStream(DIDDocument.class.getResourceAsStream("diddocument-skeleton.jsonld"));
		didDocumentJsonObject.putAll(this.didDocumentJsonLdObject);

		JsonLdOptions options = new JsonLdOptions();
		Object rdf = JsonLdProcessor.compact(didDocumentJsonObject, JSONLD_CONTEXT_THIN, options);
		String result = JsonUtils.toPrettyString(rdf);

		return result;
	}

	public String getId() {

		Object entry = this.didDocumentJsonLdObject.get(JSONLD_TERM_ID);
		if (entry == null) return null;
		if (! (entry instanceof URI)) return null;

		String id = (String) entry;

		return id;
	}

	@SuppressWarnings("unchecked")
	public List<PublicKey> getPublicKeys() {

		Object entry = this.didDocumentJsonLdObject.get(JSONLD_TERM_PUBLICKEY);
		if (entry == null) return null;
		if (! (entry instanceof LinkedList<?>)) return null;

		LinkedList<Object> publicKeysJsonLdArray = (LinkedList<Object>) entry;

		List<PublicKey> publicKeys = new ArrayList<PublicKey> ();

		for (Object entry2 : publicKeysJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			LinkedHashMap<String, Object> publicKeyJsonLdObject = (LinkedHashMap<String, Object>) entry2;

			publicKeys.add(PublicKey.build(publicKeyJsonLdObject));
		}

		return publicKeys;
	}

	@SuppressWarnings("unchecked")
	public List<Service> getServices() {

		Object entry = this.didDocumentJsonLdObject.get(JSONLD_TERM_SERVICE);
		if (entry == null) return null;
		if (! (entry instanceof LinkedList<?>)) return null;

		LinkedList<Object> servicesJsonLdArray = (LinkedList<Object>) entry;

		List<Service> controls = new ArrayList<Service> ();

		for (Object entry2 : servicesJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			LinkedHashMap<String, Object> serviceJsonLdObject = (LinkedHashMap<String, Object>) entry2;

			controls.add(Service.build(serviceJsonLdObject));
		}

		return controls;
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		try {

			return this.serialize();
		} catch (IOException | JsonLdError ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
