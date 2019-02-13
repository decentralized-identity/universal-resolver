package uniresolver.driver;

import java.util.Map;

import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public ResolveResult resolve(String identifier) throws ResolutionException;
	public Map<String, Object> properties() throws ResolutionException;
}
