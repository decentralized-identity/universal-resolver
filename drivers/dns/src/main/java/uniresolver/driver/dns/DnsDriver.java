package uniresolver.driver.dns;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Type;
import org.xbill.DNS.URIRecord;

import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

public class DnsDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DnsDriver.class);

	public static final Pattern DNS_PATTERN = Pattern.compile("^((?:(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*(?:[A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]))$");

	private Map<String, Object> properties;

	private String dnsServers;

	private Resolver resolver = null;

	public DnsDriver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DnsDriver() {

		this(getPropertiesFromEnvironment());
	}

	private static Map<String, Object> getPropertiesFromEnvironment() {

		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

		Map<String, Object> properties = new HashMap<String, Object> ();

		try {

			String env_dnsServers = System.getenv("uniresolver_driver_dns_dnsServers");

			if (env_dnsServers != null) properties.put("dnsServers", env_dnsServers);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
	}

	private void configureFromProperties() {

		if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

		try {

			String prop_dnsServers = (String) this.getProperties().get("dnsServers");

			if (prop_dnsServers != null) this.setDnsServers(prop_dnsServers);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {

		// open pool

		if (this.getResolver() == null) this.openResolver();

		// parse identifier

		Matcher matcher = DNS_PATTERN.matcher(identifier);
		if (! matcher.matches()) return null;

		// DNS lookup

		Lookup lookup = null;
		Record[] records;

		try {

			lookup = new Lookup("_did." + identifier, Type.URI);
			lookup.setResolver(this.getResolver());
			records = lookup.run();
		} catch (Exception ex) {

			throw new ResolutionException("DNS resolution problem: " + ex.getMessage() + (lookup != null ? (" (" + lookup.getErrorString() + ")") : ""));
		}

		if (lookup.getErrorString() != null && ! "successful".equals(lookup.getErrorString())) {

			if (log.isDebugEnabled()) log.debug("For identifier " + identifier + " got error: " + lookup.getErrorString());
			throw new ResolutionException("DNS resolution error: " + lookup.getErrorString());
		}

		if (records == null) return null;

		for (int i=0; i<records.length; i++) {

			URIRecord uri = (URIRecord) records[i];
			if (log.isDebugEnabled()) log.debug("For identifier " + identifier + " found entry " + uri.getTarget() + " with preference " + uri.getPriority());
		}

		String did = records.length > 0 ? ((URIRecord) records[0]).getTarget() : null;
		Integer priority = records.length > 0 ? Integer.valueOf(((URIRecord) records[0]).getPriority()) : null;

		// create METHOD METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		if (did != null) methodMetadata.put("redirect", did);
		if (priority != null) methodMetadata.put("priority", priority);

		// create RESOLVE RESULT

		ResolveResult resolveResult = ResolveResult.build(null, null, null, null, methodMetadata);

		// done

		return resolveResult;
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
	}

	private void openResolver() throws ResolutionException {

		// create resolver

		try {

			if (this.getDnsServers() != null && ! this.getDnsServers().trim().isEmpty()) {

				String[] dnsServers = this.getDnsServers().split(";");
				this.resolver = new ExtendedResolver(dnsServers);
				if (log.isInfoEnabled()) log.info("Created DNS resolver with servers " + Arrays.asList(dnsServers) + ".");
			} else {

				this.resolver = new ExtendedResolver();
				if (log.isInfoEnabled()) log.info("Created default DNS resolver.");
			}
		} catch (UnknownHostException ex) {

			throw new ResolutionException("Unable to create DNS resolver: " + ex.getMessage(), ex);
		}

	}

	/*
	 * Getters and setters
	 */

	public Map<String, Object> getProperties() {

		return this.properties;
	}

	public void setProperties(Map<String, Object> properties) {

		this.properties = properties;
		this.configureFromProperties();
	}

	public String getDnsServers() {

		return this.dnsServers;
	}

	public void setDnsServers(String dnsServers) {

		this.dnsServers = dnsServers;
	}

	public Resolver getResolver() {

		return this.resolver;
	}

	public void setResolver(Resolver resolver) {

		this.resolver = resolver;
	}
}
