package uniresolver.driver;

import java.util.Map;

import uniresolver.ResolutionException;
import uniresolver.result.ResolutionResult;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public ResolutionResult resolve(String identifier) throws ResolutionException;
	public Map<String, Object> properties() throws ResolutionException;
}
