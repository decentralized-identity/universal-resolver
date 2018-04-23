package uniresolver.did;

import java.util.Map;

public class PublicKey extends JsonLdObject {

	private PublicKey(Map<String, Object> jsonLdObject) {

		super(jsonLdObject);
	}

	public static PublicKey build(Map<String, Object> jsonLdObject) {

		return new PublicKey(jsonLdObject);
	}

	public static PublicKey build(String id, String[] types, String publicKeyBase64, String publicKeyBase58, String publicKeyHex, String publicKeyPem) {

		Map<String, Object> jsonLdObject = JsonLdObject.init(id, types);

		// add 'publicKeyBase64'

		if (publicKeyBase64 != null) {

			jsonLdObject.put(DIDDocument.JSONLD_TERM_PUBLICKEYBASE64, publicKeyBase64);
		}

		// add 'publicKeyBase58'

		if (publicKeyBase58 != null) {

			jsonLdObject.put(DIDDocument.JSONLD_TERM_PUBLICKEYBASE58, publicKeyBase58);
		}

		// add 'publicKeyHex'

		if (publicKeyHex != null) {

			jsonLdObject.put(DIDDocument.JSONLD_TERM_PUBLICKEYHEX, publicKeyHex);
		}

		// add 'publicKeyPem'

		if (publicKeyPem != null) {

			jsonLdObject.put(DIDDocument.JSONLD_TERM_PUBLICKEYPEM, publicKeyPem);
		}

		// done

		return new PublicKey(jsonLdObject);
	}

	/*
	 * Getters
	 */

	public String getPublicKeyBase64() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_PUBLICKEYBASE64);
		if (entry == null) return null;
		if (! (entry instanceof String)) return null;

		String publicKeyBase64 = (String) entry;

		return publicKeyBase64;
	}

	public String getPublicKeyBase58() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_PUBLICKEYBASE58);
		if (entry == null) return null;
		if (! (entry instanceof String)) return null;

		String publicKeyBase58 = (String) entry;

		return publicKeyBase58;
	}

	public String getPublicKeyHex() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_PUBLICKEYHEX);
		if (entry == null) return null;
		if (! (entry instanceof String)) return null;

		String publicKeyHex = (String) entry;

		return publicKeyHex;
	}

	public String getPublicKeyPem() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_PUBLICKEYPEM);
		if (entry == null) return null;
		if (! (entry instanceof String)) return null;

		String publicKeyPem = (String) entry;

		return publicKeyPem;
	}
}