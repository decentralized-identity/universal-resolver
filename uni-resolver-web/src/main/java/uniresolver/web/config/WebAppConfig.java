package uniresolver.web.config;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uniresolver.driver.Driver;
import uniresolver.driver.http.HttpDriver;
import uniresolver.local.LocalUniResolver;
import uniresolver.web.servlet.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class WebAppConfig {

	private static final Logger log = LogManager.getLogger(WebAppConfig.class);

	@Autowired
	private DriverConfigs driverConfigs;

	@Autowired
	private ServletMappings servletMappings;

	@Autowired
	private LocalUniResolver localUniResolver;

	@Bean(name = "ResolveServlet")
	public ResolveServlet resolveServlet() {
		return new ResolveServlet();
	}

	@Bean
	public ServletRegistrationBean<PropertiesServlet> propertiesServletRegistrationBean() {
		return new ServletRegistrationBean<>(propertiesServlet(), fixWildcardPattern(servletMappings.getProperties()));
	}

	@Bean(name = "PropertiesServlet")
	public PropertiesServlet propertiesServlet() {
		return new PropertiesServlet();
	}

	@Bean
	public ServletRegistrationBean<ResolveServlet> resolveServletRegistrationBean() {
		return new ServletRegistrationBean<>(resolveServlet(), fixWildcardPattern(servletMappings.getResolve()));
	}

	@Bean(name = "MethodServlet")
	public MethodsServlet methodsServlet() {
		return new MethodsServlet();
	}

	@Bean
	public ServletRegistrationBean<MethodsServlet> methodServletRegistrationBean() {
		return new ServletRegistrationBean<>(methodsServlet(), fixWildcardPattern(servletMappings.getMethods()));
	}

	@Bean(name = "TestIdentifiersServlet")
	public TestIdentifiersServlet testIdentifiersServlet() {
		return new TestIdentifiersServlet();
	}

	@Bean
	public ServletRegistrationBean<TestIdentifiersServlet> testIdentifiersServletRegistrationBean() {
		return new ServletRegistrationBean<>(testIdentifiersServlet(), servletMappings.getTestIdentifiers());
	}

	@Bean(name = "TraitsServlet")
	public TraitsServlet traitsServlet() {
		return new TraitsServlet();
	}

	@Bean
	public ServletRegistrationBean<TraitsServlet> traitsServletRegistrationBean() {
		return new ServletRegistrationBean<>(traitsServlet(), servletMappings.getTraits());
	}

	public static String fixWildcardPattern(String s) {
		if(s == null) return "";
		if (s.endsWith("*")) return s;
		if (s.endsWith("/")) return s + "*";
		return s + "/*";
	}

	public static String normalizeUri(String s, boolean postSlash) {
		if (s == null) return null;
		String url = s;
		if (url.endsWith("*")) url = url.substring(0, url.length() - 1);

		URI uri = URI.create(url + "/").normalize();

		return postSlash ? uri.toString() : uri.toString().substring(0, uri.toString().length() - 1);
	}

	public void configureLocalUniresolver(DriverConfigs driverConfigs, LocalUniResolver uniResolver) {

		List<Driver> drivers = new ArrayList<>();

		for (DriverConfigs.DriverConfig driverConfig : driverConfigs.getDrivers()) {

			String pattern = driverConfig.getPattern();
			String url = driverConfig.getUrl();
			String propertiesEndpoint = driverConfig.getPropertiesEndpoint();
			String supportsOptions = driverConfig.getSupportsOptions();
			String supportsDereference = driverConfig.getSupportsDereference();
			String acceptHeaderValue = driverConfig.getAcceptHeaderValue();
			List<String> testIdentifiers = driverConfig.getTestIdentifiers();
			Map<String, Object> traits = driverConfig.getTraits();

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
				driver.setResolveUri(normalizeUri((url + this.servletMappings.getResolve()), true));
				if ("true".equals(propertiesEndpoint)) driver.setPropertiesUri(normalizeUri((url + this.servletMappings.getProperties()), false));
			}

			if (supportsOptions != null) driver.setSupportsOptions(Boolean.parseBoolean(supportsOptions));
			if (supportsDereference != null) driver.setSupportsDereference(Boolean.parseBoolean(supportsDereference));
			if (acceptHeaderValue != null) driver.setAcceptHeaderValue(acceptHeaderValue);
			if (testIdentifiers != null) driver.setTestIdentifiers(testIdentifiers);
			if (traits != null) driver.setTraits(traits);

			// done

			drivers.add(driver);
			if (log.isInfoEnabled()) log.info("Added driver for pattern '" + driverConfig.getPattern() + "' at " + driver.getResolveUri() + " (" + driver.getPropertiesUri() + ")");
		}

		uniResolver.setDrivers(drivers);
	}

	@PostConstruct
	private void initDrivers() {
		if (this.driverConfigs.getDrivers() != null) configureLocalUniresolver(this.driverConfigs, this.localUniResolver);
	}
}
