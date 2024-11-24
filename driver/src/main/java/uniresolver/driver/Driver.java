package uniresolver.driver;

import foundation.identity.did.DID;
import foundation.identity.did.representations.Representations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveRepresentationResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	static final Logger log = LoggerFactory.getLogger(Driver.class);

	default public ResolveDataModelResult resolve(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("Driver: resolve(" + did + ")  with options: " + resolutionOptions);

		String accept = (String) resolutionOptions.get("accept");
		if (accept != null) throw new ResolutionException("Driver: Unexpected 'accept' provided in 'resolutionOptions' for resolve().");

		Map<String, Object> resolveRepresentationResolutionOptions = Map.of("accept", Representations.DEFAULT_MEDIA_TYPE);
		ResolveRepresentationResult resolveRepresentationResult = this.resolveRepresentation(did, resolveRepresentationResolutionOptions);

		return resolveRepresentationResult == null ? null : resolveRepresentationResult.toResolveDataModelResult();
	}

	default public ResolveRepresentationResult resolveRepresentation(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("Driver: resolveRepresentation(" + did + ")  with options: " + resolutionOptions);

		String accept = (String) resolutionOptions.get("accept");

		Map<String, Object> resolveDataModelResolutionOptions = new HashMap<>(resolutionOptions);
		resolveDataModelResolutionOptions.remove("accept");
		ResolveDataModelResult resolveDataModelResult = this.resolve(did, resolveDataModelResolutionOptions);

		return resolveDataModelResult == null ? null : resolveDataModelResult.toResolveRepresentationResult(accept);
	}

	default public Map<String, Object> properties() throws ResolutionException {
		return Collections.emptyMap();
	}

	default public List<String> testIdentifiers() throws ResolutionException {
		return Collections.emptyList();
	}

	default public Map<String, Object> traits() throws ResolutionException {
		return Collections.emptyMap();
	}
}
