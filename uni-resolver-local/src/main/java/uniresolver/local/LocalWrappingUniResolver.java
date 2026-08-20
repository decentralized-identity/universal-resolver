package uniresolver.local;

import foundation.identity.did.DID;
import foundation.identity.did.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.ResolverExtension;
import uniresolver.local.extensions.util.ExecutionStateUtil;
import uniresolver.result.ResolveResult;

import java.util.*;

public class LocalWrappingUniResolver implements UniResolver {

	public static final List<ResolverExtension> DEFAULT_EXTENSIONS = List.of(
	);

	private static final Logger log = LoggerFactory.getLogger(LocalWrappingUniResolver.class);

	private UniResolver uniResolver;
	private List<ResolverExtension> extensions = new ArrayList<>(DEFAULT_EXTENSIONS);

	public LocalWrappingUniResolver() {

	}

	public LocalWrappingUniResolver(UniResolver uniResolver) {
		this.uniResolver = uniResolver;
	}

	/*
	 * Resolver methods
	 */

	@Override
	public ResolveResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		return this.resolve(didString, resolutionOptions, null);
	}

	public ResolveResult resolve(String didString, Map<String, Object> resolutionOptions, Map<String, Object> initialExecutionState) throws ResolutionException {

		if (log.isDebugEnabled()) log.debug("resolve(" + didString + ") with options: " + resolutionOptions);

		if (didString == null) throw new NullPointerException();
		if (this.getUniResolver() == null) throw new ResolutionException("No Universal Resolver configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare resolve result

		final DID did;
		final ResolveResult resolveResult = ResolveResult.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// parse

		try {

			did = DID.fromString(didString);
			if (log.isDebugEnabled()) log.debug("DID " + didString + " is valid: " + did);
		} catch (IllegalArgumentException | ParserException ex) {

			String errorMessage = ex.getMessage();
			if (log.isWarnEnabled()) log.warn(errorMessage);
			throw new ResolutionException(ResolutionException.ERROR_INVALID_DID, errorMessage);
		}

		// [before resolve]

		this.executeExtensions(ResolverExtension.BeforeResolveResolverExtension.class, extensionStatus, e -> e.beforeResolve(did, resolutionOptions, resolveResult, executionState, this), resolutionOptions, resolveResult, executionState);

		// [resolve]

		if (! extensionStatus.skipResolve()) {

			if (log.isInfoEnabled()) log.info("Resolving DID: " + did);

			long driverStart = System.currentTimeMillis();
			ResolveResult driverResolveResult = this.resolveWithUniResolver(did, resolutionOptions);
			long driverStop = System.currentTimeMillis();
			resolveResult.getDidResolutionMetadata().put("driverDuration", driverStop - driverStart);

			if (driverResolveResult == null) {
				if (log.isInfoEnabled()) log.info("Method not supported: " + did.getMethodName());
				throw new ResolutionException(ResolutionException.ERROR_METHOD_NOT_SUPPORTED, "Method not supported: " + did.getMethodName());
			}

			resolveResult.setDidDocument(driverResolveResult.getDidDocument());
			if (driverResolveResult.getDidResolutionMetadata() != null) resolveResult.getDidResolutionMetadata().putAll(driverResolveResult.getDidResolutionMetadata());
			if (driverResolveResult.getDidDocumentMetadata() != null) resolveResult.getDidDocumentMetadata().putAll(driverResolveResult.getDidDocumentMetadata());
		}

		// incomplete result?

		if (! resolveResult.isComplete()) {
			if (log.isInfoEnabled()) log.info("Resolve result is incomplete: " + resolveResult);
			throw new ResolutionException(ResolutionException.ERROR_NOT_FOUND, "No resolve result for " + didString);
		}

		// [after resolve]

		this.executeExtensions(ResolverExtension.AfterResolveResolverExtension.class, extensionStatus, e -> e.afterResolve(did, resolutionOptions, resolveResult, executionState, this), resolutionOptions, resolveResult, executionState);

		// additional metadata

		long stop = System.currentTimeMillis();
		resolveResult.getDidResolutionMetadata().put("duration", stop - start);
		resolveResult.getDidResolutionMetadata().put("did", did.toMap(false));

		// done

		if (log.isInfoEnabled()) log.info("Final resolve result: " + resolveResult);
		return resolveResult;
	}

	public ResolveResult resolveWithUniResolver(DID did, Map<String, Object> resolutionOptions) throws ResolutionException {

		ResolveResult uniResolverResolveResult;

		UniResolver uniResolver = this.getUniResolver();
		if (log.isInfoEnabled()) log.info("Executing resolve " + did + " with Universal Resolver " + uniResolver.getClass().getSimpleName());

		uniResolverResolveResult = uniResolver.resolve(did.getDidString(), resolutionOptions);
		if (uniResolverResolveResult == null) return null;

		return uniResolverResolveResult;
	}

	private <E extends ResolverExtension> void executeExtensions(Class<E> extensionClass, ExtensionStatus extensionStatus, ResolverExtension.ExtensionFunction<E> extensionFunction, Map<String, Object> resolutionOptions, ResolveResult resolveResult, Map<String, Object> executionState) throws ResolutionException {

		String extensionStage = extensionClass.getAnnotation(ResolverExtension.ExtensionStage.class).value();

		List<E> extensions = this.getExtensions().stream().filter(extensionClass::isInstance).map(extensionClass::cast).toList();
		if (log.isDebugEnabled()) log.debug("EXTENSIONS (" + extensionStage + "), TRYING: {}", ResolverExtension.extensionClassNames(extensions));

		List<ResolverExtension> skippedExtensions = new ArrayList<>();
		List<ResolverExtension> inapplicableExtensions = new ArrayList<>();

		for (E extension : extensions) {
			if (extensionStatus.skip(extensionStage)) { skippedExtensions.add(extension); continue; }
			String beforeResolutionOptions = "" + resolutionOptions;
			String beforeResolveResult = "" + resolveResult;
			String beforeExecutionState = "" + executionState;
			ExtensionStatus returnedExtensionStatus = extensionFunction.apply(extension);
			extensionStatus.or(returnedExtensionStatus);
			if (returnedExtensionStatus == null) { inapplicableExtensions.add(extension); continue; }
			String afterResolutionOptions = "" + resolutionOptions;
			String afterResolveResult = "" + resolveResult;
			String afterExecutionState = "" + executionState;
			String changedResolutionOptions = afterResolutionOptions.equals(beforeResolutionOptions) ? "(unchanged)" : afterResolutionOptions;
			String changedResolveResult = afterResolveResult.equals(beforeResolveResult) ? "(unchanged)" : afterResolveResult;
			String changedExecutionState = afterExecutionState.equals(beforeExecutionState) ? "(unchanged)" : afterExecutionState;
			if (log.isDebugEnabled()) log.debug("Executed extension (" + extensionStage + ") " + extension.getClass().getSimpleName() + " with resolution options " + changedResolutionOptions + " and resolve result " + changedResolveResult + " and execution state " + changedExecutionState);
			ExecutionStateUtil.addResolverExtensionStage(executionState, extensionClass, extension);
		}

		if (log.isDebugEnabled()) {
			List<E> executedExtensions = extensions.stream().filter(e -> ! skippedExtensions.contains(e)).filter(e -> ! inapplicableExtensions.contains(e)).toList();
			log.debug("EXTENSIONS (" + extensionStage + "), EXECUTED: {}, SKIPPED: {}, INAPPLICABLE: {}", ResolverExtension.extensionClassNames(executedExtensions), ResolverExtension.extensionClassNames(skippedExtensions), ResolverExtension.extensionClassNames(inapplicableExtensions));
		}
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws ResolutionException {
		if (this.getUniResolver() == null) throw new ResolutionException("No Universal Registrar configured.");
		return this.getUniResolver().properties();
	}

	@Override
	public Set<String> methods() throws ResolutionException {
		if (this.getUniResolver() == null) throw new ResolutionException("No Universal Registrar configured.");
		return this.getUniResolver().methods();
	}


	@Override
	public Map<String, List<String>> testIdentifiers() throws ResolutionException {
		if (this.getUniResolver() == null) throw new ResolutionException("No Universal Registrar configured.");
		return this.getUniResolver().testIdentifiers();
	}

	@Override
	public Map<String, Map<String, Object>> traits() throws ResolutionException {
		if (this.getUniResolver() == null) throw new ResolutionException("No Universal Registrar configured.");
		return this.getUniResolver().traits();
	}

	/*
	 * Getters and setters
	 */

	public UniResolver getUniResolver() {
		return uniResolver;
	}

	public void setUniResolver(UniResolver uniResolver) {
		this.uniResolver = uniResolver;
	}

	public List<ResolverExtension> getExtensions() {
		return this.extensions;
	}

	public void setExtensions(List<ResolverExtension> extensions) {
		this.extensions = extensions;
	}
}
