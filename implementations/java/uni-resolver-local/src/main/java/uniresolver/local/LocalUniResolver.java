package uniresolver.local;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.did.DID;
import uniresolver.did.parser.ParserException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolutionResult;

public class LocalUniResolver implements UniResolver {

	private static Logger log = LoggerFactory.getLogger(LocalUniResolver.class);

	private static final Map<String, Driver> DEFAULT_DRIVERS = new LinkedHashMap<String, Driver> ();
	private static final LocalUniResolver DEFAULT_RESOLVER = new LocalUniResolver();

	private Map<String, Driver> drivers = DEFAULT_DRIVERS;

	public LocalUniResolver() {

	}

	public static LocalUniResolver getDefault() {

		return DEFAULT_RESOLVER;
	}

	@Override
	public ResolutionResult resolve(String identifier) throws ResolutionException {

		return this.resolve(identifier, null);
	}

	@Override
	public ResolutionResult resolve(String identifier, String selectServiceType) throws ResolutionException {

		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		// parse DID

		DID didReference = null;

		try {

			didReference = DID.fromString(identifier);
			log.debug("identifier " + identifier + " is a valid DID reference: " + didReference.getDid());

			identifier = didReference.getDid();
		} catch (IllegalArgumentException | ParserException ex) {

			log.debug("Identifier " + identifier + " is not a valid DID reference: " + ex.getMessage());
		}

		// start time

		long start = System.currentTimeMillis();

		// try all drivers to resolve identifier

		ResolutionResult resolutionResult = null;
		String usedDriverId = null;
		Driver usedDriver = null;

		for (Entry<String, Driver> driver : this.getDrivers().entrySet()) {

			if (log.isDebugEnabled()) log.debug("Attemping to resolve " + identifier + " with driver " + driver.getValue().getClass());
			resolutionResult = driver.getValue().resolve(identifier);

			if (resolutionResult != null) {

				usedDriverId = driver.getKey();
				usedDriver = driver.getValue();
				break;
			}
		}

		// stop time

		long stop = System.currentTimeMillis();

		// no driver was able to resolve the identifier?

		if (resolutionResult == null) {

			if (log.isDebugEnabled()) log.debug("No resolution result.");
			return null;
		}

		if (resolutionResult.getDidDocument() == null) {

			if (log.isDebugEnabled()) log.debug("No DID document.");
			return null;
		}

		// service selection

		String selectServiceName = didReference != null ? didReference.getService() : null;
		Integer[] selectedServices;

		if (selectServiceName == null && selectServiceType == null) {

			selectedServices = null;
		} else {

			selectedServices = resolutionResult.getDidDocument().selectServices(selectServiceName, selectServiceType);
		}

		// add DID REFERENCE

		if (didReference != null) resolutionResult.setDidReference(didReference);

		// add RESOLVER METADATA

		Map<String, Object> resolverMetadata = new LinkedHashMap<String, Object> ();
		resolverMetadata.put("driverId", usedDriverId);
		resolverMetadata.put("driver", usedDriver.getClass().getSimpleName());
		resolverMetadata.put("duration", Long.valueOf(stop - start));
		if (selectedServices != null) resolverMetadata.put("selectedServices", selectedServices);

		resolutionResult.setResolverMetadata(resolverMetadata);

		// done

		return resolutionResult;
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
