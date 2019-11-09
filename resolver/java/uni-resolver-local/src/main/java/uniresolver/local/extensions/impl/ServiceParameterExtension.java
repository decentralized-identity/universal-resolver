package uniresolver.local.extensions.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import did.DIDDocument;
import did.DIDURL;
import did.PublicKey;
import did.Service;
import did.parser.ParserException;
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

			selectedServices = selectServices(resolveResult.getDidDocument(), selectServiceName, selectServiceType).keySet().toArray(new Integer[0]);

			if (log.isDebugEnabled()) log.debug("Selected services: " + Arrays.asList(selectedServices));
		}

		Integer[] selectedKeys = null;

		String selectKeyName = didUrl.getParametersMap().get("key");
		String selectKeyType = didUrl.getParametersMap().get("key-type");

		if (selectKeyName != null || selectKeyType != null) {

			selectedKeys = selectKeys(resolveResult.getDidDocument(), selectKeyName, selectKeyType).keySet().toArray(new Integer[0]);

			if (log.isDebugEnabled()) log.debug("Selected keys: " + Arrays.asList(selectedKeys));
		}

		if (selectedServices != null) resolveResult.getResolverMetadata().put("selectedServices", selectedServices);
		if (selectedKeys != null) resolveResult.getResolverMetadata().put("selectedKeys", selectedKeys);

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
				try { serviceDidUrl = DIDURL.fromString(service.getId()); } catch (ParserException ex) { serviceDidUrl = null; }
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

	public static Map<Integer, PublicKey> selectKeys(DIDDocument didDocument, String selectKeyName, String selectKeyType) {

		int i = -1;
		Map<Integer, PublicKey> selectedKeys = new HashMap<Integer, PublicKey> ();
		if (didDocument.getPublicKeys() == null) return selectedKeys;

		for (PublicKey publicKey : didDocument.getPublicKeys()) {

			i++;

			if (selectKeyName != null && publicKey.getId() != null) {

				DIDURL publicKeyDidUrl;
				try { publicKeyDidUrl = DIDURL.fromString(publicKey.getId()); } catch (ParserException ex) { publicKeyDidUrl = null; }
				String publicKeyName = publicKeyDidUrl == null ? null : publicKeyDidUrl.getFragment();

				if (publicKeyName == null) continue;
				if (! publicKeyName.equals(selectKeyName)) continue;
			}

			if (selectKeyType != null && publicKey.getTypes() != null) {

				if (! Arrays.asList(publicKey.getTypes()).contains(selectKeyType)) continue;
			}

			selectedKeys.put(Integer.valueOf(i), publicKey);
		}

		return selectedKeys;
	}
}
