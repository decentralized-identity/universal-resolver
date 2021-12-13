package uniresolver.local;

import foundation.identity.did.DIDURL;
import foundation.identity.did.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.UniDereferencer;
import uniresolver.UniResolver;
import uniresolver.local.configuration.LocalUniResolverConfigurator;
import uniresolver.local.extensions.DereferencerExtension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.impl.DIDDocumentExtension;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalUniDereferencer implements UniDereferencer {

    public static final List<DereferencerExtension> DEFAULT_EXTENSIONS = List.of(
            new DIDDocumentExtension()
    );

    private static final Logger log = LoggerFactory.getLogger(LocalUniDereferencer.class);

    private UniResolver uniResolver;

    private List<DereferencerExtension> extensions = new ArrayList<>(DEFAULT_EXTENSIONS);

    public LocalUniDereferencer() {

    }

    public LocalUniDereferencer(UniResolver uniResolver) {
        this.uniResolver = uniResolver;
    }

    /*
     * Factory methods
     */

    public static LocalUniDereferencer fromConfigFile(String filePath) throws IOException {

        LocalUniResolver localUniResolver = new LocalUniResolver();
        LocalUniResolverConfigurator.configureLocalUniResolver(filePath, localUniResolver);

        LocalUniDereferencer localUniDereferencer = new LocalUniDereferencer();
        localUniDereferencer.setUniResolver(localUniResolver);

        return localUniDereferencer;
    }

    /*
     * Dereferencer methods
     */

    @Override
    public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions) throws ResolutionException, DereferencingException {

        if (log.isDebugEnabled()) log.debug("dereference(" + didUrlString + ")  with options: " + dereferenceOptions);

        if (didUrlString == null) throw new NullPointerException();

        // prepare dereference result

        DIDURL didUrl = null;
        DIDURL didUrlWithoutFragment = null;
        String didUrlFragment = null;
        DereferenceResult dereferenceResult = DereferenceResult.build();
        ExtensionStatus extensionStatus = new ExtensionStatus();

        // parse

        try {

            didUrl = DIDURL.fromString(didUrlString);
            didUrlWithoutFragment = DIDURL.fromUri(didUrl.getUriWithoutFragment());
            didUrlFragment = didUrl.getFragment() == null ? null : "#" + didUrl.getFragment();
            if (log.isDebugEnabled()) log.debug("DID URL " + didUrlString + " is valid: " + didUrl);
        } catch (IllegalArgumentException | ParserException ex) {

            String errorMessage = ex.getMessage();
            if (log.isWarnEnabled()) log.warn(errorMessage);
            throw new DereferencingException(DereferenceResult.ERROR_INVALIDDIDURL, errorMessage, ex);
        }

        // [before dereference]

        if (! extensionStatus.skipBeforeDereference()) {
            for (DereferencerExtension extension : this.getExtensions()) {
                extensionStatus.or(extension.beforeDereference(didUrl, dereferenceOptions, dereferenceResult, this));
                if (extensionStatus.skipBeforeDereference()) break;
            }
        }

        // [resolve]

        ResolveResult resolveResult = null;

        if (! extensionStatus.skipResolve()) {

            // dereference options + DID parameters = resolve options

            Map<String, Object> resolveOptions = new HashMap<>(dereferenceOptions);
            if (didUrl.getParameters() != null) resolveOptions.putAll(didUrl.getParameters());

            // resolve

            resolveResult = this.uniResolver.resolveRepresentation(didUrl.getDid().getDidString(), resolveOptions);

            // dereferencing metadata = DID resolution metadata - content type

            if (resolveResult != null) {
                dereferenceResult.getDereferencingMetadata().putAll(resolveResult.getDidResolutionMetadata());
                dereferenceResult.setContentType(null);
            }
        }

        // [dereference primary]

        if (! extensionStatus.skipDereferencePrimary()) {
            if (log.isInfoEnabled()) log.info("Dereferencing (primary): " + didUrlWithoutFragment);
            for (DereferencerExtension extension : this.getExtensions()) {
                extensionStatus.or(extension.dereferencePrimary(didUrlWithoutFragment, dereferenceOptions, resolveResult, dereferenceResult, this));
                if (extensionStatus.skipDereferencePrimary()) break;
            }
        }

        // [dereference secondary]

        if (! extensionStatus.skipDereferenceSecondary()) {
            if (log.isInfoEnabled()) log.info("Dereferencing (secondary): " + didUrlWithoutFragment + ", " + didUrlFragment);
            for (DereferencerExtension extension : this.getExtensions()) {
                extensionStatus.or(extension.dereferenceSecondary(didUrlWithoutFragment, didUrlFragment, dereferenceOptions, dereferenceResult, this));
                if (extensionStatus.skipDereferenceSecondary()) break;
            }
        }

        // [after dereference]

        if (! extensionStatus.skipAfterDereference()) {
            for (DereferencerExtension extension : this.getExtensions()) {
                extensionStatus.or(extension.afterDereference(didUrl, dereferenceOptions, dereferenceResult, this));
                if (extensionStatus.skipAfterDereference()) break;
            }
        }

        // additional metadata

        dereferenceResult.getDereferencingMetadata().put("didUrl", didUrl.toMap(false));

        // nothing found?

        if (! dereferenceResult.isComplete()) {
            if (log.isInfoEnabled()) log.info("Dereference result is incomplete: " + dereferenceResult);
            throw new DereferencingException(DereferenceResult.ERROR_NOTFOUND, "No dereference result for " + didUrlString, dereferenceResult.getDereferencingMetadata());
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

    public List<DereferencerExtension> getExtensions() {
        return this.extensions;
    }

    public void setExtensions(List<DereferencerExtension> extensions) {
        this.extensions = extensions;
    }
}
