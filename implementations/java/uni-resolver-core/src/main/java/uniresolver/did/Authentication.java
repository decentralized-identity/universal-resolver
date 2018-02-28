package uniresolver.did;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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

	@SuppressWarnings("unchecked")
	public List<PublicKey> getPublicKeys() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_PUBLICKEY);
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

	public String getPublicKey() {

		Object entry = this.jsonLdObject.get(DIDDocument.JSONLD_TERM_PUBLICKEY);
		if (entry == null) return null;
		if (! (entry instanceof String)) return null;

		String publicKey = (String) entry;

		return publicKey;
	}
}