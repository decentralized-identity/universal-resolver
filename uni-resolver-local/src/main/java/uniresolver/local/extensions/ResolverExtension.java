package uniresolver.local.extensions;

import foundation.identity.did.DID;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.ResolveResult;

import java.util.Map;

public interface ResolverExtension {

	interface BeforeResolveResolverExtension extends ResolverExtension {
		default ExtensionStatus beforeResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, Map<String, Object> executionState, LocalUniResolver localUniResolver) throws ResolutionException {
			return null;
		}
	}

	interface AfterResolveResolverExtension extends ResolverExtension {
		default ExtensionStatus afterResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, Map<String, Object> executionState, LocalUniResolver localUniResolver) throws ResolutionException {
			return null;
		}
	}

	abstract class AbstractResolverExtension implements BeforeResolveResolverExtension, AfterResolveResolverExtension {
	}
}
