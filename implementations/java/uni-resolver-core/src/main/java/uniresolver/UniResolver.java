package uniresolver;

import java.util.Map;

import uniresolver.result.ResolutionResult;

public interface UniResolver {

	public ResolutionResult resolve(String identifier) throws ResolutionException;
	public Map<String, Map<String, Object>> properties() throws ResolutionException;
}
