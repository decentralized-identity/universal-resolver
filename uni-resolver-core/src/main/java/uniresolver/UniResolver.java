package uniresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.ResolveResult;
import uniresolver.w3c.DIDResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UniResolver extends DIDResolver {

	public static final String PROPERTIES_MEDIA_TYPE = "application/json";
	public static final String METHODS_MEDIA_TYPE = "application/json";
	public static final String TEST_IDENTIFIER_MEDIA_TYPE = "application/json";
	public static final String TRAITS_MEDIA_TYPE = "application/json";

	static final Logger log = LoggerFactory.getLogger(UniResolver.class);

	@Override public ResolveResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException;

	default public ResolveResult resolve(String didString) throws ResolutionException {
		return this.resolve(didString, new HashMap<>());
	}

	public Map<String, Map<String, Object>> properties() throws ResolutionException;
	public Set<String> methods() throws ResolutionException;
	public Map<String, List<String>> testIdentifiers() throws ResolutionException;
	public Map<String, Map<String, Object>> traits() throws ResolutionException;
}
