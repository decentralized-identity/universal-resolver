package uniresolver;

import uniresolver.result.ResolveResult;

import java.util.Map;

public interface ExecutionStateUniResolver extends UniResolver {

    public ResolveResult resolve(String didString, Map<String, Object> resolutionOptions, Map<String, Object> executionState) throws ResolutionException;
}
