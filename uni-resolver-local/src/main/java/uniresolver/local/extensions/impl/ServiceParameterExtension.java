package uniresolver.local.extensions.impl;

import foundation.identity.did.*;
import foundation.identity.did.parameters.Parameters;
import foundation.identity.did.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniDereferencer;
import uniresolver.local.LocalUniResolver;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.ParameterExtension;
import uniresolver.local.extensions.ParameterExtension.AbstractParameterExtension;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServiceParameterExtension extends AbstractParameterExtension implements ParameterExtension {

	private static Logger log = LoggerFactory.getLogger(ServiceParameterExtension.class);

	private static final String[] HANDLES_PARAMETERS = new String[] { Parameters.DID_URL_PARAMETER_SERVICE, "serviceType" };

	@Override
	public String[] handlesParameters() {

		return HANDLES_PARAMETERS;
	}

	@Override
	public ExtensionStatus afterDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, DIDDocument didDocument, LocalUniDereferencer localUniDereferencer) throws DereferencingException {

		if (didUrl == null) return ExtensionStatus.DEFAULT;
		if (didUrl.getParameters() == null) return ExtensionStatus.DEFAULT;
		if (! (didUrl.getParameters().containsKey("service") || didUrl.getParameters().containsKey("serviceType"))) return ExtensionStatus.DEFAULT;

		Integer[] selectedServices = null;

		String selectServiceName = didUrl.getParameters().get("service");
		String selectServiceType = didUrl.getParameters().get("serviceType");

		if (selectServiceName != null || selectServiceType != null) {

			selectedServices = selectServices(didDocument, selectServiceName, selectServiceType).keySet().toArray(new Integer[0]);

			if (log.isDebugEnabled()) log.debug("Selected services: " + Arrays.asList(selectedServices));
		}

		if (selectedServices != null) dereferenceResult.getDereferencingMetadata().put("selectedServices", selectedServices);

		return ExtensionStatus.DEFAULT;
	}

	/*
	 * Helper methods
	 */

	public static Map<Integer, Service> selectServices(DIDDocument didDocument, String selectServiceName, String selectServiceType) {

		int i = -1;
		Map<Integer, Service> selectedServices = new HashMap<Integer, Service> ();
		if (didDocument.getServices() == null) return selectedServices;

		for (Service service : didDocument.getServices()) {

			i++;

			if (selectServiceName != null && service.getId() != null) {

				DIDURL serviceDidUrl;
				try { serviceDidUrl = DIDURL.fromUri(service.getId()); } catch (ParserException ex) { serviceDidUrl = null; }
				String serviceName = serviceDidUrl == null ? null : serviceDidUrl.getFragment();

				if (serviceName == null) continue;
				if (! serviceName.equals(selectServiceName)) continue;
			}

			if (selectServiceType != null & service.getTypes() != null) {

				if (! service.getTypes().contains(selectServiceType)) continue;
			}

			selectedServices.put(Integer.valueOf(i), service);
		}

		return selectedServices;
	}
}
