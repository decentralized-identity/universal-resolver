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
        return this.dereference(didUrlString, dereferenceOptions, null);
    }

    public DereferenceResult dereference(String didUrlString, Map<String, Object> dereferenceOptions, Map<String, Object> initialExecutionState) throws ResolutionException, DereferencingException {

        if (log.isDebugEnabled()) log.debug("dereference(" + didUrlString + ")  with options: " + dereferenceOptions);

        if (didUrlString == null) throw new NullPointerException();

        // prepare dereference result

        final DIDURL didUrl;
        final DIDURL didUrlWithoutFragment;
        final String didUrlFragment;
        DereferenceResult dereferenceResult = DereferenceResult.build();
        ExtensionStatus extensionStatus = new ExtensionStatus();

        // prepare execution state

        Map<String, Object> executionState = new HashMap<>();
        if (initialExecutionState != null) executionState.putAll(initialExecutionState);

        // parse

        try {

            didUrl = DIDURL.fromString(didUrlString);
            didUrlWithoutFragment = DIDURL.fromUri(didUrl.getUriWithoutFragment());
            didUrlFragment = didUrl.getFragment() == null ? null : "#" + didUrl.getFragment();
            if (log.isDebugEnabled()) log.debug("DID URL " + didUrlString + " is valid: " + didUrl);
        } catch (IllegalArgumentException | ParserException ex) {

            String errorMessage = ex.getMessage();
            if (log.isWarnEnabled()) log.warn(errorMessage);
            throw new DereferencingException(DereferencingException.ERROR_INVALIDDIDURL, errorMessage, ex);
        }

        // [before dereference]

        this.executeExtensions(DereferencerExtension.BeforeDereferenceDereferencerExtension.class, extensionStatus, e -> e.beforeDereference(didUrl, dereferenceOptions, dereferenceResult, executionState, this), dereferenceOptions, dereferenceResult, executionState);

        // [resolve]

        final ResolveResult resolveResult;

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
        } else {
            resolveResult = null;
        }

        // [dereference primary]

        this.executeExtensions(DereferencerExtension.DereferencePrimaryDereferencerExtension.class, extensionStatus, e -> e.dereferencePrimary(didUrlWithoutFragment, dereferenceOptions, resolveResult, dereferenceResult, executionState, this), dereferenceOptions, dereferenceResult, executionState);

        // nothing found?

        if (! dereferenceResult.isComplete()) {
            if (log.isInfoEnabled()) log.info("Primary dereference result is incomplete: " + dereferenceResult);
            throw new DereferencingException(DereferencingException.ERROR_NOTFOUND, "No dereference result for " + didUrlString, dereferenceResult.getDereferencingMetadata());
        }

        // [dereference secondary]

        this.executeExtensions(DereferencerExtension.DereferenceSecondaryDereferencerExtension.class, extensionStatus, e -> e.dereferenceSecondary(didUrlWithoutFragment, didUrlFragment, dereferenceOptions, dereferenceResult, executionState, this), dereferenceOptions, dereferenceResult, executionState);

        // nothing found?

        if (! dereferenceResult.isComplete()) {
            if (log.isInfoEnabled()) log.info("Secondary dereference result is incomplete: " + dereferenceResult);
            throw new DereferencingException(DereferencingException.ERROR_NOTFOUND, "No dereference result for " + didUrlString, dereferenceResult.getDereferencingMetadata());
        }

        // [after dereference]

        this.executeExtensions(DereferencerExtension.AfterDereferenceDereferencerExtension.class, extensionStatus, e -> e.afterDereference(didUrl, dereferenceOptions, dereferenceResult, executionState, this), dereferenceOptions, dereferenceResult, executionState);

        // additional metadata

        dereferenceResult.getDereferencingMetadata().put("didUrl", didUrl.toMap(false));

        // done

        if (log.isInfoEnabled()) log.info("Final dereference result: " + dereferenceResult);

        return dereferenceResult;
    }

    private <E extends DereferencerExtension> void executeExtensions(Class<E> extensionClass, ExtensionStatus extensionStatus, DereferencerExtension.ExtensionFunction<E> extensionFunction, Map<String, Object> dereferenceOptions, DereferenceResult dereferenceResult, Map<String, Object> executionState) throws ResolutionException, DereferencingException {

        String extensionStage = extensionClass.getAnnotation(DereferencerExtension.ExtensionStage.class).value();

        List<E> extensions = this.getExtensions().stream().filter(extensionClass::isInstance).map(extensionClass::cast).toList();
        if (log.isDebugEnabled()) log.debug("EXTENSIONS (" + extensionStage + "), TRYING: {}", DereferencerExtension.extensionClassNames(extensions));

        List<DereferencerExtension> skippedExtensions = new ArrayList<>();
        List<DereferencerExtension> inapplicableExtensions = new ArrayList<>();

        for (E extension : extensions) {
            if (extensionStatus.skip(extensionStage)) { skippedExtensions.add(extension); continue; }
            String beforeDereferenceOptions = "" + dereferenceOptions;
            String beforeDereferenceResult = "" + dereferenceResult;
            String beforeExecutionState = "" + executionState;
            ExtensionStatus returnedExtensionStatus = extensionFunction.apply(extension);
            extensionStatus.or(returnedExtensionStatus);
            if (returnedExtensionStatus == null) { inapplicableExtensions.add(extension); continue; }
            String afterDereferenceOptions = "" + dereferenceOptions;
            String afterDereferenceResult = "" + dereferenceResult;
            String afterExecutionState = "" + executionState;
            String changedDereferenceOptions = afterDereferenceOptions.equals(beforeDereferenceOptions) ? "(unchanged)" : afterDereferenceOptions;
            String changedDereferenceResult = afterDereferenceResult.equals(beforeDereferenceResult) ? "(unchanged)" : afterDereferenceResult;
            String changedExecutionState = afterExecutionState.equals(beforeExecutionState) ? "(unchanged)" : afterExecutionState;
            if (log.isDebugEnabled()) log.debug("Executed extension (" + extensionStage + ") " + extension.getClass().getSimpleName() + " with dereference options " + changedDereferenceOptions + " and dereference result " + changedDereferenceResult + " and execution state " + changedExecutionState);
        }

        if (log.isDebugEnabled()) {
            List<E> executedExtensions = extensions.stream().filter(e -> ! skippedExtensions.contains(e)).filter(e -> ! inapplicableExtensions.contains(e)).toList();
            log.debug("EXTENSIONS (" + extensionStage + "), EXECUTED: {}, SKIPPED: {}, INAPPLICABLE: {}", DereferencerExtension.extensionClassNames(executedExtensions), DereferencerExtension.extensionClassNames(skippedExtensions), DereferencerExtension.extensionClassNames(inapplicableExtensions));
        }
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
