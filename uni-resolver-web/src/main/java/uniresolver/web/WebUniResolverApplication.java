package uniresolver.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import uniresolver.local.LocalUniResolver;

@SpringBootApplication
public class WebUniResolverApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(WebUniResolverApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WebUniResolverApplication.class);
	}

	@Bean(name = "UniResolver")
	public LocalUniResolver localUniResolver() {
		return new LocalUniResolver();
	}
}