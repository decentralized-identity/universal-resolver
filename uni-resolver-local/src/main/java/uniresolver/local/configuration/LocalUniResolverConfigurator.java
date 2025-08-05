package uniresolver.local.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;

public class LocalUniResolverConfigurator {

    private static final Logger log = LoggerFactory.getLogger(LocalUniResolverConfigurator.class);

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static void configureLocalUniResolver(String filePath, LocalUniResolver localUniResolver) throws IOException {

        List<Driver> drivers = new ArrayList<>();

        try (Reader reader = new FileReader(filePath)) {

            Map<String, Object> jsonRoot = objectMapper.readValue(reader, Map.class);
            List<Map<String, Object>> jsonDrivers = (List<Map<String, Object>>) jsonRoot.get("drivers");

            for (Map<String, Object> jsonDriver : jsonDrivers) {

                String pattern = jsonDriver.containsKey("pattern") ? (String) jsonDriver.get("pattern") : null;
                String url = jsonDriver.containsKey("url") ? (String) jsonDriver.get("url") : null;
                String propertiesEndpoint = jsonDriver.containsKey("propertiesEndpoint") ? (String) jsonDriver.get("propertiesEndpoint") : null;
                String supportsOptions = jsonDriver.containsKey("supportsOptions") ? (String) jsonDriver.get("supportsOptions") : null;
                String supportsDereference = jsonDriver.containsKey("supportsDereference") ? (String) jsonDriver.get("supportsDereference") : null;
                String acceptHeaderValue = jsonDriver.containsKey("acceptHeaderValue") ? (String) jsonDriver.get("acceptHeaderValue") : null;
                String acceptHeaderValueDereference = jsonDriver.containsKey("acceptHeaderValueDereference") ? (String) jsonDriver.get("acceptHeaderValueDereference") : null;
                List<String> testIdentifiers = jsonDriver.containsKey("testIdentifiers") ? (List<String>) jsonDriver.get("testIdentifiers") : null;
                Map<String, Object> traits = jsonDriver.containsKey("traits") ? (Map<String, Object>) jsonDriver.get("traits") : null;

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

                if (supportsOptions != null) driver.setSupportsOptions(Boolean.parseBoolean(supportsOptions));
                if (supportsDereference != null) driver.setSupportsDereference(Boolean.parseBoolean(supportsDereference));
                if (acceptHeaderValue != null) driver.setAcceptHeaderValue(acceptHeaderValue);
                if (acceptHeaderValueDereference != null) driver.setAcceptHeaderValueDereference(acceptHeaderValueDereference);
                if (testIdentifiers != null) driver.setTestIdentifiers(testIdentifiers);
                if (traits != null) driver.setTraits(traits);

                // done

                drivers.add(driver);
                if (log.isInfoEnabled()) log.info("Added driver for pattern '" + pattern + "' at " + driver.getResolveUri() + " (" + driver.getPropertiesUri() + ")");
            }
        }

        // done

        localUniResolver.setDrivers(drivers);
    }
}
