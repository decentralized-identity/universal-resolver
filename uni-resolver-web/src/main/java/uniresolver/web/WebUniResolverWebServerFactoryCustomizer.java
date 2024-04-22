package uniresolver.web;

import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class WebUniResolverWebServerFactoryCustomizer implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {

	@Override
	public void customize(JettyServletWebServerFactory factory) {
		factory.addServerCustomizers(this::customizeUriCompliance);
	}

	private void customizeUriCompliance(Server server) {
		for (Connector connector : server.getConnectors()) {
			connector.getConnectionFactories().stream()
					.filter(factory -> factory instanceof HttpConnectionFactory)
					.forEach(factory -> {
						HttpConfiguration httpConfig = ((HttpConnectionFactory) factory).getHttpConfiguration();
						httpConfig.setUriCompliance(UriCompliance.UNSAFE);
					});
		}
	}
}
