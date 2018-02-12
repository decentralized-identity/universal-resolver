package uniresolver.driver;

import java.util.Map;

import uniresolver.ResolutionException;
import uniresolver.result.ResolutionResult;

public interface Driver {

	public ResolutionResult resolve(String identifier) throws ResolutionException;
	public Map<String, Object> properties() throws ResolutionException;
}
