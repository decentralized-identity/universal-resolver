package uniresolver;

import uniresolver.result.DereferenceResult;
import uniresolver.w3c.DIDURLDereferencer;

import java.util.HashMap;
import java.util.Map;

public interface UniDereferencer extends DIDURLDereferencer {

	@Override public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions) throws DereferencingException;

	default public DereferenceResult dereference(String didUrlString) throws DereferencingException {
		return this.dereference(didUrlString, new HashMap<String, Object>());
	}
}
