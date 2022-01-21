package uniresolver.local.extensions;

import foundation.identity.did.DID;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.ResolveResult;

import java.util.Map;

public interface ResolverExtension {

	public default ExtensionStatus beforeResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException {
		return null;
	}

	public default ExtensionStatus afterResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException {
		return null;
	}

	public abstract static class AbstractResolverExtension implements ResolverExtension {
	}
}
