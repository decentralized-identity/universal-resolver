package uniresolver.local.extensions.impl;

import foundation.identity.did.DIDURL;
import foundation.identity.did.representations.Representations;
import foundation.identity.did.representations.production.RepresentationProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniDereferencer;
import uniresolver.local.extensions.DereferencerExtension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.util.Map;

public class DIDDocumentExtension implements DereferencerExtension.DereferencePrimaryDereferencerExtension {

    private static final Logger log = LoggerFactory.getLogger(DIDDocumentExtension.class);

    @Override
    public ExtensionStatus dereferencePrimary(DIDURL didUrlWithoutFragment, Map<String, Object> dereferenceOptions, ResolveResult resolveResult, DereferenceResult dereferenceResult, Map<String, Object> executionState, LocalUniDereferencer localUniDereferencer) throws ResolutionException, DereferencingException {

        // check inputs

        String accept = (String) dereferenceOptions.get("accept");

        if (didUrlWithoutFragment.getPath() != null) {
            if (log.isDebugEnabled()) log.debug("Skipping (DID URL has path).");
            return null;
        }

        if (resolveResult == null) {
            if (log.isDebugEnabled()) log.debug("Skipping (no resolve result).");
            return null;
        }

        if (log.isInfoEnabled()) log.info("Executing dereferencePrimary() with extension " + this.getClass().getName());

        // dereference

        if (! Representations.isProducibleMediaType(accept)) {
            throw new DereferencingException(DereferencingException.ERROR_CONTENTTYPENOTSUPPORTED, "Content type not supported: " + accept);
        }

        if (log.isDebugEnabled()) log.debug("Dereferencing DID URL that has no path (assuming DID document): " + didUrlWithoutFragment);

        RepresentationProducer representationProducer = Representations.getProducer(accept);
        byte[] content;
        try {
            content = representationProducer.produce(resolveResult.getDidDocument());
        } catch (IOException ex) {
            throw new DereferencingException(DereferencingException.ERROR_CONTENTTYPENOTSUPPORTED, "Cannot produce DID document in content type: " + accept);
        }

        // set dereference result

        dereferenceResult.setContentType(resolveResult.getContentType());
        dereferenceResult.setContent(content);
        dereferenceResult.setContentMetadata(resolveResult.getDidDocumentMetadata());

        // done

        return ExtensionStatus.SKIP_DEREFERENCE_PRIMARY;
    }
}
