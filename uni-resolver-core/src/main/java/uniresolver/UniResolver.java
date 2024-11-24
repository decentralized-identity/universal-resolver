package uniresolver;

import foundation.identity.did.representations.Representations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.w3c.DIDResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UniResolver extends DIDResolver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";
	public static final String METHODS_MIME_TYPE = "application/json";
	public static final String TEST_IDENTIFIER_MIME_TYPE = "application/json";
	public static final String TRAITS_MIME_TYPE = "application/json";

	static final Logger log = LoggerFactory.getLogger(UniResolver.class);

	@Override
	default public ResolveDataModelResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("resolve(" + didString + ")  with options: " + resolutionOptions);

		if (didString == null) throw new NullPointerException();
		if (resolutionOptions == null) resolutionOptions = new HashMap<>();

		String accept = (String) resolutionOptions.get("accept");
		if (accept != null) throw new ResolutionException("Unexpected 'accept' provided in 'resolutionOptions' for resolve().");

		Map<String, Object> resolveRepresentationResolutionOptions = Map.of("accept", Representations.DEFAULT_MEDIA_TYPE);

		ResolveRepresentationResult resolveRepresentationResult = this.resolveRepresentation(didString, resolveRepresentationResolutionOptions);

		return resolveRepresentationResult == null ? null : resolveRepresentationResult.toResolveDataModelResult();
	}

	@Override
	default public ResolveRepresentationResult resolveRepresentation(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("resolveRepresentation(" + didString + ")  with options: " + resolutionOptions);

		if (didString == null) throw new NullPointerException();
		if (resolutionOptions == null) resolutionOptions = new HashMap<>();

		String accept = (String) resolutionOptions.get("accept");

		Map<String, Object> resolveDataModelResolutionOptions = new HashMap<>(resolutionOptions);
		resolveDataModelResolutionOptions.remove("accept");

		ResolveDataModelResult resolveDataModelResult = this.resolve(didString, resolveDataModelResolutionOptions);

		return resolveDataModelResult == null ? null : resolveDataModelResult.toResolveRepresentationResult(accept);
	}

	default public ResolveDataModelResult resolve(String didString) throws ResolutionException {
		return this.resolve(didString, new HashMap<>());
	}

	default public ResolveRepresentationResult resolveRepresentation(String didString) throws ResolutionException {
		return this.resolveRepresentation(didString, new HashMap<>());
	}

	public Map<String, Map<String, Object>> properties() throws ResolutionException;
	public Set<String> methods() throws ResolutionException;
	public Map<String, List<String>> testIdentifiers() throws ResolutionException;
	public Map<String, Map<String, Object>> traits() throws ResolutionException;
}
