package uniresolver.driver;

import uniresolver.ResolutionException;
import uniresolver.result.ResolutionResult;

public interface Driver {

	public ResolutionResult resolve(String identifier) throws ResolutionException;
}
