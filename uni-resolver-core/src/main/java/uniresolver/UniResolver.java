package uniresolver;

import uniresolver.result.ResolveResult;
import uniresolver.util.ResolveResultUtil;
import uniresolver.w3c.DIDResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UniResolver extends DIDResolver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";
	public static final String METHODS_MIME_TYPE = "application/json";
	public static final String TEST_IDENTIFIER_MIME_TYPE = "application/json";

	@Override
	default public ResolveResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		ResolveResult resolveRepresentationResult = this.resolveRepresentation(didString, resolutionOptions);
		ResolveResult resolveResult = ResolveResultUtil.convertToResolveResult(resolveRepresentationResult);
		return resolveResult;
	}

	@Override
	default public ResolveResult resolveRepresentation(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		ResolveResult resolveResult = this.resolve(didString, resolutionOptions);
		String accept = (String) resolutionOptions.get("accept");
		if (accept == null) throw new ResolutionException("No 'accept' provided in 'resolutionOptions' for resolveRepresentation().");
		ResolveResult resolveRepresentationResult = ResolveResultUtil.convertToResolveRepresentationResult(resolveResult, accept);
		return resolveRepresentationResult;
	}

	default public ResolveResult resolve(String didString) throws ResolutionException {
		return this.resolve(didString, new HashMap<String, Object>());
	}

	default public ResolveResult resolveRepresentation(String didString) throws ResolutionException {
		return this.resolveRepresentation(didString, new HashMap<String, Object>());
	}

	public Map<String, Map<String, Object>> properties() throws ResolutionException;
	public Set<String> methods() throws ResolutionException;
	public Map<String, List<String>> testIdentifiers() throws ResolutionException;
}
