package uniresolver.local.extensions;

import foundation.identity.did.DID;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.ResolveResult;

import java.util.Map;

public interface ResolverExtension {

	default ExtensionStatus beforeResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, Map<String, Object> executionStatus, LocalUniResolver localUniResolver) throws ResolutionException {
		return null;
	}

	default ExtensionStatus afterResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, Map<String, Object> executionStatus, LocalUniResolver localUniResolver) throws ResolutionException {
		return null;
	}

	abstract class AbstractResolverExtension implements ResolverExtension {
	}
}
