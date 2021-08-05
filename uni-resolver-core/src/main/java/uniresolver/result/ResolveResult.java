package uniresolver.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.UniResolver;
import uniresolver.util.ResolveResultUtil;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonPropertyOrder({ "didResolutionMetadata", "didDocument", "didDocumentStream", "didDocumentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class ResolveResult {

	static final Logger log = LoggerFactory.getLogger(ResolveResult.class);

	public static final String MEDIA_TYPE = "application/ld+json;profile=\"https://w3id.org/did-resolution\"";

	public static final String ERROR_INVALIDDID = "invalidDid";
	public static final String ERROR_NOTFOUND = "notFound";
	public static final String ERROR_REPRESENTATIONNOTSUPPORTED = "representationNotSupported";
	public static final String ERROR_INTERNALERROR = "internalError";

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
		return new ResolveResult(new HashMap<>(), null, null, new HashMap<>());
	}

	public static ResolveResult makeErrorResolveRepresentationResult(String error, String errorMessage, String contentType) {
		ResolveResult resolveResult = ResolveResult.build();
		resolveResult.setError(error == null ? ERROR_INTERNALERROR : error);
		if (errorMessage != null) resolveResult.setErrorMessage(errorMessage);
		resolveResult.setContentType(contentType);
		resolveResult.setDidDocumentStream(new byte[0]);
		return resolveResult;
	}

	public static ResolveResult makeErrorResolveRepresentationResult(ResolutionException ex, String contentType) {
		if (ex.getResolveRepresentationResult() != null && contentType.equals(ex.getResolveRepresentationResult().getContentType())) {
			return ex.getResolveRepresentationResult();
		}
		return makeErrorResolveRepresentationResult(ex.getError(), ex.getMessage(), contentType);
	}

	/*
	 * Helper methods
	 */

	@JsonIgnore
	public boolean isErrorResult() {
		return this.getError() != null;
	}

	@JsonIgnore
	public String getError() {
		return this.getDidResolutionMetadata() == null ? null : (String) this.getDidResolutionMetadata().get("error");
	}

	@JsonIgnore
	public void setError(String error) {
		if (this.getDidResolutionMetadata() == null) this.setDidResolutionMetadata(new HashMap<>());
		this.getDidResolutionMetadata().put("error", error);
	}

	@JsonIgnore
	public String getErrorMessage() {
		return this.getDidResolutionMetadata() == null ? null : (String) this.getDidResolutionMetadata().get("errorMessage");
	}

	@JsonIgnore
	public void setErrorMessage(String errorMessage) {
		if (this.getDidResolutionMetadata() == null) this.setDidResolutionMetadata(new HashMap<>());
		this.getDidResolutionMetadata().put("errorMessage", errorMessage);
	}

	@JsonIgnore
	public String getContentType() {
		return this.getDidResolutionMetadata() == null ? null : (String) this.getDidResolutionMetadata().get("contentType");
	}

	@JsonIgnore
	public void setContentType(String contentType) {
		if (this.getDidResolutionMetadata() == null) this.setDidResolutionMetadata(new HashMap<>());
		this.getDidResolutionMetadata().put("contentType", contentType);
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
		if (this.getDidDocumentStream() == null) {
			return null;
		} else {
			if (isJson(this.getDidDocumentStream())) {
				return new String(this.getDidDocumentStream(), StandardCharsets.UTF_8);
			} else {
				return Hex.encodeHexString(this.getDidDocumentStream());
			}
		}
	}

	@JsonIgnore
	public final void setDidDocumentStream(byte[] didDocumentStream) {
		this.didDocumentStream = didDocumentStream;
	}

	@JsonSetter("didDocumentStream")
	public final void setDidDocumentStreamAsString(String didDocumentStream) throws DecoderException {
		if (didDocumentStream == null) {
			this.setDidDocumentStream(null);
		} else {
			try {
				this.setDidDocumentStream(Hex.decodeHex(didDocumentStream));
			} catch (DecoderException ex) {
				this.setDidDocumentStream(didDocumentStream.getBytes(StandardCharsets.UTF_8));
			}
		}
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
