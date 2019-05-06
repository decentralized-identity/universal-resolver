package uniresolver;

import java.util.Map;

import uniresolver.result.ResolveResult;

public interface UniResolver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public ResolveResult resolve(String identifier) throws ResolutionException;
	public ResolveResult resolve(String identifier, Map<String, String> options) throws ResolutionException;
	public Map<String, Map<String, Object>> properties() throws ResolutionException;
}
