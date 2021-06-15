package uniresolver.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({ "didResolutionMetadata", "didDocument", "didDocumentStream", "didDocumentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class ResolveResult {

	public static final String MEDIA_TYPE = "application/ld+json;profile=\"https://w3id.org/did-resolution\"";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty("didResolutionMetadata")
	private Map<String, Object> didResolutionMetadata;

	@JsonProperty("didDocument")
	private DIDDocument didDocument;

	@JsonProperty("didDocumentStream")
	private byte[] didDocumentStream;

	@JsonProperty("didDocumentMetadata")
	private Map<String, Object> didDocumentMetadata;

	static {
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	private ResolveResult(Map<String, Object> didResolutionMetadata, DIDDocument didDocument, byte[] didDocumentStream, Map<String, Object> didDocumentMetadata) {
		this.didResolutionMetadata = didResolutionMetadata != null ? didResolutionMetadata : new HashMap<>();
		this.didDocument = didDocument;
		this.didDocumentStream = didDocumentStream;
		this.didDocumentMetadata = didDocumentMetadata != null ? didDocumentMetadata : new HashMap<>();
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static ResolveResult build(@JsonProperty(value="didResolutionMetadata", required=false) Map<String, Object> didResolutionMetadata, @JsonProperty(value="didDocument", required=false) DIDDocument didDocument, @JsonProperty(value="didDocumentStream", required=false) byte[] didDocumentStream, @JsonProperty(value="didDocumentMetadata", required=false) Map<String, Object> didDocumentMetadata) {
		return new ResolveResult(didResolutionMetadata, didDocument, didDocumentStream, didDocumentMetadata);
	}

	public static ResolveResult build() {
		return new ResolveResult(null, null, null, null);
	}

	/*
	 * Helper classes
	 */

	public enum Error {
		invalidDid,
		notFound,
		representationNotSupported,
		internalError
	}

	/*
	 * Helper methods
	 */

	public static ResolveResult makeErrorResult(Error error, String errorMessage, String contentType) {
		ResolveResult resolveResult = ResolveResult.build();
		resolveResult.setError(error.name());
		if (errorMessage != null) resolveResult.setErrorMessage(errorMessage);
		if (contentType != null) {
			resolveResult.getDidResolutionMetadata().put("contentType", contentType);
			resolveResult.setDidDocumentStream(new byte[0]);
		}
		return resolveResult;
	}

	@JsonIgnore
	public boolean isErrorResult() {
		return this.getDidResolutionMetadata() != null && this.getDidResolutionMetadata().containsKey("error");
	}

	@JsonIgnore
	public void setError(String error) {
		this.getDidResolutionMetadata().put("error", error);
	}

	@JsonIgnore
	public String getError() {
		return (String) this.getDidResolutionMetadata().get("error");
	}

	@JsonIgnore
	public void setErrorMessage(String errorMessage) {
		this.getDidResolutionMetadata().put("errorMessage", errorMessage);
	}

	@JsonIgnore
	public String getErrorMessage() {
		return (String) this.getDidResolutionMetadata().get("errorMessage");
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

	public Map<String, Object> toMap() {
		return objectMapper.convertValue(this, Map.class);
	}

	public String toJson() {
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Cannot write JSON: " + ex.getMessage(), ex);
		}
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

	@JsonRawValue
	public final DIDDocument getDidDocument() {
		return this.didDocument;
	}

	@JsonSetter
	public final void setDidDocument(DIDDocument didDocument) {
		this.didDocument = didDocument;
	}

	@JsonIgnore
	public final byte[] getDidDocumentStream() {
		return this.didDocumentStream;
	}

	@JsonGetter("didDocumentStream")
	public final String getDidDocumentStreamAsString() {
		return this.getDidDocumentStream() == null ? null : new String(this.getDidDocumentStream(), StandardCharsets.UTF_8);
	}

	@JsonIgnore
	public final void setDidDocumentStream(byte[] didDocumentStream) {
		this.didDocumentStream = didDocumentStream;
	}

	@JsonSetter("didDocumentStream")
	public final void setDidDocumentStreamAsString(String didDocumentStream) throws DecoderException {
		this.setDidDocumentStream(didDocumentStream == null ? null : Hex.decodeHex(didDocumentStream));
	}

	@JsonGetter
	public final Map<String, Object> getDidDocumentMetadata() {
		return this.didDocumentMetadata;
	}

	@JsonSetter
	public final void setDidDocumentMetadata(Map<String, Object> didDocumentMetadata) {
		this.didDocumentMetadata = didDocumentMetadata;
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {
		return this.toJson();
	}
}
