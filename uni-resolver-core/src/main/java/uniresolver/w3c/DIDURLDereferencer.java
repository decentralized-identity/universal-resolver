package uniresolver.w3c;

import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.result.DereferenceResult;

import java.util.Map;

public interface DIDURLDereferencer {

	public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions) throws DereferencingException, ResolutionException;
}
