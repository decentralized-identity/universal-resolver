package uniresolver.web.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uniresolver.driver.Driver;
import uniresolver.driver.http.HttpDriver;
import uniresolver.local.LocalUniResolver;
import uniresolver.web.servlet.MethodsServlet;
import uniresolver.web.servlet.PropertiesServlet;
import uniresolver.web.servlet.ResolveServlet;
import uniresolver.web.servlet.TestIdentifiersServlet;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

		for (DriverConfigs.DriverConfig dc : driverConfigs.getDrivers()) {

			if (dc.getPattern() == null)
				throw new IllegalArgumentException("Missing 'pattern' entry in driver configuration.");
			if (dc.getURL() == null) throw new IllegalArgumentException("Missing 'url' entry in driver configuration.");

			// adopted from LocalUniResolverConfigurator
			// construct HTTP driver

			HttpDriver driver = new HttpDriver();

			driver.setPattern(dc.getPattern());
			if (dc.getURL().contains("$1") || dc.getURL().contains("$2")) {

				driver.setResolveUri(dc.getURL());
				driver.setPropertiesUri((URI) null);
			} else {

				if (!dc.getURL().endsWith("/")) dc.setURL(dc.getURL() + "/");

				driver.setResolveUri(normalizeUri((dc.getURL() + servletMappings.getResolve()), true));
				if ("true".equals(dc.getPropertiesEndpoint()))
					driver.setPropertiesUri(normalizeUri((dc.getURL() + servletMappings.getProperties()), false));
			}

			driver.setTestIdentifiers(dc.getTestIdentifiers());

			// done

			drivers.add(driver);
			if (log.isInfoEnabled())
				log.info("Added driver for pattern '" + dc.getPattern() + "' at " + driver.getResolveUri() + " (" + driver.getPropertiesUri() + ")");
		}

		uniResolver.setDrivers(drivers);
	}

	@PostConstruct
	private void initDrivers() {
		if (driverConfigs.getDrivers() != null) configureLocalUniresolver(driverConfigs, localUniResolver);
	}
}
