package uniresolver.driver;

import uniresolver.ResolutionException;
import uniresolver.did.DIDDocument;

public interface Driver {

	public DIDDocument resolve(String identifier) throws ResolutionException;
}
