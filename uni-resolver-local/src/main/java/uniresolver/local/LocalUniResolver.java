package uniresolver.local;

import foundation.identity.did.DID;
import foundation.identity.did.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.driver.Driver;
import uniresolver.driver.http.HttpDriver;
import uniresolver.local.configuration.LocalUniResolverConfigurator;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.ResolverExtension;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.util.*;

public class LocalUniResolver implements UniResolver {

	public static final List<ResolverExtension> DEFAULT_EXTENSIONS = List.of(
	);

	private static final Logger log = LoggerFactory.getLogger(LocalUniResolver.class);

	private List<Driver> drivers = new ArrayList<>();
	private List<ResolverExtension> extensions = new ArrayList<>(DEFAULT_EXTENSIONS);

	public LocalUniResolver() {

	}

	public LocalUniResolver(List<Driver> drivers) {
		this.drivers = drivers;
	}

	/*
	 * Factory methods
	 */

	public static LocalUniResolver fromConfigFile(String filePath) throws IOException {

		LocalUniResolver localUniResolver = new LocalUniResolver();
		LocalUniResolverConfigurator.configureLocalUniResolver(filePath, localUniResolver);

		return localUniResolver;
	}

	/*
	 * Resolver methods
	 */

	@Override
	public ResolveDataModelResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		return this.resolve(didString, resolutionOptions, null);
	}

	public ResolveDataModelResult resolve(String didString, Map<String, Object> resolutionOptions, Map<String, Object> initialExecutionState) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("resolve(" + didString + ")  with options: " + resolutionOptions);
		return (ResolveDataModelResult) this.resolveOrResolveRepresentation(didString, resolutionOptions, false, initialExecutionState);
	}

	@Override
	public ResolveRepresentationResult resolveRepresentation(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		return this.resolveRepresentation(didString, resolutionOptions, null);
	}

	public ResolveRepresentationResult resolveRepresentation(String didString, Map<String, Object> resolutionOptions, Map<String, Object> initialExecutionState) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("resolveRepresentation(" + didString + ")  with options: " + resolutionOptions);
		return (ResolveRepresentationResult) this.resolveOrResolveRepresentation(didString, resolutionOptions, true, initialExecutionState);
	}

	private ResolveResult resolveOrResolveRepresentation(String didString, Map<String, Object> resolutionOptions, boolean resolveRepresentation, Map<String, Object> initialExecutionState) throws ResolutionException {

		if (didString == null) throw new NullPointerException();
		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare resolve result

		final DID did;
		final ResolveResult resolveResult = resolveRepresentation ? ResolveRepresentationResult.build() : ResolveDataModelResult.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// parse

		try {

			did = DID.fromString(didString);
			if (log.isDebugEnabled()) log.debug("DID " + didString + " is valid: " + did);
		} catch (IllegalArgumentException | ParserException ex) {

			String errorMessage = ex.getMessage();
			if (log.isWarnEnabled()) log.warn(errorMessage);
			throw new ResolutionException(ResolutionException.ERROR_INVALIDDID, errorMessage);
		}

		// check options

		String accept = (String) resolutionOptions.get("accept");

		// [before resolve]

		this.executeExtensions(ResolverExtension.BeforeResolveResolverExtension.class, extensionStatus, e -> e.beforeResolve(did, resolutionOptions, resolveResult, resolveRepresentation, executionState, this), resolutionOptions, resolveResult, executionState);

		// [resolve]

		if (! extensionStatus.skipResolve()) {

			if (log.isInfoEnabled()) log.info("Resolving DID: " + did);

			ResolveResult driverResolveResult = this.resolveOrResolveRepresentationWithDrivers(did, resolutionOptions, resolveRepresentation);
			if (driverResolveResult != null) {
				resolveResult.getDidResolutionMetadata().putAll(driverResolveResult.getDidResolutionMetadata());
				if (resolveResult instanceof ResolveDataModelResult) ((ResolveDataModelResult) resolveResult).setDidDocument(((ResolveDataModelResult) driverResolveResult).getDidDocument());
				if (resolveResult instanceof ResolveRepresentationResult) ((ResolveRepresentationResult) resolveResult).setDidDocumentStream(((ResolveRepresentationResult) driverResolveResult).getDidDocumentStream());
				resolveResult.getDidDocumentMetadata().putAll(driverResolveResult.getDidDocumentMetadata());
			}
		}

		// nothing found?

		if (! resolveResult.isComplete()) {
			if (log.isInfoEnabled()) log.info("Resolve result is incomplete: " + resolveResult);
			throw new ResolutionException(ResolutionException.ERROR_NOTFOUND, "No resolve result for " + didString);
		}

		// [after resolve]

		this.executeExtensions(ResolverExtension.AfterResolveResolverExtension.class, extensionStatus, e -> e.afterResolve(did, resolutionOptions, resolveResult, resolveRepresentation, executionState, this), resolutionOptions, resolveResult, executionState);

		// additional metadata

		long stop = System.currentTimeMillis();
		resolveResult.getDidResolutionMetadata().put("duration", stop - start);
		resolveResult.getDidResolutionMetadata().put("did", did.toMap(false));

		// done

		final ResolveResult finalResolveResult = resolveRepresentation ? resolveResult.toResolveRepresentationResult(accept) : resolveResult.toResolveDataModelResult();
		if (log.isInfoEnabled()) log.info("Final resolve result: " + finalResolveResult + " (" + resolveRepresentation + ", " + finalResolveResult.getClass().getSimpleName() + ")");

		return finalResolveResult;
	}

	public ResolveResult resolveOrResolveRepresentationWithDrivers(DID did, Map<String, Object> resolutionOptions, boolean resolveRepresentation) throws ResolutionException {

		ResolveResult driverResolveResult = null;
		Driver usedDriver = null;

		for (Driver driver : this.getDrivers()) {

			if (log.isDebugEnabled()) log.debug("Attempting to resolve " + did + " with driver " + driver.getClass().getSimpleName());

			if (resolveRepresentation)
				driverResolveResult = driver.resolveRepresentation(did, resolutionOptions);
			else
				driverResolveResult = driver.resolve(did, resolutionOptions);

			if (driverResolveResult != null) {
				usedDriver = driver;
				break;
			}
		}

		if (usedDriver == null) {

			if (log.isInfoEnabled()) log.info("Method not supported: " + did.getMethodName());
			throw new ResolutionException(ResolutionException.ERROR_METHODNOTSUPPORTED, "Method not supported: " + did.getMethodName());
		}

		if (usedDriver instanceof HttpDriver) {

			driverResolveResult.getDidResolutionMetadata().put("pattern", ((HttpDriver) usedDriver).getPattern().pattern());
			driverResolveResult.getDidResolutionMetadata().put("driverUrl", ((HttpDriver) usedDriver).getResolveUri());

			if (log.isDebugEnabled()) log.debug("Resolved " + did + " with driver " + usedDriver.getClass().getSimpleName() + " and pattern " + ((HttpDriver) usedDriver).getPattern().pattern());
		} else {

			if (log.isDebugEnabled()) log.debug("Resolved " + did + " with driver " + usedDriver.getClass().getSimpleName());
		}

		return driverResolveResult;
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
		}

		if (log.isDebugEnabled()) {
			List<E> executedExtensions = extensions.stream().filter(e -> ! skippedExtensions.contains(e)).filter(e -> ! inapplicableExtensions.contains(e)).toList();
			log.debug("EXTENSIONS (" + extensionStage + "), EXECUTED: {}, SKIPPED: {}, INAPPLICABLE: {}", ResolverExtension.extensionClassNames(executedExtensions), ResolverExtension.extensionClassNames(skippedExtensions), ResolverExtension.extensionClassNames(inapplicableExtensions));
		}
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws ResolutionException {

		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		Map<String, Map<String, Object>> properties = new LinkedHashMap<String, Map<String, Object>> ();

		int i = 0;

		for (Driver driver : this.getDrivers()) {

			if (log.isDebugEnabled()) log.debug("Loading properties for driver " + driver.getClass().getSimpleName());

			String driverKey = "driver-" + i;
			Map<String, Object> driverProperties = driver.properties();
			if (driverProperties == null) driverProperties = Collections.emptyMap();

			properties.put(driverKey, driverProperties);

			i++;
		}

		// done

		if (log.isDebugEnabled()) log.debug("Loaded properties: " + properties);
		return properties;
	}

	@Override
	public Set<String> methods() throws ResolutionException {

		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		Set<String> methods = this.testIdentifiers().keySet();

		// done

		if (log.isDebugEnabled()) log.debug("Loaded methods: " + methods);
		return methods;
	}

	@Override
	public Map<String, List<String>> testIdentifiers() throws ResolutionException {

		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		Map<String, List<String>> testIdentifiers = new LinkedHashMap<String, List<String>> ();

		for (Driver driver : this.getDrivers()) {

			if (log.isDebugEnabled()) log.debug("Loading test identifiers for driver " + driver.getClass().getSimpleName());

			List<String> driverTestIdentifiers = driver.testIdentifiers();
			if (driverTestIdentifiers == null) driverTestIdentifiers = Collections.emptyList();

			for (String driverTestIdentifier : driverTestIdentifiers) {

				String driverTestIdentifierMethod = driverTestIdentifier.substring("did:".length());
				driverTestIdentifierMethod = driverTestIdentifierMethod.substring(0, driverTestIdentifierMethod.indexOf(':'));

				List<String> methodTestIdentifiers = testIdentifiers.get(driverTestIdentifierMethod);

				if (methodTestIdentifiers == null) {

					methodTestIdentifiers = new ArrayList<String> ();
					testIdentifiers.put(driverTestIdentifierMethod, methodTestIdentifiers);
				}

				methodTestIdentifiers.add(driverTestIdentifier);
			}
		}

		// done

		if (log.isDebugEnabled()) log.debug("Loaded test identifiers: " + testIdentifiers);
		return testIdentifiers;
	}

	/*
	 * Getters and setters
	 */

	public List<Driver> getDrivers() {
		return this.drivers;
	}

	@SuppressWarnings("unchecked")
	public <T extends Driver> T getDriver(Class<T> driverClass) {
		for (Driver driver : this.getDrivers()) {
			if (driverClass.isAssignableFrom(driver.getClass())) return (T) driver;
		}
		return null;
	}

	public void setDrivers(List<Driver> drivers) {
		this.drivers = drivers;
	}

	public List<ResolverExtension> getExtensions() {
		return this.extensions;
	}

	public void setExtensions(List<ResolverExtension> extensions) {
		this.extensions = extensions;
	}
}
