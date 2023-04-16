package uniresolver.local.extensions.impl;

import foundation.identity.did.DIDURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniDereferencer;
import uniresolver.local.extensions.DereferencerExtension;
import uniresolver.local.extensions.DereferencerExtension.AbstractDereferencerExtension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.result.DereferenceResult;

import java.util.Map;

public class DummyDereferencerExtension extends AbstractDereferencerExtension implements DereferencerExtension {

	private static Logger log = LoggerFactory.getLogger(DummyDereferencerExtension.class);

	@Override
	public ExtensionStatus afterDereference(DIDURL didUrl, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {

		if (log.isDebugEnabled()) log.debug("Dummy extension called!");
		return ExtensionStatus.DEFAULT;
	}
}
