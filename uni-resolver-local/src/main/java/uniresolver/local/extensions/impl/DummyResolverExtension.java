package uniresolver.local.extensions.impl;

import foundation.identity.did.DID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniResolver;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.ResolverExtension;
import uniresolver.result.ResolveResult;

import java.util.Map;

public class DummyResolverExtension implements ResolverExtension {

	private static Logger log = LoggerFactory.getLogger(DummyResolverExtension.class);

	@Override
	public ExtensionStatus afterResolve(DID did, Map<String, Object> resolutionOptions, ResolveResult resolveResult, boolean resolveRepresentation, LocalUniResolver localUniResolver) throws ResolutionException {

		if (log.isDebugEnabled()) log.debug("Dummy extension called!");
		return ExtensionStatus.DEFAULT;
	}
}
