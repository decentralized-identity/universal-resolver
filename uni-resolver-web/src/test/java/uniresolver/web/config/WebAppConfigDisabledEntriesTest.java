package uniresolver.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uniresolver.driver.Driver;
import uniresolver.driver.http.HttpDriver;
import uniresolver.local.LocalUniResolver;
import uniresolver.web.config.DriverConfigs.DriverConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebAppConfigDisabledEntriesTest {

	@Test
	void configuresEntryIdsAndDisabledFlagsOnDrivers() {
		DriverConfigs driverConfigs = new DriverConfigs();
		driverConfigs.setDisabledEntries(List.of("entry-b"));
		driverConfigs.setEntryProbeToken("probe-token");
		driverConfigs.setDrivers(List.of(
				driverConfig("entry-a", "^(did:example:.+)$", "http://example.com/a/"),
				driverConfig("entry-b", "^(did:web:.+)$", "http://example.com/b/")
		));
		driverConfigs.validate();

		ServletMappings servletMappings = new ServletMappings();
		servletMappings.setResolve("/1.0/identifiers");
		servletMappings.setProperties("/1.0/properties");

		WebAppConfig webAppConfig = new WebAppConfig();
		ReflectionTestUtils.setField(webAppConfig, "servletMappings", servletMappings);

		LocalUniResolver resolver = new LocalUniResolver();
		webAppConfig.configureLocalUniresolver(driverConfigs, resolver);

		assertThat(resolver.getDrivers()).hasSize(2);
		assertThat(resolver.getDrivers()).allMatch(HttpDriver.class::isInstance);
		assertThat(resolver.getDrivers()).extracting(driver -> ((HttpDriver) driver).getId())
				.containsExactly("entry-a", "entry-b");
		assertThat(resolver.getDrivers()).extracting(driver -> ((HttpDriver) driver).getDisabled())
				.containsExactly(false, true);
		assertThat(resolver.getEntryProbeToken()).isEqualTo("probe-token");
	}

	private static DriverConfig driverConfig(String id, String pattern, String url) {
		DriverConfig driverConfig = new DriverConfig();
		driverConfig.setId(id);
		driverConfig.setPattern(pattern);
		driverConfig.setUrl(url);
		return driverConfig;
	}
}
