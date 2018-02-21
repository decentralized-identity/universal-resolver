package uniresolver.did;

import java.util.Map;

public class Authentication extends JsonLdObject {

	private Authentication(Map<String, Object> jsonLdObject) {

		super(jsonLdObject);
	}

	public static Authentication build(Map<String, Object> jsonLdObject) {

		return new Authentication(jsonLdObject);
	}

	public static Authentication build(String id, String[] types, String publicKey) {

		Map<String, Object> jsonLdObject = JsonLdObject.init(id, types);

		// add 'publicKey'

		if (publicKey != null) {

			jsonLdObject.put(DIDDocument.JSONLD_TERM_PUBLICKEY, publicKey);
		}

		// done

		return new Authentication(jsonLdObject);
	}

	/*
	 * Getters
	 */

	public String getPublicKeyBase() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_PUBLICKEY);
		if (entry == null) return null;
		if (! (entry instanceof String)) return null;

		String publicKey = (String) entry;

		return publicKey;
	}
}