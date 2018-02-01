package uniresolver.did;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class JsonLdObject {

	protected final LinkedHashMap<String, Object> jsonLdObject;

	protected JsonLdObject(LinkedHashMap<String, Object> jsonLdObject) {

		this.jsonLdObject = jsonLdObject;
	}

	public final LinkedHashMap<String, Object> getJsonLdObject() {

		return this.jsonLdObject;
	}

	protected static LinkedHashMap<String, Object> init(String id, String[] types) {

		LinkedHashMap<String, Object> jsonLdObject = new LinkedHashMap<String, Object> ();

		// add 'id'

		if (id != null) {

			jsonLdObject.put(DIDDocument.JSONLD_TERM_ID, id);
		}

		// add 'type'

		if (types != null) {

			if (types.length == 1) {

				jsonLdObject.put(DIDDocument.JSONLD_TERM_TYPE, types[0]);
			} else {

				LinkedList<String> typesJsonLdArray = new LinkedList<String> ();
				for (String type : types) typesJsonLdArray.add(type);

				jsonLdObject.put(DIDDocument.JSONLD_TERM_TYPE, Arrays.asList(types));
			}
		}

		// done

		return jsonLdObject;
	}

	protected static LinkedHashMap<String, Object> init(String id, String type) {

		return init(id, new String[] { type });
	}

	/*
	 * Getters
	 */

	public final String getId() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_ID);
		if (entry == null) return null;
		if (! (entry instanceof String)) return null;

		String id = (String) entry;

		return id;
	}

	public final String[] getTypes() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_TYPE);
		if (entry == null) return null;

		if (entry instanceof String)
			return new String[] { (String) entry };
		else if (entry instanceof List)
			return ((List<String>) entry).toArray(new String[((List<String>) entry).size()]);
		else return null;
	}

	public final String getType() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_TYPE);
		if (entry == null) return null;

		if (entry instanceof String)
			return (String) entry;
		else if (entry instanceof List)
			return ((List<String>) entry).get(0);
		else return null;
	}

	public final boolean isType(String type) {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_TYPE);
		if (entry == null) return false;

		if (entry instanceof String)
			return ((String) entry).equals(type);
		else if (entry instanceof List)
			return ((List<String>) entry).contains(type);
		else return false;
	}
}