package uniresolver.local.extensions;

import foundation.identity.did.DID;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.result.ResolveResult;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

public interface ResolverExtension {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface ExtensionStage {
		String value();
	}

	@FunctionalInterface
	interface ExtensionFunction<E extends ResolverExtension> {
		ExtensionStatus apply(E extension) throws ResolutionException;
	}

	@ExtensionStage("beforeResolve")
	interface BeforeResolveResolverExtension extends ResolverExtension {
		default ExtensionStatus beforeResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, Map<String, Object> executionState, LocalUniResolver localUniResolver) throws ResolutionException {
			return null;
		}
	}

	@ExtensionStage("afterResolve")
	interface AfterResolveResolverExtension extends ResolverExtension {
		default ExtensionStatus afterResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, Map<String, Object> executionState, LocalUniResolver localUniResolver) throws ResolutionException {
			return null;
		}
	}

	abstract class AbstractResolverExtension implements BeforeResolveResolverExtension, AfterResolveResolverExtension {
	}

	static List<String> extensionClassNames(List<? extends ResolverExtension> extensions) {
		return extensions.stream().map(e -> e.getClass().getSimpleName()).toList();
	}
}
