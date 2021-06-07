package uniresolver.driver;

import foundation.identity.did.DID;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;
import uniresolver.util.ResolveResultUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	default public ResolveResult resolve(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {
		ResolveResult resolveRepresentationResult = this.resolveRepresentation(did, resolutionOptions);
		ResolveResult resolveResult = ResolveResultUtil.convertToResolveResult(resolveRepresentationResult);
		return resolveResult;
	}

	default public ResolveResult resolveRepresentation(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {
		ResolveResult resolveResult = this.resolve(did, resolutionOptions);
		String accept = (String) resolutionOptions.get("accept");
		if (accept == null) throw new ResolutionException("No 'accept' provided in 'resolutionOptions' for resolveRepresentation().");
		ResolveResult resolveRepresentationResult = ResolveResultUtil.convertToResolveRepresentationResult(resolveResult, accept);
		return resolveRepresentationResult;
	}

	default public Map<String, Object> properties() throws ResolutionException {
		return Collections.emptyMap();
	}

	default public List<String> testIdentifiers() throws ResolutionException {
		return Collections.emptyList();
	}
}
