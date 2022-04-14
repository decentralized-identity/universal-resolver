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

public interface DereferencerExtension {

	default ExtensionStatus beforeDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionStatus, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
		return null;
	}

	default ExtensionStatus dereferencePrimary(DIDURL didUrlWithoutFragment, Map<String, Object> dereferenceOptions, ResolveResult resolveResult, DereferenceResult dereferenceResult, Map<String, Object> executionStatus, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
		return null;
	}

	default ExtensionStatus dereferenceSecondary(DIDURL didUrlWithoutFragment, String didUrlFragment, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionStatus, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
		return null;
	}

	default ExtensionStatus afterDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionStatus, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
		return null;
	}

	abstract class AbstractDereferencerExtension implements DereferencerExtension {
	}
}
