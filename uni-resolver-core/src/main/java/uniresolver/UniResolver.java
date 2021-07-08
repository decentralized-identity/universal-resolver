package uniresolver;

import foundation.identity.did.representations.Representations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	static final Logger log = LoggerFactory.getLogger(UniResolver.class);

	@Override
	default public ResolveResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("resolve(" + didString + ")  with options: " + resolutionOptions);
		String accept = (String) resolutionOptions.get("accept");
		if (accept != null) throw new ResolutionException("Unexpected 'accept' provided in 'resolutionOptions' for resolve().");
		resolutionOptions = new HashMap<>(resolutionOptions);
		resolutionOptions.put("accept", Representations.DEFAULT_MEDIA_TYPE);
		ResolveResult resolveRepresentationResult = null;
		try {
			resolveRepresentationResult = this.resolveRepresentation(didString, resolutionOptions);
		} catch (ResolutionException ex) {
			if (ex.getResolveResult() != null) ex.setResolveResult(ResolveResultUtil.convertToResolveResult(ex.getResolveResult()));
			throw ex;
		}
		ResolveResult resolveResult = resolveRepresentationResult == null ? null : ResolveResultUtil.convertToResolveResult(resolveRepresentationResult);
		return resolveResult;
	}

	@Override
	default public ResolveResult resolveRepresentation(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("resolveRepresentation(" + didString + ")  with options: " + resolutionOptions);
		String accept = (String) resolutionOptions.get("accept");
		if (accept == null) throw new ResolutionException("No 'accept' provided in 'resolutionOptions' for resolveRepresentation().");
		resolutionOptions = new HashMap<>(resolutionOptions);
		ResolveResult resolveResult;
		try {
			resolveResult = this.resolve(didString, resolutionOptions);
		} catch (ResolutionException ex) {
			if (ex.getResolveResult() != null) ex.setResolveResult(ResolveResultUtil.convertToResolveRepresentationResult(ex.getResolveResult(), accept));
			throw ex;
		}
		ResolveResult resolveRepresentationResult = resolveResult == null ? null : ResolveResultUtil.convertToResolveRepresentationResult(resolveResult, accept);
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
