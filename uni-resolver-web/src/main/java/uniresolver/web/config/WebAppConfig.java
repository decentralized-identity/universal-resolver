package uniresolver.web.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uniresolver.local.LocalUniResolver;
import uniresolver.web.servlet.MethodsServlet;
import uniresolver.web.servlet.PropertiesServlet;
import uniresolver.web.servlet.ResolveServlet;
import uniresolver.web.servlet.TestIdentifiersServlet;

import java.io.IOException;

@Configuration
public class WebAppConfig {

	@Bean(name = "UniResolver")
	public LocalUniResolver localUniResolver() throws IOException {
		return LocalUniResolver.fromConfigFile("./config.json");
	}

	@Bean(name = "ResolveServlet")
	public ResolveServlet resolveServlet() {
		return new ResolveServlet();
	}

	@Bean
	public ServletRegistrationBean<PropertiesServlet> propertiesServletRegistrationBean() {
		return new ServletRegistrationBean<>(propertiesServlet(), "/1.0/properties/*");
	}

	@Bean(name = "PropertiesServlet")
	public PropertiesServlet propertiesServlet() {
		return new PropertiesServlet();
	}

	@Bean
	public ServletRegistrationBean<ResolveServlet> resolveServletRegistrationBean() {
		return new ServletRegistrationBean<>(resolveServlet(), "/1.0/identifiers/*");
	}

	@Bean(name = "MethodServlet")
	public MethodsServlet methodsServlet() {
		return new MethodsServlet();
	}

	@Bean
	public ServletRegistrationBean<MethodsServlet> methodServletRegistrationBean() {
		return new ServletRegistrationBean<>(methodsServlet(), "/1.0/methods/*");
	}

	@Bean(name = "TestIdentifiersServlet")
	public TestIdentifiersServlet testIdentifiersServlet() {
		return new TestIdentifiersServlet();
	}

	@Bean
	public ServletRegistrationBean<TestIdentifiersServlet> testIdentifiersServletRegistrationBean() {
		return new ServletRegistrationBean<>(testIdentifiersServlet(), "/1.0/testIdentifiers/*");
	}
}
