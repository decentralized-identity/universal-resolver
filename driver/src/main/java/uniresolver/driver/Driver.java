package uniresolver.driver;

import foundation.identity.did.DID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;
import uniresolver.util.ResolveResultUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	static final Logger log = LoggerFactory.getLogger(Driver.class);

	default public ResolveResult resolve(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("Driver: resolve(" + did + ")  with options: " + resolutionOptions);
		ResolveResult resolveRepresentationResult = this.resolveRepresentation(did, resolutionOptions);
		ResolveResult resolveResult = resolveRepresentationResult == null ? null : ResolveResultUtil.convertToResolveResult(resolveRepresentationResult);
		return resolveResult;
	}

	default public ResolveResult resolveRepresentation(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("Driver: resolveRepresentation(" + did + ")  with options: " + resolutionOptions);
		String accept = (String) resolutionOptions.get("accept");
		if (accept == null) throw new ResolutionException("Driver: No 'accept' provided in 'resolutionOptions' for resolveRepresentation().");
		ResolveResult resolveResult = this.resolve(did, resolutionOptions);
		ResolveResult resolveRepresentationResult = resolveResult == null ? null : ResolveResultUtil.convertToResolveRepresentationResult(resolveResult, accept);
		return resolveRepresentationResult;
	}

	default public Map<String, Object> properties() throws ResolutionException {
		return Collections.emptyMap();
	}

	default public List<String> testIdentifiers() throws ResolutionException {
		return Collections.emptyList();
	}
}
