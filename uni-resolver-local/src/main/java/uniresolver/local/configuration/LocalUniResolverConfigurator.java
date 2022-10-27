package uniresolver.local.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.driver.Driver;
import uniresolver.driver.http.HttpDriver;
import uniresolver.local.LocalUniResolver;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LocalUniResolverConfigurator {

    private static final Logger log = LoggerFactory.getLogger(LocalUniResolverConfigurator.class);

    public static void configureLocalUniResolver(String filePath, LocalUniResolver localUniResolver) throws IOException {

        final Gson gson = new Gson();

        List<Driver> drivers = new ArrayList<Driver>();

        try (Reader reader = new FileReader(filePath)) {

            JsonObject jsonObjectRoot  = gson.fromJson(reader, JsonObject.class);
            JsonArray jsonArrayDrivers = jsonObjectRoot.getAsJsonArray("drivers");

            for (Iterator<JsonElement> jsonElementsDrivers = jsonArrayDrivers.iterator(); jsonElementsDrivers.hasNext(); ) {

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

        // done

        localUniResolver.setDrivers(drivers);
    }

    private static List<String> readTestIdentifiers(JsonArray jsonTestIdentifiers) {

        List<String> testIdentifiers = new ArrayList<String> (jsonTestIdentifiers.size());
        for (JsonElement jsonTestIdentifier : jsonTestIdentifiers) testIdentifiers.add(jsonTestIdentifier.getAsString());
        return testIdentifiers;
    }
}
