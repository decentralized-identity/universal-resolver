package uniresolver;

import uniresolver.did.DIDDocument;

public interface UniResolver {

	public DIDDocument resolve(String identifier) throws ResolutionException;
}
