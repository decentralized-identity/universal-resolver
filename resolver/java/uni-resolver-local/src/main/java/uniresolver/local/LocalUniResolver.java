package uniresolver.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import did.DID;
import did.DIDDocument;
import did.DIDURL;
import did.parser.ParserException;
import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

public class LocalUniResolver implements UniResolver {

	private static Logger log = LoggerFactory.getLogger(LocalUniResolver.class);

	private static final LocalUniResolver DEFAULT_RESOLVER;

	private Map<String, Driver> drivers = new HashMap<String, Driver> ();

	static {

		DEFAULT_RESOLVER = new LocalUniResolver();
	}

	public LocalUniResolver() {

	}

	public static LocalUniResolver getDefault() {

		return DEFAULT_RESOLVER;
	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {

		return this.resolve(identifier, null);
	}

	@Override
	public ResolveResult resolve(String identifier, Map<String, String> options) throws ResolutionException {

		if (identifier == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// parse DID URL

		DIDURL didUrl = null;
		DID did = null;

		try {

			didUrl = DIDURL.fromString(identifier);
			log.debug("Identifier " + identifier + " is a valid DID URL: " + didUrl.getDid());

			did = didUrl.getDid();
		} catch (IllegalArgumentException | ParserException ex) {

			log.debug("Identifier " + identifier + " is not a valid DID URL: " + ex.getMessage());
		}

		// resolve earlier version?

		String resolveIdentifier = did != null ? did.getDidString() : identifier;
		ResolveResult resolveResult = null;

		// try all drivers

		String usedDriverId = null;
		Driver usedDriver = null;

		if (resolveResult == null) {

			for (Entry<String, Driver> driver : this.getDrivers().entrySet()) {

				if (log.isDebugEnabled()) log.debug("Attemping to resolve " + resolveIdentifier + " with driver " + driver.getValue().getClass());
				resolveResult = driver.getValue().resolve(resolveIdentifier);

				if (resolveResult != null) {

					usedDriverId = driver.getKey();
					usedDriver = driver.getValue();
					break;
				}
			}

			if (log.isDebugEnabled()) log.debug("Resolved " + resolveIdentifier + " with driver " + usedDriverId);
		}

		// result contains a new identifier to resolve (redirect)?

		List<String> redirectedIdentifiers = new ArrayList<String> ();
		List<ResolveResult> redirectedResolveResults = new ArrayList<ResolveResult> ();

		while (resolveResult != null && resolveResult.getMethodMetadata().containsKey("redirect")) {

			redirectedIdentifiers.add(resolveIdentifier);
			redirectedResolveResults.add(resolveResult);

			resolveIdentifier = (String) resolveResult.getMethodMetadata().get("redirect");

			for (Entry<String, Driver> driver : this.getDrivers().entrySet()) {

				if (log.isDebugEnabled()) log.debug("Attemping to resolve " + identifier + " with driver " + driver.getValue().getClass());
				resolveResult = driver.getValue().resolve(identifier);

				if (resolveResult != null) {

					usedDriverId = driver.getKey();
					usedDriver = driver.getValue();
					break;
				}
			}

			if (log.isDebugEnabled()) log.debug("Resolved " + identifier + " with driver " + usedDriverId);
		}

		if (redirectedIdentifiers.isEmpty()) redirectedIdentifiers = null;
		if (redirectedResolveResults.isEmpty()) redirectedResolveResults = null;

		if (resolveResult == null && redirectedResolveResults != null && redirectedIdentifiers != null) {

			resolveIdentifier = redirectedIdentifiers.get(0);
			resolveResult = redirectedResolveResults.get(0);
			if (log.isDebugEnabled()) log.debug("Falling back to redirected identifier and resolve result: " + identifier);
		}

		// stop time

		long stop = System.currentTimeMillis();

		// no driver was able to fulfill a request?

		if (resolveResult == null) {

			if (log.isDebugEnabled()) log.debug("No resolve result.");
			return null;
		}

		// dereferencing

		Integer[] selectedServices = null;
		String selectServiceName = didUrl.getParameters() == null ? null : didUrl.getParameters().get("service");
		String selectServiceType = didUrl.getParameters() == null ? null : didUrl.getParameters().get("service-type");

		if (selectServiceName != null || selectServiceType != null) {

			selectedServices = resolveResult.getDidDocument().selectServices(selectServiceName, selectServiceType).keySet().toArray(new Integer[0]);
		}

		Integer[] selectedKeys = null;
		String selectKeyName = didUrl.getParameters() == null ? null : didUrl.getParameters().get("key");
		String selectKeyType = didUrl.getParameters() == null ? null : didUrl.getParameters().get("key-type");

		if (selectKeyName != null || selectKeyType != null) {

			selectedKeys = resolveResult.getDidDocument().selectKeys(selectKeyName, selectKeyType).keySet().toArray(new Integer[0]);
		}

		// add RESOLVER METADATA

		Map<String, Object> resolverMetadata = new LinkedHashMap<String, Object> ();
		resolverMetadata.put("duration", Long.valueOf(stop - start));
		if (usedDriverId != null) resolverMetadata.put("driverId", usedDriverId);
		if (usedDriver != null) resolverMetadata.put("driver", usedDriver.getClass().getSimpleName());
		if (didUrl != null) resolverMetadata.put("didUrl", didUrl);
		if (redirectedIdentifiers != null) resolverMetadata.put("redirectedIdentifiers", redirectedIdentifiers);
		if (selectedServices != null) resolverMetadata.put("selectedServices", selectedServices);
		if (selectedKeys != null) resolverMetadata.put("selectedKeys", selectedKeys);

		resolveResult.setResolverMetadata(resolverMetadata);

		// done

		return resolveResult;
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws ResolutionException {

		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		Map<String, Map<String, Object>> properties = new HashMap<String, Map<String, Object>> ();

		for (Entry<String, Driver> driver : this.getDrivers().entrySet()) {

			if (log.isDebugEnabled()) log.debug("Loading properties for driver " + driver.getKey() + " (" + driver.getValue().getClass().getSimpleName() + ")");

			Map<String, Object> driverProperties = driver.getValue().properties();
			if (driverProperties == null) driverProperties = Collections.emptyMap();

			properties.put(driver.getKey(), driverProperties);
		}

		if (log.isDebugEnabled()) log.debug("Loading properties: " + properties);

		return properties;
	}

	/*
	 * Getters and setters
	 */

	public Map<String, Driver> getDrivers() {

		return this.drivers;
	}

	@SuppressWarnings("unchecked")
	public <T extends Driver> T getDriver(Class<T> driverClass) {

		for (Driver driver : this.getDrivers().values()) {

			if (driverClass.isAssignableFrom(driver.getClass())) return (T) driver;
		}

		return null;
	}

	public void setDrivers(Map<String, Driver> drivers) {

		this.drivers = drivers;
	}
}
