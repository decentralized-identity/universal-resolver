package uniresolver;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uniresolver.result.ResolveResult;

public interface UniResolver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";
	public static final String METHODS_MIME_TYPE = "application/json";
	public static final String TEST_IDENTIFIER_MIME_TYPE = "application/json";

	public ResolveResult resolve(String identifier) throws ResolutionException;
	public ResolveResult resolve(String identifier, Map<String, String> options) throws ResolutionException;
	public Map<String, Map<String, Object>> properties() throws ResolutionException;
	public Set<String> methods() throws ResolutionException;
	public Map<String, List<String>> testIdentifiers() throws ResolutionException;
}
