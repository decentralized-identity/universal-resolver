package uniresolver.local;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniresolver.ResolutionException;
import uniresolver.UniResolver;
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

		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

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

		if (resolutionResult == null) return null;

		// create RESOLVER METADATA

		Map<String, Object> resolverMetadata = new LinkedHashMap<String, Object> ();
		resolverMetadata.put("driverId", usedDriverId);
		resolverMetadata.put("driver", usedDriver.getClass().getSimpleName());
		resolverMetadata.put("duration", Long.valueOf(stop - start));

		resolutionResult.setResolverMetadata(resolverMetadata);

		// done

		return resolutionResult;
	}

	@Override
	public Collection<String> getDriverIds() throws ResolutionException {

		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		return this.getDrivers().keySet();
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
