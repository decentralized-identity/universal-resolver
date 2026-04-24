package uniresolver.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DriverConfigsBindingTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
			.withUserConfiguration(DriverConfigs.class);

	@Test
	void bindsEntryIdsAndDisabledEntries() {
		this.contextRunner
				.withPropertyValues(
						"uniresolver.disabled-entries[0]=entry-b",
						"uniresolver.entry-probe-token=probe-token",
						"uniresolver.drivers[0].id=entry-a",
						"uniresolver.drivers[0].pattern=^(did:example:.+)$",
						"uniresolver.drivers[0].url=http://example.com/a/",
						"uniresolver.drivers[1].id=entry-b",
						"uniresolver.drivers[1].pattern=^(did:web:.+)$",
						"uniresolver.drivers[1].url=http://example.com/b/")
				.run(context -> {
					assertThat(context).hasNotFailed();

					DriverConfigs driverConfigs = context.getBean(DriverConfigs.class);
					assertThat(driverConfigs.getDisabledEntries()).containsExactly("entry-b");
					assertThat(driverConfigs.getEntryProbeToken()).isEqualTo("probe-token");
					assertThat(driverConfigs.getDrivers()).extracting(DriverConfigs.DriverConfig::getId)
							.containsExactly("entry-a", "entry-b");
				});
	}

	@Test
	void bindsDisabledEntriesFromEnvironmentPlaceholder() {
		this.contextRunner
				.withSystemProperties(
						"UNIRESOLVER_DISABLED_ENTRIES=entry-a,entry-b",
						"UNIRESOLVER_ENTRY_PROBE_TOKEN=probe-token")
				.withPropertyValues(
						"uniresolver.disabled-entries=${UNIRESOLVER_DISABLED_ENTRIES:}",
						"uniresolver.entry-probe-token=${UNIRESOLVER_ENTRY_PROBE_TOKEN:}",
						"uniresolver.drivers[0].id=entry-a",
						"uniresolver.drivers[0].pattern=^(did:example:.+)$",
						"uniresolver.drivers[0].url=http://example.com/a/",
						"uniresolver.drivers[1].id=entry-b",
						"uniresolver.drivers[1].pattern=^(did:web:.+)$",
						"uniresolver.drivers[1].url=http://example.com/b/")
				.run(context -> {
					assertThat(context).hasNotFailed();

					DriverConfigs driverConfigs = context.getBean(DriverConfigs.class);
					assertThat(driverConfigs.getDisabledEntries()).containsExactly("entry-a", "entry-b");
					assertThat(driverConfigs.getEntryProbeToken()).isEqualTo("probe-token");
				});
	}

	@Test
	void emptyDisabledEntriesEnvironmentClearsTheList() {
		this.contextRunner
				.withPropertyValues(
						"uniresolver.disabled-entries=${UNIRESOLVER_DISABLED_ENTRIES:}",
						"uniresolver.drivers[0].id=entry-a",
						"uniresolver.drivers[0].pattern=^(did:example:.+)$",
						"uniresolver.drivers[0].url=http://example.com/a/")
				.run(context -> {
					assertThat(context).hasNotFailed();

					DriverConfigs driverConfigs = context.getBean(DriverConfigs.class);
					assertThat(driverConfigs.getDisabledEntries()).isEmpty();
				});
	}

	@Test
	void failsWhenEntryIdIsMissing() {
		this.contextRunner
				.withPropertyValues(
						"uniresolver.drivers[0].pattern=^(did:example:.+)$",
						"uniresolver.drivers[0].url=http://example.com/a/")
				.run(context -> {
					assertThat(context).hasFailed();
					assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("Missing 'id' entry");
				});
	}

	@Test
	void failsWhenEntryIdIsDuplicated() {
		this.contextRunner
				.withPropertyValues(
						"uniresolver.drivers[0].id=entry-a",
						"uniresolver.drivers[0].pattern=^(did:example:.+)$",
						"uniresolver.drivers[0].url=http://example.com/a/",
						"uniresolver.drivers[1].id=entry-a",
						"uniresolver.drivers[1].pattern=^(did:web:.+)$",
						"uniresolver.drivers[1].url=http://example.com/b/")
				.run(context -> {
					assertThat(context).hasFailed();
					assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("Duplicate 'id' entry");
				});
	}
}
