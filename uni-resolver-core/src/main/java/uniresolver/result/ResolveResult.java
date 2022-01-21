package uniresolver.result;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ResolveResult implements Result {

    public static final String MEDIA_TYPE = "application/ld+json;profile=\"https://w3id.org/did-resolution\"";

    public static final String ERROR_INVALIDDID = "invalidDid";
    public static final String ERROR_NOTFOUND = "notFound";
    public static final String ERROR_REPRESENTATIONNOTSUPPORTED = "representationNotSupported";
    public static final String ERROR_INTERNALERROR = "internalError";

    private static final Logger log = LoggerFactory.getLogger(ResolveResult.class);

    protected ResolveDataModelResult resolveDataModelResult = null;
    protected Map<String, ResolveRepresentationResult> resolveRepresentationResults = new HashMap<>();

    @JsonProperty("didResolutionMetadata")
    private Map<String, Object> didResolutionMetadata;

    @JsonProperty("didDocumentMetadata")
    private Map<String, Object> didDocumentMetadata;

    protected ResolveResult(Map<String, Object> didResolutionMetadata, Map<String, Object> didDocumentMetadata) {
        this.didResolutionMetadata = didResolutionMetadata != null ? didResolutionMetadata : new LinkedHashMap<>();
        this.didDocumentMetadata = didDocumentMetadata != null ? didDocumentMetadata : new LinkedHashMap<>();
    }

    public abstract boolean isComplete();

    /*
     * Conversion
     */

    public ResolveDataModelResult toResolveDataModelResult() throws ResolutionException {
        if (this.resolveDataModelResult == null) {
            if (this instanceof ResolveDataModelResult) {
                this.resolveDataModelResult = (ResolveDataModelResult) this;
            } else if (this instanceof ResolveRepresentationResult) {
                ResolveDataModelResult resolveDataModelResult = Conversion.convertToResolveDataModelResult((ResolveRepresentationResult) this);
                this.resolveDataModelResult = resolveDataModelResult;
            }
        }
        return this.resolveDataModelResult;
    }

    public ResolveRepresentationResult toResolveRepresentationResult(String representationMediaType) throws ResolutionException {
        if (representationMediaType == null) throw new NullPointerException();
        if (! this.resolveRepresentationResults.containsKey(representationMediaType)) {
            if (this instanceof ResolveRepresentationResult) {
                if (representationMediaType.equals(((ResolveRepresentationResult) this).getContentType())) {
                    this.resolveRepresentationResults.put(representationMediaType, (ResolveRepresentationResult) this);
                } else {
                    ResolveDataModelResult resolveDataModelResult = this.toResolveDataModelResult();
                    ResolveRepresentationResult resolveRepresentationResult = Conversion.convertToResolveRepresentationResult(resolveDataModelResult, representationMediaType);
                    resolveRepresentationResult.getDidResolutionMetadata().put("convertedFrom", ((ResolveRepresentationResult) this).getContentType());
                    resolveRepresentationResult.getDidResolutionMetadata().put("convertedTo", representationMediaType);
                    this.resolveRepresentationResults.put(representationMediaType, resolveRepresentationResult);
                }
            } else if (this instanceof ResolveDataModelResult) {
                ResolveRepresentationResult resolveRepresentationResult = Conversion.convertToResolveRepresentationResult((ResolveDataModelResult) this, representationMediaType);
                this.resolveRepresentationResults.put(representationMediaType, resolveRepresentationResult);
            }
        }
        return this.resolveRepresentationResults.get(representationMediaType);
    }

    public abstract void updateConversion() throws ResolutionException;

    /*
     * Error methods
     */

    @Override
    @JsonIgnore
    public boolean isErrorResult() {
        return this.getError() != null;
    }

    @Override
    @JsonIgnore
    public String getError() {
        return this.getDidResolutionMetadata() == null ? null : (String) this.getDidResolutionMetadata().get("error");
    }

    @Override
    @JsonIgnore
    public void setError(String error) {
        if (this.getDidResolutionMetadata() == null) this.setDidResolutionMetadata(new LinkedHashMap<>());
        if (error != null)
            this.getDidResolutionMetadata().put("error", error);
        else
            this.getDidResolutionMetadata().remove("error");
    }

    @Override
    @JsonIgnore
    public String getErrorMessage() {
        return this.getDidResolutionMetadata() == null ? null : (String) this.getDidResolutionMetadata().get("errorMessage");
    }

    @Override
    @JsonIgnore
    public void setErrorMessage(String errorMessage) {
        if (this.getDidResolutionMetadata() == null) this.setDidResolutionMetadata(new LinkedHashMap<>());
        if (errorMessage != null)
            this.getDidResolutionMetadata().put("errorMessage", errorMessage);
        else
            this.getDidResolutionMetadata().remove("errorMessage");
    }

    /*
     * Getters and setters
     */

    @JsonGetter
    public final Map<String, Object> getDidResolutionMetadata() {
        return this.didResolutionMetadata;
    }

    @JsonSetter
    public final void setDidResolutionMetadata(Map<String, Object> didResolutionMetadata) {
        this.didResolutionMetadata = didResolutionMetadata;
    }

    @JsonGetter
    public final Map<String, Object> getDidDocumentMetadata() {
        return this.didDocumentMetadata;
    }

    @JsonSetter
    public final void setDidDocumentMetadata(Map<String, Object> didDocumentMetadata) {
        this.didDocumentMetadata = didDocumentMetadata;
    }
}
