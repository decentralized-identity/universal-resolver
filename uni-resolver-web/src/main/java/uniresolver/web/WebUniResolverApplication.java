package uniresolver.web;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import uniresolver.local.LocalUniDereferencer;
import uniresolver.local.LocalUniResolver;

@SpringBootApplication
public class WebUniResolverApplication extends SpringBootServletInitializer implements ApplicationContextAware {

	public static void main(String[] args) {
		SpringApplication.run(WebUniResolverApplication.class, args);
	}

	private ApplicationContext applicationContext;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WebUniResolverApplication.class);
	}

	@Bean(name = "UniResolver")
	public LocalUniResolver localUniResolver() {
		return new LocalUniResolver();
	}

	@Bean(name = "UniDereferencer")
	public LocalUniDereferencer localUniDereferencer() {
		return new LocalUniDereferencer(this.applicationContext.getBean("UniResolver", LocalUniResolver.class));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}