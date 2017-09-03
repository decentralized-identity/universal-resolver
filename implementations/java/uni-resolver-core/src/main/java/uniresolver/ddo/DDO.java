package uniresolver.ddo;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class DDO {

	public static final String MIME_TYPE = "application/ld+json";

	public static final String JSONLD_TERM_ID = "id";
	public static final String JSONLD_TERM_TYPE = "type";
	public static final String JSONLD_TERM_OWNER = "owner";
	public static final String JSONLD_TERM_CONTROL = "control";
	public static final String JSONLD_TERM_SERVICE = "service";
	public static final String JSONLD_TERM_CURVE = "curve";
	public static final String JSONLD_TERM_PUBLICKEYBASE64 = "publicKeyBase64";
	public static final String JSONLD_TERM_PUBLICKEYHEX = "publicKeyHex";

	public static final Object JSONLD_CONTEXT_THIN;

	private final LinkedHashMap<String, Object> ddoJsonLdObject;

	static {

		try {

			JSONLD_CONTEXT_THIN = JsonUtils.fromInputStream(DDO.class.getResourceAsStream("ddo-context-thin.jsonld"));
		} catch (IOException ex) {

			throw new ExceptionInInitializerError(ex);
		}
	}

	private DDO(LinkedHashMap<String, Object> ddoJsonLdObject) {

		this.ddoJsonLdObject = ddoJsonLdObject;
	}

	public LinkedHashMap<String, Object> getJsonLdObject() {

		return this.ddoJsonLdObject;
	}

	public static DDO build(LinkedHashMap<String, Object> ddoJsonLdObject) {

		return new DDO(ddoJsonLdObject);
	}

	public static DDO build(String id, List<Owner> owners, List<Control> controls, Map<String, String> services) {

		// add 'id'

		LinkedHashMap<String, Object> ddoJsonLdObject = new LinkedHashMap<String, Object> ();
		ddoJsonLdObject.put(JSONLD_TERM_ID, id);

		// add 'owner'

		if (owners != null) {

			LinkedList<Object> ownersJsonLdArray = new LinkedList<Object> ();

			for (Owner owner : owners) {

				LinkedHashMap<String, Object> ownerJsonLdObject = owner.getJsonLdObject();

				ownersJsonLdArray.add(ownerJsonLdObject);
			}

			ddoJsonLdObject.put(JSONLD_TERM_OWNER, ownersJsonLdArray);
		}

		// add 'control'

		if (controls != null) {

			LinkedList<Object> controlsJsonLdArray = new LinkedList<Object> ();

			for (Control control : controls) {

				LinkedHashMap<String, Object> controlJsonLdObject = control.getJsonLdObject();

				controlsJsonLdArray.add(controlJsonLdObject);
			}

			ddoJsonLdObject.put(JSONLD_TERM_CONTROL, controlsJsonLdArray);
		}

		// add 'service'

		if (services != null) {

			LinkedHashMap<String, Object> servicesJsonLdObject = new LinkedHashMap<String, Object> ();

			for (Map.Entry<String, String> service : services.entrySet()) {

				servicesJsonLdObject.put(service.getKey(), service.getValue());
			}

			ddoJsonLdObject.put(JSONLD_TERM_SERVICE, servicesJsonLdObject);
		}

		// done

		return new DDO(ddoJsonLdObject);
	}

	public static DDO fromInputStream(InputStream input, String enc) throws IOException {

		return build((LinkedHashMap<String, Object>) JsonUtils.fromInputStream(input, enc));
	}

	public static DDO fromReader(Reader reader) throws IOException {

		return build((LinkedHashMap<String, Object>) JsonUtils.fromReader(reader));
	}

	public static DDO fromString(String jsonString) throws IOException {

		return build((LinkedHashMap<String, Object>) JsonUtils.fromString(jsonString));
	}

	public String serialize() throws IOException, JsonLdError {

		Object ddo = JsonUtils.fromInputStream(DDO.class.getResourceAsStream("ddo-skeleton.jsonld"));
		((LinkedHashMap<String, Object>) ddo).putAll(this.ddoJsonLdObject);

		JsonLdOptions options = new JsonLdOptions();
		Object rdf = JsonLdProcessor.compact(ddo, JSONLD_CONTEXT_THIN, options);
		String result = JsonUtils.toPrettyString(rdf);

		return result;
	}

	public String getId() {

		Object entry = this.ddoJsonLdObject.get(JSONLD_TERM_ID);
		if (entry == null) return null;
		if (! (entry instanceof URI)) return null;

		String id = (String) entry;

		return id;
	}

	public List<Owner> getOwners() {

		Object entry = this.ddoJsonLdObject.get(JSONLD_TERM_OWNER);
		if (entry == null) return null;
		if (! (entry instanceof LinkedList<?>)) return null;

		LinkedList<Object> ownersJsonLdArray = (LinkedList<Object>) entry;

		List<Owner> owners = new ArrayList<Owner> ();

		for (Object entry2 : ownersJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			LinkedHashMap<String, Object> ownerJsonLdObject = (LinkedHashMap<String, Object>) entry2;

			owners.add(Owner.build(ownerJsonLdObject));
		}

		return owners;
	}

	public List<Control> getControls() {

		Object entry = this.ddoJsonLdObject.get(JSONLD_TERM_CONTROL);
		if (entry == null) return null;
		if (! (entry instanceof LinkedList<?>)) return null;

		LinkedList<Object> controlsJsonLdArray = (LinkedList<Object>) entry;

		List<Control> controls = new ArrayList<Control> ();

		for (Object entry2 : controlsJsonLdArray) {

			if (! (entry2 instanceof LinkedHashMap<?, ?>)) continue;

			LinkedHashMap<String, Object> controlJsonLdObject = (LinkedHashMap<String, Object>) entry2;

			controls.add(Control.build(controlJsonLdObject));
		}

		return controls;
	}

	public Map<String, String> getServices() {

		Object entry = this.ddoJsonLdObject.get(JSONLD_TERM_SERVICE);
		if (entry == null) return null;
		if (! (entry instanceof LinkedHashMap<?, ?>)) return null;

		LinkedHashMap<String, Object> servicesJsonLdObject = (LinkedHashMap<String, Object>) entry;

		Map<String, String> services = new HashMap<String, String> ();

		for (Map.Entry<String, Object> entry2 : servicesJsonLdObject.entrySet()) {

			if (! (entry2.getValue() instanceof String)) continue;

			services.put(entry2.getKey(), (String) entry2.getValue());
		}

		return services;
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

	/*
	 * Helper classes
	 */

	public static class Owner {

		private final LinkedHashMap<String, Object> ownerJsonLdObject;

		private Owner(LinkedHashMap<String, Object> ownerJsonLdObject) {

			this.ownerJsonLdObject = ownerJsonLdObject;
		}

		public LinkedHashMap<String, Object> getJsonLdObject() {

			return this.ownerJsonLdObject;
		}

		public static Owner build(LinkedHashMap<String, Object> ownerJsonLdObject) {

			return new Owner(ownerJsonLdObject);
		}

		public static Owner build(String id, String[] types, String curve, String publicKeyBase64, String publicKeyHex) {

			LinkedHashMap<String, Object> ownerJsonLdObject = new LinkedHashMap<String, Object> ();

			// add 'id'

			if (id != null) {

				ownerJsonLdObject.put(JSONLD_TERM_ID, id);
			}

			// add 'type'

			if (types != null) {

				LinkedList<String> typesJsonLdArray = new LinkedList<String> ();
				for (String type : types) typesJsonLdArray.add(type);

				ownerJsonLdObject.put(JSONLD_TERM_TYPE, typesJsonLdArray);
			}

			// add 'curve'

			if (curve != null) {

				ownerJsonLdObject.put(JSONLD_TERM_CURVE, curve);
			}

			// add 'publicKeyBase64'

			if (publicKeyBase64 != null) {

				ownerJsonLdObject.put(JSONLD_TERM_PUBLICKEYBASE64, publicKeyBase64);
			}

			// add 'publicKeyHex'

			if (publicKeyHex != null) {

				ownerJsonLdObject.put(JSONLD_TERM_PUBLICKEYHEX, publicKeyHex);
			}

			// done

			return new Owner(ownerJsonLdObject);
		}

		public String getId() {

			Object entry = this.ownerJsonLdObject.get(JSONLD_TERM_ID);
			if (entry == null) return null;
			if (! (entry instanceof String)) return null;

			String id = (String) entry;

			return id;
		}

		public String getType() {

			Object entry = this.ownerJsonLdObject.get(JSONLD_TERM_TYPE);
			if (entry == null) return null;
			if (! (entry instanceof String)) return null;

			String type = (String) entry;

			return type;
		}

		public String getCurve() {

			Object entry = this.ownerJsonLdObject.get(JSONLD_TERM_CURVE);
			if (entry == null) return null;
			if (! (entry instanceof String)) return null;

			String curve = (String) entry;

			return curve;
		}

		public String getPublicKeyBase64() {

			Object entry = this.ownerJsonLdObject.get(JSONLD_TERM_PUBLICKEYBASE64);
			if (entry == null) return null;
			if (! (entry instanceof String)) return null;

			String publicKeyBase64 = (String) entry;

			return publicKeyBase64;
		}

		public String getPublicKeyHex() {

			Object entry = this.ownerJsonLdObject.get(JSONLD_TERM_PUBLICKEYHEX);
			if (entry == null) return null;
			if (! (entry instanceof String)) return null;

			String publicKeyHex = (String) entry;

			return publicKeyHex;
		}
	}

	public static class Control {

		private final LinkedHashMap<String, Object> controlJsonLdObject;

		private Control(LinkedHashMap<String, Object> controlJsonLdObject) {

			this.controlJsonLdObject = controlJsonLdObject;
		}

		public LinkedHashMap<String, Object> getJsonLdObject() {

			return this.controlJsonLdObject;
		}

		public static Control build(LinkedHashMap<String, Object> controlJsonLdObject) {

			return new Control(controlJsonLdObject);
		}

		public static Control build(String type) {

			LinkedHashMap<String, Object> controlJsonLdObject = new LinkedHashMap<String, Object> ();

			// add 'type'

			if (type != null) {

				controlJsonLdObject.put(JSONLD_TERM_TYPE, type);
			}

			// done

			return new Control(controlJsonLdObject);
		}

		public String getType() {

			Object entry = this.controlJsonLdObject.get(JSONLD_TERM_TYPE);
			if (entry == null) return null;
			if (! (entry instanceof String)) return null;

			String type = (String) entry;

			return type;
		}
	}
}
