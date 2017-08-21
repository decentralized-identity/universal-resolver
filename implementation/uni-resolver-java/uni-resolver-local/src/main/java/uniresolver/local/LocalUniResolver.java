package uniresolver.local;

import java.util.ArrayList;
import java.util.List;

import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.ddo.DDO;
import uniresolver.driver.Driver;
import uniresolver.driver.did.btcr.DidBtcrDriver;
import uniresolver.driver.did.sov.DidSovDriver;

public class LocalUniResolver implements UniResolver {

	private static final List<Driver> DEFAULT_DRIVERS;
	private static final LocalUniResolver DEFAULT_RESOLVER;

	private List<Driver> drivers = DEFAULT_DRIVERS;

	static {

		DEFAULT_DRIVERS = new ArrayList<Driver> ();
		DEFAULT_DRIVERS.add(new DidBtcrDriver());
		DEFAULT_DRIVERS.add(new DidSovDriver());

		DEFAULT_RESOLVER = new LocalUniResolver();
	}

	public LocalUniResolver() {

	}

	public static LocalUniResolver getDefault() {

		return DEFAULT_RESOLVER;
	}

	@Override
	public DDO resolve(String identifier) throws ResolutionException {

		if (this.getDrivers() == null) throw new ResolutionException("No drivers configured.");

		// try all drivers to resolve identifier

		for (Driver driver : this.getDrivers()) {

			DDO result = driver.resolve(identifier);
			if (result != null) return result;
		}

		// no driver was able to resolve the identifier

		return null;
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
}
