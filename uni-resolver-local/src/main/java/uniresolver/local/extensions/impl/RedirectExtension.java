package uniresolver.local.extensions.impl;

import foundation.identity.did.DID;
import foundation.identity.did.DIDURL;
import foundation.identity.did.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.local.extensions.Extension;
import uniresolver.local.extensions.Extension.AbstractExtension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.result.ResolveResult;

import java.util.HashMap;
import java.util.Map;

public class RedirectExtension extends AbstractExtension implements Extension {

	private static Logger log = LoggerFactory.getLogger(RedirectExtension.class);

	@Override
	public ExtensionStatus afterResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException {

		if (! resolveResult.getDidDocumentMetadata().containsKey("redirect")) return ExtensionStatus.DEFAULT;

		while (resolveResult.getDidDocumentMetadata().containsKey("redirect")) {

			String redirectedIdentifier = (String) resolveResult.getDidDocumentMetadata().get("redirect");
			DID redirectedDid = null;
			try {
				redirectedDid = DID.fromString(redirectedIdentifier);
			} catch (ParserException ex) {
				throw new ResolutionException("Invalid redirected DID " + redirectedIdentifier + ": " + ex.getMessage(), ex);
			}
			if (log.isDebugEnabled()) log.debug("Resolving redirected DID: " + redirectedDid);

			ResolveResult previousResolveResult = ResolveResult.build();
			previousResolveResult.setDidResolutionMetadata(resolveResult.getDidResolutionMetadata());
			previousResolveResult.setDidDocument(resolveResult.getDidDocument());
			previousResolveResult.setDidDocumentStream(resolveResult.getDidDocumentStream());
			previousResolveResult.setDidDocumentMetadata(resolveResult.getDidDocumentMetadata());

			ResolveResult driverResolveResult = ResolveResult.build();
			localUniResolver.resolveOrResolveRepresentationWithDrivers(redirectedDid, resolutionOptions, driverResolveResult, resolveRepresentation);

			resolveResult.setDidResolutionMetadata(new HashMap<>());
			resolveResult.getDidResolutionMetadata().putAll(driverResolveResult.getDidResolutionMetadata());
			resolveResult.getDidResolutionMetadata().put("previous", previousResolveResult);
			resolveResult.setDidDocument(driverResolveResult.getDidDocument());
			resolveResult.setDidDocumentStream(driverResolveResult.getDidDocumentStream());
			resolveResult.setDidDocumentMetadata(driverResolveResult.getDidDocumentMetadata());
		}

		return ExtensionStatus.DEFAULT;
	}
}
