package uniresolver.did;

import java.util.Map;

public class Encryption extends JsonLdObject {

	private Encryption(Map<String, Object> jsonLdObject) {

		super(jsonLdObject);
	}

	public static Encryption build(Map<String, Object> jsonLdObject) {

		return new Encryption(jsonLdObject);
	}

	public static Encryption build(String id, String[] types, String publicKey) {

		Map<String, Object> jsonLdObject = JsonLdObject.init(id, types);

		// add 'publicKey'

		if (publicKey != null) {

			jsonLdObject.put(DIDDocument.JSONLD_TERM_PUBLICKEY, publicKey);
		}

		// done

		return new Encryption(jsonLdObject);
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