package uniresolver.local.extensions;

import foundation.identity.did.DIDURL;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniDereferencer;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

public interface DereferencerExtension {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface ExtensionStage {
		String value();
	}

	@FunctionalInterface
	interface ExtensionFunction<E extends DereferencerExtension> {
		ExtensionStatus apply(E extension) throws ResolutionException, DereferencingException;
	}

	@ExtensionStage("beforeDereference")
	interface BeforeDereferenceDereferencerExtension extends DereferencerExtension {
		default ExtensionStatus beforeDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
			return null;
		}
	}

	@ExtensionStage("dereferencePrimary")
	interface DereferencePrimaryDereferencerExtension extends DereferencerExtension {
		default ExtensionStatus dereferencePrimary(DIDURL didUrlWithoutFragment, Map<String, Object> dereferenceOptions, ResolveResult resolveResult, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
			return null;
		}
	}

	@ExtensionStage("dereferenceSecondary")
	interface DereferenceSecondaryDereferencerExtension extends DereferencerExtension {
		default ExtensionStatus dereferenceSecondary(DIDURL didUrlWithoutFragment, String didUrlFragment, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
			return null;
		}
	}

	@ExtensionStage("afterDereference")
	interface AfterDereferenceDereferencerExtension extends DereferencerExtension {
		default ExtensionStatus afterDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {
			return null;
		}
	}

	abstract class AbstractDereferencerExtension implements BeforeDereferenceDereferencerExtension, DereferencePrimaryDereferencerExtension, DereferenceSecondaryDereferencerExtension, AfterDereferenceDereferencerExtension {
	}

	static List<String> extensionClassNames(List<? extends DereferencerExtension> extensions) {
		return extensions.stream().map(e -> e.getClass().getSimpleName()).toList();
	}
}
