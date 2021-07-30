package uniresolver.local;

import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import foundation.identity.did.parser.ParserException;
import foundation.identity.jsonld.JsonLDDereferencer;
import foundation.identity.jsonld.JsonLDObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.UniDereferencer;
import uniresolver.UniResolver;
import uniresolver.driver.Driver;
import uniresolver.local.extensions.Extension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;
import uniresolver.util.ResolveResultUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalUniDereferencer implements UniDereferencer {

    private static final Logger log = LoggerFactory.getLogger(LocalUniDereferencer.class);

    private UniResolver uniResolver;

    private List<Extension> extensions = new ArrayList<Extension>();

    public LocalUniDereferencer() {

    }

    public LocalUniDereferencer(UniResolver uniResolver) {
        this.uniResolver = uniResolver;
    }

    @Override
    public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions) throws DereferencingException {

        if (didUrlString == null) throw new NullPointerException();

        // prepare dereference result

        DIDURL didUrl = null;
        DereferenceResult dereferenceResult = DereferenceResult.build();
        ExtensionStatus extensionStatus = new ExtensionStatus();

        // check options

        String accept = (String) dereferenceOptions.get("accept");

        // parse

        try {

            didUrl = DIDURL.fromString(didUrlString);
            if (log.isDebugEnabled()) log.debug("DID URL " + didUrlString + " is valid: " + didUrl);
        } catch (IllegalArgumentException | ParserException ex) {

            String errorMessage = ex.getMessage();
            if (log.isWarnEnabled()) log.warn(errorMessage);

            throw new DereferencingException(DereferenceResult.makeErrorDereferenceResult(DereferenceResult.ERROR_INVALIDDIDURL, errorMessage, accept));
        }

        // [resolve]

        Map<String, Object> resolveOptions = new HashMap<>(dereferenceOptions);

        ResolveResult resolveRepresentationResult;
        ResolveResult resolveResult;
        DIDDocument didDocument;

        try {

            resolveRepresentationResult = this.uniResolver.resolveRepresentation(didUrl.getDid().getDidString(), resolveOptions);
            resolveResult = ResolveResultUtil.convertToResolveResult(resolveRepresentationResult);
            didDocument = resolveResult.getDidDocument();
        } catch (ResolutionException ex) {

            if (ex.getResolveResult() != null && ex.getResolveResult().isErrorResult()) {
                if (ResolveResult.ERROR_INVALIDDID.equals(ex.getResolveResult().getError()))
                    throw new DereferencingException(DereferenceResult.makeErrorDereferenceResult(DereferenceResult.ERROR_INVALIDDIDURL, "Error " + ex.getResolveResult().getError() + " from resolver: " + ex.getResolveResult().getErrorMessage(), accept));
                else if (ResolveResult.ERROR_NOTFOUND.equals(ex.getResolveResult().getError()))
                    throw new DereferencingException(DereferenceResult.makeErrorDereferenceResult(DereferenceResult.ERROR_NOTFOUND, "Error " + ex.getResolveResult().getError() + " from resolver: " + ex.getResolveResult().getErrorMessage(), accept));
                else
                    throw new DereferencingException(DereferenceResult.makeErrorDereferenceResult(DereferenceResult.ERROR_INTERNALERROR, "Error " + ex.getResolveResult().getError() + " from resolver: " + ex.getResolveResult().getErrorMessage(), accept));
            }
            throw new DereferencingException("Cannot resolve DID " + didUrl.getDid().getDidString() + ": " + ex.getMessage(), ex);
        }

        dereferenceResult.getDereferencingMetadata().putAll(resolveRepresentationResult.getDidResolutionMetadata());

        // [before dereference]

        if (! extensionStatus.skipBeforeDereference()) {
            for (Extension extension : this.getExtensions()) {
                extensionStatus.or(extension.beforeDereference(didUrl, dereferenceOptions, dereferenceResult, didDocument, this));
                if (extensionStatus.skipBeforeDereference()) break;
            }
        }

        // [dereference]

        if (! extensionStatus.skipResolve()) {

            if (didUrl.isBareDid()) {

                if (log.isDebugEnabled()) log.debug("Dereferencing DID URL that is a bare DID: " + didUrl);
                dereferenceResult.setContentStream(resolveRepresentationResult.getDidDocumentStream());
                dereferenceResult.setContentMetadata(resolveRepresentationResult.getDidDocumentMetadata());
            }

            if (didUrl.getFragment() != null && didUrl.getUriWithoutFragment().equals(didUrl.getDid().toUri())) {

                if (log.isDebugEnabled()) log.debug("Dereferencing DID URL with a fragment: " + didUrl);
                JsonLDObject jsonLdObject = JsonLDDereferencer.findByIdInJsonLdObject(didDocument, didUrl.toUri(), didUrl.getDid().toUri());
                if (jsonLdObject == null) {
                    throw new DereferencingException(DereferenceResult.makeErrorDereferenceResult(DereferenceResult.ERROR_NOTFOUND, "Fragment not found: " + didUrl.getFragment(), accept));
                }
                dereferenceResult.setContentType("application/ld+json");
                dereferenceResult.setContentStream(jsonLdObject.toJson().getBytes(StandardCharsets.UTF_8));
                dereferenceResult.setContentMetadata(resolveRepresentationResult.getDidDocumentMetadata());
            }
        }

        // [after dereference]

        if (! extensionStatus.skipAfterDereference()) {
            for (Extension extension : this.getExtensions()) {
                extensionStatus.or(extension.afterDereference(didUrl, dereferenceOptions, dereferenceResult, didDocument, this));
                if (extensionStatus.skipAfterDereference()) break;
            }
        }

        // done

        return dereferenceResult;
    }

    /*
     * Getters and setters
     */

    public UniResolver getUniResolver() {
        return uniResolver;
    }

    public void setUniResolver(UniResolver uniResolver) {
        this.uniResolver = uniResolver;
    }

    public List<Extension> getExtensions() {
        return this.extensions;
    }

    public void setExtensions(List<Extension> extensions) {
        this.extensions = extensions;
    }
}
