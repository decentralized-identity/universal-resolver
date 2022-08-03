package uniresolver.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
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

		public String getPattern() {
			return pattern;
		}

		public void setPattern(String value) {
			this.pattern = value;
		}

		public String getURL() {
			return url;
		}

		public void setURL(String value) {
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

		@Override
		public String toString() {
			return new StringJoiner(", ", DriverConfig.class.getSimpleName() + "[", "]").add(
							"pattern='" + pattern + "'")
					.add("url='" + url + "'")
					.add("propertiesEndpoint='" + propertiesEndpoint + "'")
					.add("testIdentifiers=" + testIdentifiers)
					.toString();
		}
	}
}
