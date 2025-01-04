package uniresolver;

import uniresolver.result.DereferenceResult;
import uniresolver.w3c.DIDURLDereferencer;

import java.util.HashMap;
import java.util.Map;

public interface UniDereferencer extends DIDURLDereferencer {

	@Override public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions) throws DereferencingException, ResolutionException;

	default public DereferenceResult dereference(String didUrlString) throws DereferencingException, ResolutionException {
		return this.dereference(didUrlString, new HashMap<>());
	}
}
