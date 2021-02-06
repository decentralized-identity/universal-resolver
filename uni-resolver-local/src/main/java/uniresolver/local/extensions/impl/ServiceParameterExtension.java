package uniresolver.local.extensions.impl;

import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import foundation.identity.did.Service;
import foundation.identity.did.VerificationMethod;
import foundation.identity.did.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.ParameterExtension;
import uniresolver.local.extensions.ParameterExtension.AbstractParameterExtension;
import uniresolver.result.ResolveResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
		if (didUrl.getParameters() == null) return ExtensionStatus.DEFAULT;
		if (! (didUrl.getParameters().containsKey("service") || didUrl.getParameters().containsKey("service-type") || didUrl.getParameters().containsKey("key") || didUrl.getParameters().containsKey("key-type"))) return ExtensionStatus.DEFAULT;

		Integer[] selectedServices = null;

		String selectServiceName = didUrl.getParameters().get("service");
		String selectServiceType = didUrl.getParameters().get("service-type");

		if (selectServiceName != null || selectServiceType != null) {

			selectedServices = selectServices(resolveResult.getDidDocument(), selectServiceName, selectServiceType).keySet().toArray(new Integer[0]);

			if (log.isDebugEnabled()) log.debug("Selected services: " + Arrays.asList(selectedServices));
		}

		Integer[] selectedVerificationMethods = null;

		String selectKeyName = didUrl.getParameters().get("key");
		String selectKeyType = didUrl.getParameters().get("key-type");

		if (selectKeyName != null || selectKeyType != null) {

			selectedVerificationMethods = selectKeys(resolveResult.getDidDocument(), selectKeyName, selectKeyType).keySet().toArray(new Integer[0]);

			if (log.isDebugEnabled()) log.debug("Selected keys: " + Arrays.asList(selectedVerificationMethods));
		}

		if (selectedServices != null) resolveResult.getDidResolutionMetadata().put("selectedServices", selectedServices);
		if (selectedVerificationMethods != null) resolveResult.getDidResolutionMetadata().put("selectedVerificationMethods", selectedVerificationMethods);

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

				if (! Arrays.asList(service.getTypes()).contains(selectServiceType)) continue;
			}

			selectedServices.put(Integer.valueOf(i), service);
		}

		return selectedServices;
	}

	public static Map<Integer, VerificationMethod> selectKeys(DIDDocument didDocument, String selectKeyName, String selectKeyType) {

		int i = -1;
		Map<Integer, VerificationMethod> selectedKeys = new HashMap<Integer, VerificationMethod> ();
		if (didDocument.getVerificationMethods() == null) return selectedKeys;

		for (VerificationMethod verificationMethod : didDocument.getVerificationMethods()) {

			i++;

			if (selectKeyName != null && verificationMethod.getId() != null) {

				DIDURL verificationMethodDidUrl;
				try { verificationMethodDidUrl = DIDURL.fromUri(verificationMethod.getId()); } catch (ParserException ex) { verificationMethodDidUrl = null; }
				String verificationMethodName = verificationMethodDidUrl == null ? null : verificationMethodDidUrl.getFragment();

				if (verificationMethodName == null) continue;
				if (! verificationMethodName.equals(selectKeyName)) continue;
			}

			if (selectKeyType != null && verificationMethod.getTypes() != null) {

				if (! Arrays.asList(verificationMethod.getTypes()).contains(selectKeyType)) continue;
			}

			selectedKeys.put(Integer.valueOf(i), verificationMethod);
		}

		return selectedKeys;
	}
}
