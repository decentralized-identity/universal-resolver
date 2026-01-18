package uniresolver.web;

import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class WebUniResolverWebServerFactoryCustomizer implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

	private static final Logger log = LoggerFactory.getLogger(WebUniResolverWebServerFactoryCustomizer.class);

	@Override
	public void customize(JettyServletWebServerFactory factory) {
		factory.addServerCustomizers(this::customizeUriCompliance);
	}

	private void customizeUriCompliance(Server server) {
		if (log.isInfoEnabled()) log.info("Customizing URI compliance: " + server);
		for (Connector connector : server.getConnectors()) {
			if (log.isInfoEnabled()) log.info("Customizing connector: " + connector);
			connector.getConnectionFactories().stream()
					.filter(factory -> factory instanceof HttpConnectionFactory)
					.forEach(factory -> {
						HttpConfiguration httpConfig = ((HttpConnectionFactory) factory).getHttpConfiguration();
						httpConfig.setUriCompliance(UriCompliance.UNSAFE);
						if (log.isInfoEnabled()) log.info("Set URI compliance: " + httpConfig);
					});
		}
	}
}
