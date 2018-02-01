package uniresolver.did;

import java.util.LinkedHashMap;

public class Service extends JsonLdObject {

	private Service(LinkedHashMap<String, Object> jsonLdObject) {

		super(jsonLdObject);
	}

	public static Service build(LinkedHashMap<String, Object> jsonLdObject) {

		return new Service(jsonLdObject);
	}

	public static Service build(String[] types, String serviceEndpoint) {

		LinkedHashMap<String, Object> jsonLdObject = JsonLdObject.init(null, types);

		// add 'serviceEndpoint'

		if (serviceEndpoint != null) {

			jsonLdObject.put(DIDDocument.JSONLD_TERM_SERVICEENDPOINT, serviceEndpoint);
		}

		// done

		return new Service(jsonLdObject);
	}

	public static Service build(String type, String serviceEndpoint) {

		return build(new String[] { type }, serviceEndpoint);
	}

	/*
	 * Getters
	 */

	public String getServiceEndpoint() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_SERVICEENDPOINT);
		if (entry == null) return null;
		if (! (entry instanceof String)) return null;

		String serviceEndpoint = (String) entry;

		return serviceEndpoint;
	}
}