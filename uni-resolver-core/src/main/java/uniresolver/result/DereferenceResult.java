package uniresolver.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonPropertyOrder({ "dereferencingMetadata", "contentStream", "contentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class DereferenceResult {

	public static final String MIME_TYPE = "application/ld+json;profile=\"https://w3id.org/did-resolution\"";

	public static final String ERROR_INVALIDDIDURL = "invalidDidUrl";
	public static final String ERROR_NOTFOUND = "notFound";
	public static final String ERROR_INTERNALERROR = "internalError";

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

	public static DereferenceResult makeErrorDereferenceResult(String error, String errorMessage, String contentType) {
		DereferenceResult dereferenceResult = DereferenceResult.build();
		dereferenceResult.setError(error == null ? ERROR_INTERNALERROR : error);
		if (errorMessage != null) dereferenceResult.setErrorMessage(errorMessage);
		dereferenceResult.setContentType(contentType);
		dereferenceResult.setContentStream(new byte[0]);
		return dereferenceResult;
	}

	/*
	 * Helper methods
	 */

	@JsonIgnore
	public boolean isErrorDereferenceResult() {
		return this.getError() != null;
	}

	@JsonIgnore
	public String getError() {
		return this.getDereferencingMetadata() == null ? null : (String) this.getDereferencingMetadata().get("error");
	}

	@JsonIgnore
	public void setError(String error) {
		if (this.getDereferencingMetadata() == null) this.setDereferencingMetadata(new HashMap<>());
		this.getDereferencingMetadata().put("error", error);
	}

	@JsonIgnore
	public String getErrorMessage() {
		return this.getDereferencingMetadata() == null ? null : (String) this.getDereferencingMetadata().get("errorMessage");
	}

	@JsonIgnore
	public void setErrorMessage(String errorMessage) {
		if (this.getDereferencingMetadata() == null) this.setDereferencingMetadata(new HashMap<>());
		this.getDereferencingMetadata().put("errorMessage", errorMessage);
	}

	@JsonIgnore
	public String getContentType() {
		return this.getDereferencingMetadata() == null ? null : (String) this.getDereferencingMetadata().get("contentType");
	}

	@JsonIgnore
	public void setContentType(String contentType) {
		if (this.getDereferencingMetadata() == null) this.setDereferencingMetadata(new HashMap<>());
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

	public Map<String, Object> toMap() {
		return objectMapper.convertValue(this, LinkedHashMap.class);
	}

	public String toJson() {
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Cannot write JSON: " + ex.getMessage(), ex);
		}
	}

	private static boolean isJson(byte[] bytes) {
		try {
			return objectMapper.getFactory().createParser(bytes).readValueAsTree() != null;
		} catch (IOException ex) {
			return false;
		}
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
		if (this.getContentStream() == null) {
			return null;
		} else {
			if (isJson(this.getContentStream())) {
				return new String(this.getContentStream(), StandardCharsets.UTF_8);
			} else {
				return Hex.encodeHexString(this.getContentStream());
			}
		}
	}

	@JsonIgnore
	public final void setContentStream(byte[] contentStream) {
		this.contentStream = contentStream;
	}

	@JsonSetter("contentStream")
	public final void setContentStreamAsString(String contentStream) {
		if (contentStream == null) {
			this.setContentStream(null);
		} else {
			try {
				this.setContentStream(Hex.decodeHex(contentStream));
			} catch (DecoderException ex) {
				this.setContentStream(contentStream.getBytes(StandardCharsets.UTF_8));
			}
		}
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
		return this.toJson();
	}
}
