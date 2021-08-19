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
		if (log.isDebugEnabled()) log.debug("resolve(" + didString + ")  with options: " + resolutionOptions);
		return (ResolveDataModelResult) this.resolveOrResolveRepresentation(didString, resolutionOptions, false);
	}

	@Override
	public ResolveRepresentationResult resolveRepresentation(String didString, Map<String, Object> resolutionOptions) throws ResolutionException {
		if (log.isDebugEnabled()) log.debug("resolveRepresentation(" + didString + ")  with options: " + resolutionOptions);
		return (ResolveRepresentationResult) this.resolveOrResolveRepresentation(didString, resolutionOptions, true);
	}

	private ResolveResult resolveOrResolveRepresentation(String didString, Map<String, Object> resolutionOptions, boolean resolveRepresentation) throws ResolutionException {

		if (didString == null) throw new NullPointerException();
		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare resolve result

		DID did = null;
		ResolveResult resolveResult = resolveRepresentation ? ResolveRepresentationResult.build() : ResolveDataModelResult.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// parse

		try {

			did = DID.fromString(didString);
			if (log.isDebugEnabled()) log.debug("DID " + didString + " is valid: " + did);
		} catch (IllegalArgumentException | ParserException ex) {

			String errorMessage = ex.getMessage();
			if (log.isWarnEnabled()) log.warn(errorMessage);
			throw new ResolutionException(ResolveResult.ERROR_INVALIDDID, errorMessage);
		}

		// check options

		String accept = (String) resolutionOptions.get("accept");

		// [before resolve]

		if (! extensionStatus.skipBeforeResolve()) {
			for (ResolverExtension resolverExtension : this.getExtensions()) {
				extensionStatus.or(resolverExtension.beforeResolve(did, resolutionOptions, resolveResult, resolveRepresentation, this));
				if (extensionStatus.skipBeforeResolve()) break;
			}
		}

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

		// [after resolve]

		if (! extensionStatus.skipAfterResolve()) {
			for (ResolverExtension resolverExtension : this.getExtensions()) {
				extensionStatus.or(resolverExtension.afterResolve(did, resolutionOptions, resolveResult, resolveRepresentation, this));
				if (extensionStatus.skipAfterResolve()) break;
			}
		}

		// additional metadata

		long stop = System.currentTimeMillis();
		resolveResult.getDidResolutionMetadata().put("duration", stop - start);
		resolveResult.getDidResolutionMetadata().put("did", did.toMap(false));

		// nothing found?

		if (! resolveResult.isComplete()) {
			if (log.isInfoEnabled()) log.info("Resolve result is incomplete: " + resolveResult);
			throw new ResolutionException(ResolveResult.ERROR_NOTFOUND, "No resolve result for " + didString, resolveResult.getDidResolutionMetadata());
		}

		// done

		return resolveRepresentation ? resolveResult.toResolveRepresentationResult(accept) : resolveResult.toResolveDataModelResult();
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

		if (usedDriver != null) {

			if (usedDriver instanceof HttpDriver) {

				driverResolveResult.getDidResolutionMetadata().put("pattern", ((HttpDriver) usedDriver).getPattern().pattern());
				driverResolveResult.getDidResolutionMetadata().put("driverUrl", ((HttpDriver) usedDriver).getResolveUri());

				if (log.isDebugEnabled()) log.debug("Resolved " + did + " with driver " + usedDriver.getClass().getSimpleName() + " and pattern " + ((HttpDriver) usedDriver).getPattern().pattern());
			} else {

				if (log.isDebugEnabled()) log.debug("Resolved " + did + " with driver " + usedDriver.getClass().getSimpleName());
			}
		} else {

			if (log.isDebugEnabled()) log.debug("No result with " + this.getDrivers().size() + " drivers.");
		}

		return driverResolveResult;
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
