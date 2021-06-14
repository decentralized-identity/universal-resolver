package uniresolver.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({ "dereferencingMetadata", "contentStream", "contentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class DereferenceResult {

	public static final String MIME_TYPE = "application/ld+json;profile=\"https://w3id.org/did-resolution\"";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private Map<String, Object> dereferencingMetadata;

	@JsonProperty
	private byte[] contentStream;

	@JsonProperty
	private Map<String, Object> contentMetadata;

	private DereferenceResult(Map<String, Object> dereferencingMetadata, byte[] contentStream, Map<String, Object> contentMetadata) {
		this.dereferencingMetadata = dereferencingMetadata != null ? dereferencingMetadata : new HashMap<>();
		this.contentStream = contentStream;
		this.contentMetadata = contentMetadata != null ? contentMetadata : new HashMap<>();
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static DereferenceResult build(@JsonProperty(value="dereferencingMetadata", required=false) Map<String, Object> dereferencingMetadata, @JsonProperty(value="contentStream", required=true) byte[] contentStream, @JsonProperty(value="contentMetadata", required=false) Map<String, Object> contentMetadata) {
		return new DereferenceResult(dereferencingMetadata, contentStream, contentMetadata);
	}

	public static DereferenceResult build(byte[] contentStream) {
		return new DereferenceResult(new HashMap<String, Object> (), contentStream, new HashMap<String, Object> ());
	}

	public static DereferenceResult build() {
		return new DereferenceResult(null, null, null);
	}

	/*
	 * Helper classes
	 */

	public enum Error {
		invalidDidUrl,
		notFound,
		internalError
	}

	/*
	 * Helper methods
	 */

	public static DereferenceResult makeErrorResult(Error error, String errorMessage) {
		DereferenceResult dereferenceResult = DereferenceResult.build();
		dereferenceResult.setError(error.name());
		if (errorMessage != null) dereferenceResult.setErrorMessage(errorMessage);
		return dereferenceResult;
	}

	public static DereferenceResult makeErrorResult(Error error) {
		return makeErrorResult(error, null);
	}

	@JsonIgnore
	public boolean isErrorDereferenceResult() {
		return this.getDereferencingMetadata() != null && this.getDereferencingMetadata().containsKey("error");
	}

	@JsonIgnore
	public void setError(String error) {
		this.getDereferencingMetadata().put("error", error);
	}

	@JsonIgnore
	public String getError() {
		return (String) this.getDereferencingMetadata().get("error");
	}

	@JsonIgnore
	public void setErrorMessage(String errorMessage) {
		this.getDereferencingMetadata().put("errorMessage", errorMessage);
	}

	@JsonIgnore
	public String getErrorMessage() {
		return (String) this.getDereferencingMetadata().get("errorMessage");
	}

	@JsonIgnore
	public String getContentType() {
		return (String) this.getDereferencingMetadata().get("contentType");
	}

	@JsonIgnore
	public void setContentType(String contentType) {
		this.getDereferencingMetadata().put("contentType", contentType);
	}

	/*
	 * Serialization
	 */

	public static DereferenceResult fromJson(String json) throws IOException {
		return objectMapper.readValue(json, DereferenceResult.class);
	}

	public static DereferenceResult fromJson(Reader reader) throws IOException {
		return objectMapper.readValue(reader, DereferenceResult.class);
	}

	public String toJson() throws JsonProcessingException {
		return objectMapper.writeValueAsString(this);
	}

	/*
	 * Getters and setters
	 */

	@JsonGetter
	public final Map<String, Object> getDereferencingMetadata() {
		return this.dereferencingMetadata;
	}

	@JsonSetter
	public final void setDereferencingMetadata(Map<String, Object> dereferencingMetadata) {
		this.dereferencingMetadata = dereferencingMetadata;
	}

	@JsonIgnore
	public final byte[] getContentStream() {
		return this.contentStream;
	}

	@JsonGetter("contentStream")
	public final String getContentStreamAsString() {
		return this.getContentStream() == null ? null : new String(this.getContentStream(), StandardCharsets.UTF_8);
	}

	@JsonIgnore
	public final void setContentStream(byte[] contentStream) {
		this.contentStream = contentStream;
	}

	@JsonSetter("contentStream")
	public final void setDidDocumentStreamAsString(String contentStream) throws DecoderException {
		this.setContentStream(contentStream == null ? null : Hex.decodeHex(contentStream));
	}

	@JsonGetter
	public final Map<String, Object> getContentMetadata() {
		return this.contentMetadata;
	}

	@JsonSetter
	public final void setContentMetadata(Map<String, Object> contentMetadata) {
		this.contentMetadata = contentMetadata;
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {
		try {
			return this.toJson();
		} catch (JsonProcessingException ex) {
			return ex.getMessage();
		}
	}
}
