package uniresolver.local;

import foundation.identity.did.DIDURL;
import foundation.identity.did.parser.ParserException;
import foundation.identity.did.representations.Representations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.UniDereferencer;
import uniresolver.UniResolver;
import uniresolver.driver.Driver;
import uniresolver.driver.http.HttpDriver;
import uniresolver.driver.util.HttpBindingServerUtil;
import uniresolver.local.configuration.LocalUniResolverConfigurator;
import uniresolver.local.extensions.DereferencerExtension;
import uniresolver.local.extensions.ExtensionStatus;
import uniresolver.local.extensions.impl.DIDDocumentExtension;
import uniresolver.local.extensions.util.ExecutionStateUtil;
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
        if (this.getUniResolver() == null) throw new ResolutionException("No resolver configured.");

        // start time

        long start = System.currentTimeMillis();

        // prepare execution state

        Map<String, Object> executionState = new HashMap<>();
        if (initialExecutionState != null) executionState.putAll(initialExecutionState);

        // prepare dereference result

        final DIDURL didUrl;
        final DIDURL didUrlWithoutFragment;
        final String didUrlFragment;
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
            throw new DereferencingException(DereferencingException.ERROR_INVALIDDIDURL, errorMessage, ex);
        }

        // [before dereference]

        this.executeExtensions(DereferencerExtension.BeforeDereferenceDereferencerExtension.class, extensionStatus, e -> e.beforeDereference(didUrl, dereferenceOptions, dereferenceResult, executionState, this), dereferenceOptions, dereferenceResult, executionState);

        // [dereference primary] with drivers

        if (! extensionStatus.skipDereferencePrimary()) {

            if (log.isInfoEnabled()) log.info("Dereferencing DID URL with drivers: " + didUrlWithoutFragment);

            long driverStart = System.currentTimeMillis();
            DereferenceResult driverDereferenceResult = this.dereferencePrimaryWithDrivers(didUrlWithoutFragment, dereferenceOptions);
            long driverStop = System.currentTimeMillis();
            dereferenceResult.getDereferencingMetadata().put("driverDuration", driverStop - driverStart);

            if (driverDereferenceResult != null) {
                dereferenceResult.setContent(driverDereferenceResult.getContent());
                if (driverDereferenceResult.getDereferencingMetadata() != null) dereferenceResult.getDereferencingMetadata().putAll(driverDereferenceResult.getDereferencingMetadata());
                if (driverDereferenceResult.getContentMetadata() != null) dereferenceResult.getContentMetadata().putAll(driverDereferenceResult.getContentMetadata());
            }
        }

        // [dereference primary]

        if (! dereferenceResult.isComplete() && ! extensionStatus.skipDereferencePrimary()) {

            if (log.isInfoEnabled()) log.info("Dereferencing DID URL with resolver: " + didUrlWithoutFragment);

            // [resolve]

            final ResolveResult resolveResult;

            if (! extensionStatus.skipResolve()) {

                // resolve options = dereference options + DID parameters

                Map<String, Object> resolveOptions = new HashMap<>(dereferenceOptions);
                resolveOptions.put("accept", Representations.DEFAULT_MEDIA_TYPE);
                if (didUrl.getParameters() != null) resolveOptions.putAll(didUrl.getParameters());

                // resolve

                resolveResult = this.getUniResolver().resolve(didUrl.getDid().getDidString(), resolveOptions);

                // dereferencing metadata

                if (resolveResult != null) {
                    dereferenceResult.getDereferencingMetadata().put("didResolutionMetadata", resolveResult.getDidResolutionMetadata());
                }
            } else {
                resolveResult = null;
            }

            // [dereference primary]

            this.executeExtensions(DereferencerExtension.DereferencePrimaryDereferencerExtension.class, extensionStatus, e -> e.dereferencePrimary(didUrlWithoutFragment, dereferenceOptions, resolveResult, dereferenceResult, executionState, this), dereferenceOptions, dereferenceResult, executionState);
        }

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

        long stop = System.currentTimeMillis();
        dereferenceResult.getDereferencingMetadata().put("duration", stop - start);
        dereferenceResult.getDereferencingMetadata().put("didUrl", didUrl.toMap(false));

        // done

        if (log.isInfoEnabled()) log.info("Final dereference result: " + dereferenceResult);
        return dereferenceResult;
    }

    public DereferenceResult dereferencePrimaryWithDrivers(DIDURL didUrl, Map<String, Object> dereferenceOptions) throws DereferencingException, ResolutionException {

        if (! (this.getUniResolver() instanceof LocalUniResolver)) {
            log.debug("Cannot dereference with drivers, no drivers available: " + didUrl);
            return null;
        }

        DereferenceResult driverDereferenceResult = null;
        Driver usedDriver = null;

        for (Driver driver : ((LocalUniResolver) this.getUniResolver()).getDrivers()) {

            if (driver instanceof HttpDriver httpDriver && ! httpDriver.getSupportsDereference()) continue;

            if (log.isDebugEnabled()) log.debug("Attempting to dereference " + didUrl + " with driver " + driver.getClass().getSimpleName());

            driverDereferenceResult = driver.dereference(didUrl, dereferenceOptions);

            if (driverDereferenceResult != null) {
                usedDriver = driver;
                break;
            }
        }

        if (driverDereferenceResult == null) return null;

        if (usedDriver instanceof HttpDriver) {

            driverDereferenceResult.getDereferencingMetadata().put("pattern", ((HttpDriver) usedDriver).getPattern().pattern());
            driverDereferenceResult.getDereferencingMetadata().put("driverUrl", ((HttpDriver) usedDriver).getResolveUri());

            if (log.isDebugEnabled()) log.debug("Resolved " + didUrl + " with driver " + usedDriver.getClass().getSimpleName() + " and pattern " + ((HttpDriver) usedDriver).getPattern().pattern());
        } else {

            if (log.isDebugEnabled()) log.debug("Resolved " + didUrl + " with driver " + usedDriver.getClass().getSimpleName());
        }

        return driverDereferenceResult;
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
            ExecutionStateUtil.addDereferencerExtensionStage(executionState, extensionClass, extension);
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
