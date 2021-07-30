package uniresolver.local.extensions;

import foundation.identity.did.DID;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniDereferencer;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.Map;

public interface Extension {

	public ExtensionStatus beforeResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException;
	public ExtensionStatus afterResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException;

	public ExtensionStatus beforeDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, DIDDocument didDocument, LocalUniDereferencer localUniDereferencer) throws DereferencingException;
	public ExtensionStatus afterDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, DIDDocument didDocument, LocalUniDereferencer localUniDereferencer) throws DereferencingException;

	public abstract static class AbstractExtension implements Extension {

		@Override
		public ExtensionStatus beforeResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException {
			return null;
		}

		@Override
		public ExtensionStatus afterResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException {
			return null;
		}

		@Override
		public ExtensionStatus beforeDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, DIDDocument didDocument, LocalUniDereferencer localUniDereferencer) throws DereferencingException {
			return null;
		}

		@Override
		public ExtensionStatus afterDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, DIDDocument didDocument, LocalUniDereferencer localUniDereferencer) throws DereferencingException {
			return null;
		}
	}
}
