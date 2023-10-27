package uniresolver.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonPropertyOrder({ "dereferencingMetadata", "contentStream", "contentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class DereferenceResult implements Result, StreamResult {

	public static final String MEDIA_TYPE = "application/ld+json;profile=\"https://w3id.org/did-resolution\"";

	private static final Logger log = LoggerFactory.getLogger(ResolveRepresentationResult.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private Map<String, Object> dereferencingMetadata;

	@JsonProperty
	private byte[] contentStream;

	@JsonProperty
	private Map<String, Object> contentMetadata;

	private DereferenceResult(Map<String, Object> dereferencingMetadata, byte[] contentStream, Map<String, Object> contentMetadata) {
		this.dereferencingMetadata = dereferencingMetadata != null ? dereferencingMetadata : new LinkedHashMap<>();
		this.contentStream = contentStream != null ? contentStream : new byte[0];
		this.contentMetadata = contentMetadata != null ? contentMetadata : new LinkedHashMap<>();
	}

	@JsonIgnore
	public boolean isComplete() {
		return this.getContentType() != null && this.getContentStream() != null;
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static DereferenceResult build(@JsonProperty(value="dereferencingMetadata") Map<String, Object> dereferencingMetadata, @JsonProperty(value="contentStream", required=true) byte[] contentStream, @JsonProperty(value="contentMetadata") Map<String, Object> contentMetadata) {
		return new DereferenceResult(dereferencingMetadata, contentStream, contentMetadata);
	}

	public static DereferenceResult build() {
		return new DereferenceResult(new LinkedHashMap<>(), new byte[0], new LinkedHashMap<>());
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

	private static boolean isJson(byte[] bytes) {
		try {
			try (JsonParser jsonParser = objectMapper.getFactory().createParser(bytes)) {
				return jsonParser.readValueAsTree() != null;
			}
		} catch (IOException ex) {
			return false;
		}
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

	/*
	 * Metadata methods
	 */

	@Override
	public Map<String, Object> getFunctionMetadata() {
		return this.getDereferencingMetadata();
	}

	@Override
	public Map<String, Object> getFunctionContentMetadata() {
		return this.getContentMetadata();
	}

	/*
	 * Content stream methods
	 */

	@Override
	public byte[] getFunctionContentStream() {
		return this.getContentStream();
	}

	@Override
	public void setFunctionContentStream(byte[] functionContentStream) {
		this.setContentStream(functionContentStream);
	}

	/*
	 * Conversion
	 */

	public ResolveRepresentationResult toResolveRepresentationResult() {
		ResolveRepresentationResult resolveRepresentationResult = ResolveRepresentationResult.build();
		resolveRepresentationResult.getDidResolutionMetadata().putAll(this.getDereferencingMetadata());
		resolveRepresentationResult.setDidDocumentStream(this.getContentStream());
		resolveRepresentationResult.getDidDocumentMetadata().putAll(this.getContentMetadata());
		return resolveRepresentationResult;
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
