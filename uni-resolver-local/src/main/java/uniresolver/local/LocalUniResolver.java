package uniresolver.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.*;

import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import foundation.identity.did.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.driver.Driver;
import uniresolver.driver.http.HttpDriver;
import uniresolver.local.extensions.Extension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.result.ResolveResult;

public class LocalUniResolver implements UniResolver {

	private static Logger log = LoggerFactory.getLogger(LocalUniResolver.class);

	private List<Driver> drivers = new ArrayList<Driver> ();
	private List<Extension> extensions = new ArrayList<Extension> ();

	public LocalUniResolver() {

	}

	public static LocalUniResolver fromConfigFile(String filePath) throws FileNotFoundException, IOException {

		final Gson gson = new Gson();

		List<Driver> drivers = new ArrayList<Driver> ();

		try (Reader reader = new FileReader(new File(filePath))) {

			JsonObject jsonObjectRoot  = gson.fromJson(reader, JsonObject.class);
			JsonArray jsonArrayDrivers = jsonObjectRoot.getAsJsonArray("drivers");

			int i = 0;

			for (Iterator<JsonElement> jsonElementsDrivers = jsonArrayDrivers.iterator(); jsonElementsDrivers.hasNext(); ) {

				i++;

				JsonObject jsonObjectDriver = (JsonObject) jsonElementsDrivers.next();

				String pattern = jsonObjectDriver.has("pattern") ? jsonObjectDriver.get("pattern").getAsString() : null;
				String url = jsonObjectDriver.has("url") ? jsonObjectDriver.get("url").getAsString() : null;
				String propertiesEndpoint = jsonObjectDriver.has("propertiesEndpoint") ? jsonObjectDriver.get("propertiesEndpoint").getAsString() : null;
				JsonArray testIdentifiers = jsonObjectDriver.has("testIdentifiers") ? jsonObjectDriver.get("testIdentifiers").getAsJsonArray() : null;

				if (pattern == null) throw new IllegalArgumentException("Missing 'pattern' entry in driver configuration.");
				if (url == null) throw new IllegalArgumentException("Missing 'url' entry in driver configuration.");

				// construct HTTP driver

				HttpDriver driver = new HttpDriver();

				driver.setPattern(pattern);

				if (url.contains("$1") || url.contains("$2")) {

					driver.setResolveUri(url);
					driver.setPropertiesUri((URI) null);
				} else {

					if (! url.endsWith("/")) url = url + "/";

					driver.setResolveUri(url + "1.0/identifiers/");
					if ("true".equals(propertiesEndpoint)) driver.setPropertiesUri(url + "1.0/properties");
				}

				driver.setTestIdentifiers(readTestIdentifiers(testIdentifiers));

				// done

				drivers.add(driver);
				if (log.isInfoEnabled()) log.info("Added driver for pattern '" + pattern + "' at " + driver.getResolveUri() + " (" + driver.getPropertiesUri() + ")");
			}
		}

		LocalUniResolver localUniResolver = new LocalUniResolver();
		localUniResolver.setDrivers(drivers);

		return localUniResolver;
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

		// prepare resolve result

		ResolveResult resolveResult = ResolveResult.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// parse DID URL

		DIDURL didUrl = null;

		try {

			didUrl = DIDURL.fromString(identifier);
			resolveResult.getDidResolutionMetadata().put("didUrl", didUrl);

			log.debug("Identifier " + identifier + " is a valid DID URL: " + didUrl);
		} catch (IllegalArgumentException | ParserException ex) {

			log.debug("Identifier " + identifier + " is not a valid DID URL: " + ex.getMessage());
		}

		// execute extensions (before)

		if (! extensionStatus.skipExtensionsBefore()) {

			for (Extension extension : this.getExtensions()) {

				extensionStatus.or(extension.beforeResolve(identifier, didUrl, options, resolveResult, this));
				if (extensionStatus.skipExtensionsBefore()) break;
			}
		}

		// try all drivers

		if (! extensionStatus.skipDriver()) {

			String resolveIdentifier = didUrl != null ? didUrl.getDid().getDidString() : identifier;
			if (log.isDebugEnabled()) log.debug("Resolving identifier: " + resolveIdentifier);

			ResolveResult driverResolveResult = ResolveResult.build();
			this.resolveWithDrivers(resolveIdentifier, driverResolveResult);

			resolveResult.setDidDocument(driverResolveResult.getDidDocument());
			resolveResult.setDidDocumentMetadata(driverResolveResult.getDidDocumentMetadata());

			resolveResult.getDidResolutionMetadata().putAll(driverResolveResult.getDidResolutionMetadata());
		}

		// execute extensions (after)

		if (! extensionStatus.skipExtensionsAfter()) {

			for (Extension extension : this.getExtensions()) {

				extensionStatus.or(extension.afterResolve(identifier, didUrl, options, resolveResult, this));
				if (extensionStatus.skipExtensionsAfter()) break;
			}
		}

		// stop time

		long stop = System.currentTimeMillis();

		resolveResult.getDidResolutionMetadata().put("duration", Long.valueOf(stop - start));

		// done

		return resolveResult;
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
	 * Helper methods
	 */

	private static List<String> readTestIdentifiers(JsonArray jsonTestIdentifiers) {

		List<String> testIdentifiers = new ArrayList<String> (jsonTestIdentifiers.size());
		for (JsonElement jsonTestIdentifier : jsonTestIdentifiers) testIdentifiers.add(jsonTestIdentifier.getAsString());
		return testIdentifiers;
	}

	public void resolveWithDrivers(String resolveIdentifier, ResolveResult resolveResult) throws ResolutionException {

		ResolveResult driverResolveResult = null;
		Driver usedDriver = null;

		for (Driver driver : this.getDrivers()) {

			if (log.isDebugEnabled()) log.debug("Attempting to resolve " + resolveIdentifier + " with driver " + driver.getClass().getSimpleName());
			driverResolveResult = driver.resolve(resolveIdentifier);

			if (driverResolveResult != null && driverResolveResult.getDidDocument() != null && driverResolveResult.getDidDocument().getJsonObject().isEmpty()) {

				driverResolveResult.setDidDocument((DIDDocument) null);
			}

			if (driverResolveResult != null) {

				usedDriver = driver;

				resolveResult.setDidDocument(driverResolveResult.getDidDocument());
				resolveResult.setDidDocumentMetadata(driverResolveResult.getDidDocumentMetadata());

				break;
			}
		}

		if (usedDriver != null) {

			if (usedDriver instanceof HttpDriver) {

				resolveResult.getDidResolutionMetadata().put("pattern", ((HttpDriver) usedDriver).getPattern().pattern());
				resolveResult.getDidResolutionMetadata().put("driverUrl", ((HttpDriver) usedDriver).getResolveUri());

				if (log.isDebugEnabled()) log.debug("Resolved " + resolveIdentifier + " with driver " + drivers.getClass().getSimpleName() + " and pattern " + ((HttpDriver) usedDriver).getPattern().pattern());
			} else {

				if (log.isDebugEnabled()) log.debug("Resolved " + resolveIdentifier + " with driver " + drivers.getClass().getSimpleName());
			}
		} else {

			if (log.isDebugEnabled()) log.debug("No result with " + this.getDrivers().size() + " drivers.");
		}

		resolveResult.getDidResolutionMetadata().put("identifier", resolveIdentifier);
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

	public List<Extension> getExtensions() {

		return this.extensions;
	}

	public void setExtensions(List<Extension> extensions) {

		this.extensions = extensions;
	}
}
