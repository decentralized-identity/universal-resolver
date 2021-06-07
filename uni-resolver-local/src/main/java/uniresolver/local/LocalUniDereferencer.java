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
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LocalUniDereferencer implements UniDereferencer {

    private static final Logger log = LoggerFactory.getLogger(LocalUniDereferencer.class);

    private UniResolver uniResolver;

    public LocalUniDereferencer() {

    }

    public LocalUniDereferencer(UniResolver uniResolver) {
        this.uniResolver = uniResolver;
    }

    @Override
    public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions) throws DereferencingException {

        if (didUrlString == null) throw new NullPointerException();

        // prepare dereference result

        DereferenceResult dereferenceResult = DereferenceResult.build();

        // parse DID URL

        DIDURL didUrl = null;

        try {

            didUrl = DIDURL.fromString(didUrlString);
            if (log.isDebugEnabled()) log.debug("DID URL " + didUrlString + " is valid: " + didUrl);
        } catch (IllegalArgumentException | ParserException ex) {

            String errorMessage = "DID URL " + didUrlString + " is not valid: " + ex.getMessage();
            if (log.isWarnEnabled()) log.warn(errorMessage);
            throw new DereferencingException(DereferenceResult.makeErrorResult(DereferenceResult.Error.invalidDidUrl, errorMessage));
        }

        // resolve DID

        Map<String, Object> resolveOptions = new HashMap<>();
        if (dereferenceOptions.containsKey("accept")) resolveOptions.put("accept", dereferenceOptions.get("accept"));

        ResolveResult resolveResult = null;
        try {
            resolveResult = this.uniResolver.resolve(didUrl.getDid().getDidString(), resolveOptions);
        } catch (ResolutionException ex) {
            throw new DereferencingException("Cannot resolve DID " + didUrl.getDid().getDidString() + ": " + ex.getMessage(), ex);
        }

        DIDDocument didDocument = resolveResult.getDidDocument();

        // dereference

        if (resolveResult.getDidResolutionMetadata().containsKey("contentType")) {
            dereferenceResult.getDereferencingMetadata().put("contentType", (String) resolveResult.getDidResolutionMetadata().get("contentType"));
        }

        if (didUrl.isBareDid()) {
            if (log.isDebugEnabled()) log.debug("Dereferencing DID URL that is a bare DID: " + didUrl);
            dereferenceResult.setContentStream(resolveResult.getDidDocumentStream());
            dereferenceResult.setContentMetadata(resolveResult.getDidDocumentMetadata());
        }

        if (didUrl.getFragment() != null && didUrl.getUriWithoutFragment().equals(didUrl.getDid().toUri())) {
            if (log.isDebugEnabled()) log.debug("Dereferencing DID URL with a fragment: " + didUrl);
            JsonLDObject jsonLdObject = JsonLDDereferencer.findByIdInJsonLdObject(didDocument, didUrl.toUri(), didUrl.getDid().toUri());
            dereferenceResult.setContentStream(jsonLdObject.toJson().getBytes(StandardCharsets.UTF_8));
            dereferenceResult.setContentMetadata(resolveResult.getDidDocumentMetadata());
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
}
