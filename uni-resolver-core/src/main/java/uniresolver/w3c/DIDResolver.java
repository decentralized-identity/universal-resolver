package uniresolver.w3c;

import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;

import java.util.Map;

public interface DIDResolver {

	public ResolveResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException;
}
