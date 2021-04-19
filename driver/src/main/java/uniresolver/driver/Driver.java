package uniresolver.driver;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public ResolveResult resolve(String identifier) throws ResolutionException;

	default public Map<String, Object> properties() throws ResolutionException {

		return Collections.emptyMap();
	}

	default public List<String> testIdentifiers() throws ResolutionException {

		return Collections.emptyList();
	}
}
