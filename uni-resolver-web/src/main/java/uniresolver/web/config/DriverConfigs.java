package uniresolver.web.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Configuration
@ConfigurationProperties("uniresolver")
public class DriverConfigs {

	private List<String> disabledEntries = new ArrayList<>();
	private String entryProbeToken;
	private List<DriverConfig> drivers;

	@PostConstruct
	public void validate() {
		this.disabledEntries = this.disabledEntries == null ? new ArrayList<>() : this.disabledEntries.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(value -> ! value.isEmpty())
				.toList();

		if (this.drivers == null) return;

		Set<String> driverIds = new LinkedHashSet<>();
		for (int i = 0; i < this.drivers.size(); i++) {
			DriverConfig driverConfig = this.drivers.get(i);
			if (driverConfig == null) {
				throw new IllegalArgumentException("Missing driver configuration at index " + i + ".");
			}

			String id = driverConfig.getId();
			if (id == null || id.isBlank()) {
				throw new IllegalArgumentException("Missing 'id' entry in driver configuration at index " + i + ".");
			}

			if (! driverIds.add(id)) {
				throw new IllegalArgumentException("Duplicate 'id' entry in driver configuration: " + id);
			}
		}
	}

	public List<String> getDisabledEntries() {
		return disabledEntries;
	}

	public void setDisabledEntries(List<String> disabledEntries) {
		this.disabledEntries = disabledEntries;
	}

	public String getEntryProbeToken() {
		return entryProbeToken;
	}

	public void setEntryProbeToken(String entryProbeToken) {
		this.entryProbeToken = entryProbeToken;
	}

	public List<DriverConfig> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<DriverConfig> drivers) {
		this.drivers = drivers;
	}

	public static class DriverConfig {

		private String id;
		private String pattern;
		private String url;
		private String propertiesEndpoint;
		private String supportsOptions;
		private String supportsDereference;
		private String acceptHeaderValue;
		private String acceptHeaderValueDereference;
		private List<String> testIdentifiers;
		private Map<String, Object> traits;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getPattern() {
			return pattern;
		}

		public void setPattern(String value) {
			this.pattern = value;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String value) {
			this.url = value;
		}

		public String getPropertiesEndpoint() {
			return propertiesEndpoint;
		}

		public void setPropertiesEndpoint(String value) {
			this.propertiesEndpoint = value;
		}

		public String getSupportsOptions() {
			return supportsOptions;
		}

		public void setSupportsOptions(String supportsOptions) {
			this.supportsOptions = supportsOptions;
		}

		public String getSupportsDereference() {
			return supportsDereference;
		}

		public void setSupportsDereference(String supportsDereference) {
			this.supportsDereference = supportsDereference;
		}

		public String getAcceptHeaderValue() {
			return acceptHeaderValue;
		}

		public void setAcceptHeaderValue(String acceptHeaderValue) {
			this.acceptHeaderValue = acceptHeaderValue;
		}

		public String getAcceptHeaderValueDereference() {
			return this.acceptHeaderValueDereference;
		}

		public void setAcceptHeaderValueDereference(String acceptHeaderValueDereference) {
			this.acceptHeaderValueDereference = acceptHeaderValueDereference;
		}

		public List<String> getTestIdentifiers() {
			return testIdentifiers;
		}

		public void setTestIdentifiers(List<String> value) {
			this.testIdentifiers = value;
		}

		public Map<String,Object> getTraits() {
			return traits;
		}

		public void setTraits(Map<String,Object> traits) {
			this.traits = traits;
		}

		@Override
		public String toString() {
			return "DriverConfig{" +
					"id='" + id + '\'' +
					", " +
					"pattern='" + pattern + '\'' +
					", url='" + url + '\'' +
					", propertiesEndpoint='" + propertiesEndpoint + '\'' +
					", supportsOptions='" + supportsOptions + '\'' +
					", supportsDereference='" + supportsDereference + '\'' +
					", acceptHeaderValue='" + acceptHeaderValue + '\'' +
					", acceptHeaderValueDereference='" + acceptHeaderValueDereference + '\'' +
					", testIdentifiers=" + testIdentifiers +
					", traits=" + traits +
					'}';
		}
	}
}
