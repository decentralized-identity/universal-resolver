package uniresolver;

import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.Map;

public interface ExecutionStateUniDereferencer extends UniDereferencer {

    public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions, Map<String, Object> executionState) throws DereferencingException, ResolutionException;
}
