package uniresolver.driver;

import foundation.identity.did.DID;
import foundation.identity.did.DIDURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Driver {

	public static final String PROPERTIES_MEDIA_TYPE = "application/json";
	public static final String TEST_IDENTIFIER_MEDIA_TYPE = "application/json";
	public static final String TRAITS_MEDIA_TYPE = "application/json";

	static final Logger log = LoggerFactory.getLogger(Driver.class);

	public ResolveResult resolve(DID did, Map<String, Object> resolutionOptions) throws ResolutionException;

	public DereferenceResult dereference(DIDURL didUrl, Map<String, Object> dereferenceOptions) throws DereferencingException, ResolutionException;

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
