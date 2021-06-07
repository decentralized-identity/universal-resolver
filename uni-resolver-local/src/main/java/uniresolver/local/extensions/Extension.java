package uniresolver.local.extensions;

import foundation.identity.did.DID;
import foundation.identity.did.DIDURL;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.ResolveResult;

import java.util.Map;

public interface Extension {

	public ExtensionStatus beforeResolve(String identifier, DIDURL didUrl, DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException;
	public ExtensionStatus afterResolve(String identifier, DIDURL didUrl, DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException;

	public abstract static class AbstractExtension implements Extension {

		@Override
		public ExtensionStatus beforeResolve(String identifier, DIDURL didUrl, DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException {

			return null;
		}

		@Override
		public ExtensionStatus afterResolve(String identifier, DIDURL didUrl, DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException {

			return null;
		}
	}
}
