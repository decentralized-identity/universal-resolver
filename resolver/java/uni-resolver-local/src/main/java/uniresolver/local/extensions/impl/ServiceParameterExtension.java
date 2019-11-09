package uniresolver.local.extensions.impl;

import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import did.DIDURL;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.ParameterExtension;
import uniresolver.local.extensions.ParameterExtension.AbstractParameterExtension;
import uniresolver.result.ResolveResult;

public class ServiceParameterExtension extends AbstractParameterExtension implements ParameterExtension {

	private static Logger log = LoggerFactory.getLogger(ServiceParameterExtension.class);

	private static final String[] HANDLES_PARAMETERS = new String[] { "service", "service-type", "key", "key-type" };

	@Override
	public String[] handlesParameters() {

		return HANDLES_PARAMETERS;
	}

	@Override
	public ExtensionStatus afterResolve(String identifier, DIDURL didUrl, Map<String, String> options, ResolveResult resolveResult, LocalUniResolver localUniResolver) throws ResolutionException {

		if (didUrl == null) return ExtensionStatus.DEFAULT;
		if (didUrl .getParametersMap() == null) return ExtensionStatus.DEFAULT;
		if (! (didUrl.getParametersMap().containsKey("service") || didUrl.getParametersMap().containsKey("service-type") || didUrl.getParametersMap().containsKey("key") || didUrl.getParametersMap().containsKey("key-type"))) return ExtensionStatus.DEFAULT;

		Integer[] selectedServices = null;

		String selectServiceName = didUrl.getParametersMap().get("service");
		String selectServiceType = didUrl.getParametersMap().get("service-type");

		if (selectServiceName != null || selectServiceType != null) {

			selectedServices = resolveResult.getDidDocument().selectServices(selectServiceName, selectServiceType).keySet().toArray(new Integer[0]);

			if (log.isDebugEnabled()) log.debug("Selected services: " + Arrays.asList(selectedServices));
		}

		Integer[] selectedKeys = null;

		String selectKeyName = didUrl.getParametersMap().get("key");
		String selectKeyType = didUrl.getParametersMap().get("key-type");

		if (selectKeyName != null || selectKeyType != null) {

			selectedKeys = resolveResult.getDidDocument().selectKeys(selectKeyName, selectKeyType).keySet().toArray(new Integer[0]);

			if (log.isDebugEnabled()) log.debug("Selected keys: " + Arrays.asList(selectedKeys));
		}

		if (selectedServices != null) resolveResult.getResolverMetadata().put("selectedServices", selectedServices);
		if (selectedKeys != null) resolveResult.getResolverMetadata().put("selectedKeys", selectedKeys);

		return ExtensionStatus.DEFAULT;
	}
}
