package uniresolver.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("server.servlet.mappings")
public class ServletMappings {

	private String properties;
	private String resolve;
	private String methods;
	private String testIdentifiers;

	public String getProperties() {
		return properties;
	}

	public String getResolve() {
		return resolve;
	}

	public String getMethods() {
		return methods;
	}

	public String getTestIdentifiers() {
		return testIdentifiers;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public void setResolve(String resolve) {
		this.resolve = resolve;
	}

	public void setMethods(String methods) {
		this.methods = methods;
	}

	public void setTestIdentifiers(String testIdentifiers) {
		this.testIdentifiers = testIdentifiers;
	}
}
