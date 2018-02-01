package uniresolver.did;

import java.net.URI;

public class DID {

	private URI uri;

	private DID(URI uri) {

		this.uri = uri;
	}

	/*
	 * Factory methods
	 */

	public static DID fromUri(URI uri) {

		return new DID(uri);
	}

	public static DID create(String uri) {

		return fromUri(URI.create(uri));
	}

	/*
	 * Getters
	 */

	public URI getUri() {

		return this.uri;
	}

	/*
	 * Object methods
	 */

	@Override
	public int hashCode() {

		return this.uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		return this.uri.equals(obj);
	}

	@Override
	public String toString() {

		return this.uri.toString();
	}
}
