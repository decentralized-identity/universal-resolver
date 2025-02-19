package uniresolver.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.representations.production.RepresentationProducer;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonPropertyOrder({ "didResolutionMetadata", "didDocument", "didDocumentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class ResolveResult implements Result {

    public static final String MEDIA_TYPE = "application/ld+json;profile=\"https://w3id.org/did-resolution\"";
    public static final ContentType CONTENT_TYPE = ContentType.parse(MEDIA_TYPE);

    private static final URI DEFAULT_JSONLD_CONTEXT = URI.create("https://w3id.org/did-resolution/v1");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonProperty("didResolutionMetadata")
    private Map<String, Object> didResolutionMetadata;

    @JsonProperty("didDocument")
    private DIDDocument didDocument;

    @JsonProperty("didDocumentMetadata")
    private Map<String, Object> didDocumentMetadata;

    private ResolveResult(Map<String, Object> didResolutionMetadata, DIDDocument didDocument, Map<String, Object> didDocumentMetadata) {
        this.didResolutionMetadata = didResolutionMetadata != null ? didResolutionMetadata : new LinkedHashMap<>();
        this.didDocument = didDocument;
        this.didDocumentMetadata = didDocumentMetadata != null ? didDocumentMetadata : new LinkedHashMap<>();
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static ResolveResult build(@JsonProperty(value="didResolutionMetadata") Map<String, Object> didResolutionMetadata, @JsonProperty(value="didDocument") DIDDocument didDocument, @JsonProperty(value="didDocumentMetadata") Map<String, Object> didDocumentMetadata) {
        return new ResolveResult(didResolutionMetadata, didDocument, didDocumentMetadata);
    }

    public static ResolveResult build() {
        return new ResolveResult(new LinkedHashMap<>(), null, new LinkedHashMap<>());
    }

    /*
     * Field methods
     */

    @Override
    public Map<String, Object> getFunctionMetadata() {
        return this.getDidResolutionMetadata();
    }

    @Override
    public byte[] getFunctionContent() throws IOException {
        if (this.getDidDocument() == null) return null;
        return RepresentationProducer.produce(this.getDidDocument(), this.getContentType());
    }

    @Override
    public Map<String, Object> getFunctionContentMetadata() {
        return this.getDidDocumentMetadata();
    }

    /*
     * Serialization
     */

    public static ResolveResult fromJson(String json) throws IOException {
        return objectMapper.readValue(json, ResolveResult.class);
    }

    public static ResolveResult fromJson(Reader reader) throws IOException {
        return objectMapper.readValue(reader, ResolveResult.class);
    }

    @Override
    public Map<String, Object> toMap() {
        return objectMapper.convertValue(this, LinkedHashMap.class);
    }

    @Override
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Cannot write JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    public URI getDefaultContext() {
        return DEFAULT_JSONLD_CONTEXT;
    }

    @Override
    public boolean isComplete() {
        return this.getContentType() != null && this.getDidDocument() != null;
    }

    /*
     * Getters and setters
     */

    @JsonGetter("didResolutionMetadata")
    public final Map<String, Object> getDidResolutionMetadata() {
        return this.didResolutionMetadata;
    }

    @JsonSetter("didResolutionMetadata")
    public final void setDidResolutionMetadata(Map<String, Object> didResolutionMetadata) {
        this.didResolutionMetadata = didResolutionMetadata;
    }

    @JsonGetter("didDocument")
    public DIDDocument getDidDocument() {
        return this.didDocument;
    }

    @JsonSetter("didDocument")
    public void setDidDocument(DIDDocument didDocument) {
        this.didDocument = didDocument;
    }

    @JsonGetter("didDocumentMetadata")
    public final Map<String, Object> getDidDocumentMetadata() {
        return this.didDocumentMetadata;
    }

    @JsonSetter("didDocumentMetadata")
    public final void setDidDocumentMetadata(Map<String, Object> didDocumentMetadata) {
        this.didDocumentMetadata = didDocumentMetadata;
    }

    /*
     * Helper methods
     */

    @JsonIgnore
    public static boolean isMediaType(ContentType mediaType) {
        boolean isResolveResultMimeTypeEquals = CONTENT_TYPE.getMimeType().equals(mediaType.getMimeType());
        boolean isResolveResultProfileEquals = CONTENT_TYPE.getParameter("profile").equals(mediaType.getParameter("profile"));
        return isResolveResultMimeTypeEquals && isResolveResultProfileEquals;
    }

    /*
     * Object methods
     */

    @Override
    public String toString() {
        return this.toJson();
    }
}
