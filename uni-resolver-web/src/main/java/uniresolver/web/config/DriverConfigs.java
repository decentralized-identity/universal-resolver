package uniresolver.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Configuration
@ConfigurationProperties("uniresolver")
public class DriverConfigs {

	private List<DriverConfig> drivers;

	public List<DriverConfig> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<DriverConfig> drivers) {
		this.drivers = drivers;
	}

	public static class DriverConfig {

		private String pattern;
		private String url;
		private String propertiesEndpoint;
		private List<String> testIdentifiers;
		private Map<String, Object> traits;

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
					"pattern='" + pattern + '\'' +
					", url='" + url + '\'' +
					", propertiesEndpoint='" + propertiesEndpoint + '\'' +
					", testIdentifiers=" + testIdentifiers +
					", traits=" + traits +
					'}';
		}
	}
}
