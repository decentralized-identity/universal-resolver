package uniresolver.local.extensions;

import java.util.Map;

import foundation.identity.did.DIDURL;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.ResolveResult;

public interface Extension {

	public ExtensionStatus beforeResolve(String identifier, DIDURL didUrl, Map<String, String> options, ResolveResult resolveResult, LocalUniResolver localUniResolver) throws ResolutionException;
	public ExtensionStatus afterResolve(String identifier, DIDURL didUrl, Map<String, String> options, ResolveResult resolveResult, LocalUniResolver localUniResolver) throws ResolutionException;

	public abstract static class AbstractExtension implements Extension {

		@Override
		public ExtensionStatus beforeResolve(String identifier, DIDURL didUrl, Map<String, String> options, ResolveResult resolveResult, LocalUniResolver localUniResolver) throws ResolutionException {

			return null;
		}

		@Override
		public ExtensionStatus afterResolve(String identifier, DIDURL didUrl, Map<String, String> options, ResolveResult resolveResult, LocalUniResolver localUniResolver) throws ResolutionException {

			return null;
		}
	}
}
