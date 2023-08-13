package uniresolver.local.extensions.impl;

import foundation.identity.did.DIDURL;
import foundation.identity.did.representations.Representations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.local.LocalUniDereferencer;
import uniresolver.local.extensions.DereferencerExtension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;

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

        if (! Representations.isRepresentationMediaType(accept)) {
            throw new DereferencingException(DereferencingException.ERROR_CONTENTTYEPNOTSUPPORTED, "Content type not supported: " + accept);
        }

        ResolveRepresentationResult resolveRepresentationResult = resolveResult.toResolveRepresentationResult(accept);

        if (log.isDebugEnabled()) log.debug("Dereferencing DID URL that has no path (assuming DID document): " + didUrlWithoutFragment);

        // update result

        dereferenceResult.setContentType(resolveRepresentationResult.getContentType());
        dereferenceResult.setContentStream(resolveRepresentationResult.getDidDocumentStream());
        dereferenceResult.setContentMetadata(resolveRepresentationResult.getDidDocumentMetadata());

        // done

        return ExtensionStatus.SKIP_DEREFERENCE_PRIMARY;
    }
}
