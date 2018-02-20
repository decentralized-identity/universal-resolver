package uniresolver;

import java.util.Map;

import uniresolver.result.ResolutionResult;

public interface UniResolver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public ResolutionResult resolve(String identifier) throws ResolutionException;
	public ResolutionResult resolve(String identifier, String selectServiceType) throws ResolutionException;
	public Map<String, Map<String, Object>> properties() throws ResolutionException;
}
