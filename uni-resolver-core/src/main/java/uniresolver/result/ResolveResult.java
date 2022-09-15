package uniresolver.result;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ResolveResult implements Result {

    public static final URI DEFAULT_JSONLD_CONTEXT = URI.create("https://w3id.org/did-resolution/v1");

    public static final String MEDIA_TYPE = "application/ld+json;profile=\"https://w3id.org/did-resolution\"";
    public static final ContentType CONTENT_TYPE = ContentType.parse(MEDIA_TYPE);

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
     * Metadata methods
     */

    @Override
    public Map<String, Object> getFunctionMetadata() {
        return this.getDidResolutionMetadata();
    }

    @Override
    public Map<String, Object> getFunctionContentMetadata() {
        return this.getDidDocumentMetadata();
    }

    /*
     * Media type methods
     */

    public static boolean isResolveResultMediaType(ContentType mediaType) {
        boolean isResolveResultMimeTypeEquals = CONTENT_TYPE.getMimeType().equals(mediaType.getMimeType());
        boolean isResolveResultProfileEquals = CONTENT_TYPE.getParameter("profile").equals(mediaType.getParameter("profile"));
        return isResolveResultMimeTypeEquals && isResolveResultProfileEquals;
    }

    /*
     * Conversion
     */

    public ResolveDataModelResult toResolveDataModelResult() throws ResolutionException {
        if (this.resolveDataModelResult == null) {
            if (this instanceof ResolveDataModelResult) {
                if (log.isDebugEnabled()) log.debug("toResolveDataModelResult(): this=ResolveDataModelResult");
                this.resolveDataModelResult = (ResolveDataModelResult) this;
            } else if (this instanceof ResolveRepresentationResult) {
                if (log.isDebugEnabled()) log.debug("toResolveDataModelResult(): this=ResolveRepresentationResult");
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
                if (log.isDebugEnabled()) log.debug("toResolveRepresentationResult(): this=ResolveRepresentationResult, representationMediaType=" + representationMediaType + ", this.contentType=" + ((ResolveRepresentationResult) this).getContentType());
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
                if (log.isDebugEnabled()) log.debug("toResolveRepresentationResult(): this=ResolveDataModelResult");
                ResolveRepresentationResult resolveRepresentationResult = Conversion.convertToResolveRepresentationResult((ResolveDataModelResult) this, representationMediaType);
                this.resolveRepresentationResults.put(representationMediaType, resolveRepresentationResult);
            }
        }
        return this.resolveRepresentationResults.get(representationMediaType);
    }

    public abstract void updateConversion() throws ResolutionException;

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
