package uniresolver.local.extensions.impl;

import java.util.Map;

import foundation.identity.did.DIDURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.local.extensions.Extension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.Extension.AbstractExtension;
import uniresolver.result.ResolveResult;

public class RedirectExtension extends AbstractExtension implements Extension {

	private static Logger log = LoggerFactory.getLogger(RedirectExtension.class);

	@Override
	public ExtensionStatus afterResolve(String identifier, DIDURL didUrl, Map<String, String> options, ResolveResult resolveResult, LocalUniResolver localUniResolver) throws ResolutionException {

		if (! resolveResult.getDidDocumentMetadata().containsKey("redirect")) return ExtensionStatus.DEFAULT;

		while (resolveResult.getDidDocumentMetadata().containsKey("redirect")) {

			String resolveIdentifier = (String) resolveResult.getDidDocumentMetadata().get("redirect");
			if (log.isDebugEnabled()) log.debug("Resolving identifier: " + resolveIdentifier);

			ResolveResult previousResolveResult = resolveResult.copy();
			resolveResult.reset();
			resolveResult.getDidResolutionMetadata().put("previous", previousResolveResult);

			ResolveResult driverResolveResult = ResolveResult.build();
			localUniResolver.resolveWithDrivers(resolveIdentifier, driverResolveResult);

			resolveResult.setDidDocument(driverResolveResult.getDidDocument());
			resolveResult.setDidDocumentMetadata(driverResolveResult.getDidDocumentMetadata());

			resolveResult.getDidResolutionMetadata().putAll(driverResolveResult.getDidResolutionMetadata());
		}

		return ExtensionStatus.DEFAULT;
	}
}
