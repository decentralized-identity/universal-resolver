package uniresolver;

import java.util.Collection;

import uniresolver.result.ResolutionResult;

public interface UniResolver {

	public ResolutionResult resolve(String identifier) throws ResolutionException;
	public Collection<String> getDriverIds() throws ResolutionException;
}
