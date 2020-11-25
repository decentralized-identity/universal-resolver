package uniresolver.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

	private Map<String, Driver> drivers = new HashMap<String, Driver> ();
	private List<Extension> extensions = new ArrayList<Extension> ();

	public LocalUniResolver() {

	}

	public static LocalUniResolver fromConfigFile(String filePath) throws FileNotFoundException, IOException {

		final Gson gson = new Gson();

		Map<String, Driver> drivers = new HashMap<String, Driver> ();

		try (Reader reader = new FileReader(new File(filePath))) {

			JsonObject jsonObjectRoot  = gson.fromJson(reader, JsonObject.class);
			JsonArray jsonArrayDrivers = jsonObjectRoot.getAsJsonArray("drivers");

			int i = 0;

			for (Iterator<JsonElement> jsonElementsDrivers = jsonArrayDrivers.iterator(); jsonElementsDrivers.hasNext(); ) {

				i++;

				JsonObject jsonObjectDriver = (JsonObject) jsonElementsDrivers.next();

				String id = jsonObjectDriver.has("id") ? jsonObjectDriver.get("id").getAsString() : null;
				String pattern = jsonObjectDriver.has("pattern") ? jsonObjectDriver.get("pattern").getAsString() : null;
				String image = jsonObjectDriver.has("image") ? jsonObjectDriver.get("image").getAsString() : null;
				String imagePort = jsonObjectDriver.has("imagePort") ? jsonObjectDriver.get("imagePort").getAsString() : null;
				String imageProperties = jsonObjectDriver.has("imageProperties") ? jsonObjectDriver.get("imageProperties").getAsString() : null;
				String url = jsonObjectDriver.has("url") ? jsonObjectDriver.get("url").getAsString() : null;

				if (pattern == null) throw new IllegalArgumentException("Missing 'pattern' entry in driver configuration.");
				if (image == null && url == null) throw new IllegalArgumentException("Missing 'image' and 'url' entry in driver configuration (need either one).");

				HttpDriver driver = new HttpDriver();
				driver.setPattern(pattern);

				if (url != null) {

					driver.setResolveUri(url);
				} else {

					String httpDriverUri = image.substring(image.indexOf("/") + 1);
					if (httpDriverUri.contains(":")) httpDriverUri = httpDriverUri.substring(0, httpDriverUri.indexOf(":"));
					httpDriverUri = "http://" + httpDriverUri + ":" + (imagePort != null ? imagePort : "8080" ) + "/";

					driver.setResolveUri(httpDriverUri + "1.0/identifiers/$1");

					if ("true".equals(imageProperties)) {

						driver.setPropertiesUri(httpDriverUri + "1.0/properties");
					}
				}

				if (id == null) {

					id = "driver";
					if (image != null) id += "-" + image;
					if (image == null || drivers.containsKey(id)) id += "-" + Integer.toString(i);
				}

				drivers.put(id, driver);

				if (log.isInfoEnabled()) log.info("Added driver '" + id + "' at " + driver.getResolveUri() + " (" + driver.getPropertiesUri() + ")");
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

	public void resolveWithDrivers(String resolveIdentifier, ResolveResult resolveResult) throws ResolutionException {

		ResolveResult driverResolveResult = null;
		String usedDriverId = null;

		for (Entry<String, Driver> driver : this.getDrivers().entrySet()) {

			if (log.isDebugEnabled()) log.debug("Attemping to resolve " + resolveIdentifier + " with driver " + driver.getValue().getClass());
			driverResolveResult = driver.getValue().resolve(resolveIdentifier);

			if (driverResolveResult != null && driverResolveResult.getDidDocument() != null && driverResolveResult.getDidDocument().getJsonObject().isEmpty()) {

				driverResolveResult.setDidDocument((DIDDocument) null);
			}

			if (driverResolveResult != null) {

				usedDriverId = driver.getKey();

				resolveResult.setDidDocument(driverResolveResult.getDidDocument());
				resolveResult.setDidDocumentMetadata(driverResolveResult.getDidDocumentMetadata());

				break;
			}
		}

		if (usedDriverId != null) {

			resolveResult.getDidResolutionMetadata().put("driverId", usedDriverId);
			if (log.isDebugEnabled()) log.debug("Resolved " + resolveIdentifier + " with driver " + usedDriverId);
		} else {

			if (log.isDebugEnabled()) log.debug("No result with " + this.getDrivers().size() + " drivers.");
		}

		resolveResult.getDidResolutionMetadata().put("identifier", resolveIdentifier);
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

	public List<Extension> getExtensions() {

		return this.extensions;
	}

	public void setExtensions(List<Extension> extensions) {

		this.extensions = extensions;
	}
}
