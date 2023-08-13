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

	interface BeforeDereferenceDereferencerExtension extends DereferencerExtension {
		default ExtensionStatus beforeDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
			return null;
		}
	}

	interface DereferencePrimaryDereferencerExtension extends DereferencerExtension {
		default ExtensionStatus dereferencePrimary(DIDURL didUrlWithoutFragment, Map<String, Object> dereferenceOptions, ResolveResult resolveResult, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
			return null;
		}
	}

	interface DereferenceSecondaryDereferencerExtension extends DereferencerExtension {
		default ExtensionStatus dereferenceSecondary(DIDURL didUrlWithoutFragment, String didUrlFragment, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
			return null;
		}
	}

	interface AfterDereferenceDereferencerExtension extends DereferencerExtension {
		default ExtensionStatus afterDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
			return null;
		}
	}

	abstract class AbstractDereferencerExtension implements BeforeDereferenceDereferencerExtension, DereferencePrimaryDereferencerExtension, DereferenceSecondaryDereferencerExtension, AfterDereferenceDereferencerExtension {
	}
}
