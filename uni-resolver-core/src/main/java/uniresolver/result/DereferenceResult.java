package uniresolver.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonPropertyOrder({ "dereferencingMetadata", "content", "contentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class DereferenceResult implements Result {

	public static final String MEDIA_TYPE = "application/did-url-dereferencing";
	public static final ContentType CONTENT_TYPE = ContentType.parse(MEDIA_TYPE);

	public static final String LEGACY_MEDIA_TYPE = "application/ld+json;profile=\"https://w3id.org/did-url-dereferencing\"";
	public static final ContentType LEGACY_CONTENT_TYPE = ContentType.parse(LEGACY_MEDIA_TYPE);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty("dereferencingMetadata")
	private Map<String, Object> dereferencingMetadata;

	@JsonProperty("content")
	private byte[] content;

	@JsonProperty("contentMetadata")
	private Map<String, Object> contentMetadata;

	private DereferenceResult(Map<String, Object> dereferencingMetadata, byte[] content, Map<String, Object> contentMetadata) {
		this.dereferencingMetadata = dereferencingMetadata != null ? dereferencingMetadata : new LinkedHashMap<>();
		this.content = content;
		this.contentMetadata = contentMetadata != null ? contentMetadata : new LinkedHashMap<>();
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static DereferenceResult build(@JsonProperty(value="dereferencingMetadata") Map<String, Object> dereferencingMetadata, @JsonProperty(value="content", required=true) byte[] content, @JsonProperty(value="contentMetadata") Map<String, Object> contentMetadata) {
		return new DereferenceResult(dereferencingMetadata, content, contentMetadata);
	}

	public static DereferenceResult build() {
		return new DereferenceResult(new LinkedHashMap<>(), null, new LinkedHashMap<>());
	}

	/*
	 * Field methods
	 */

	@Override
	public Map<String, Object> getFunctionMetadata() {
		return this.getDereferencingMetadata();
	}

	@Override
	public byte[] getFunctionContent() {
		return this.getContent();
	}

	@Override
	public Map<String, Object> getFunctionContentMetadata() {
		return this.getContentMetadata();
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
	public boolean isComplete() {
		return this.getContentType() != null && this.getContent() != null;
	}

	/*
	 * Getters and setters
	 */

	@JsonGetter("dereferencingMetadata")
	public final Map<String, Object> getDereferencingMetadata() {
		return this.dereferencingMetadata;
	}

	@JsonSetter("dereferencingMetadata")
	public final void setDereferencingMetadata(Map<String, Object> dereferencingMetadata) {
		this.dereferencingMetadata = dereferencingMetadata;
	}

	public final byte[] getContent() {
		return this.content;
	}

	@JsonGetter("content")
	public final String getContentAsString() {
		if (this.getContent() == null) {
			return null;
		} else {
			try {
				try (JsonParser jsonParser = objectMapper.getFactory().createParser(this.getContent())) {
					if (jsonParser.readValueAsTree() != null) return new String(this.getContent(), StandardCharsets.UTF_8);
				}
			} catch (IOException ignored) {
			}
			return Hex.encodeHexString(this.getContent());
		}
	}

	public final void setContent(byte[] content) {
		this.content = content;
	}

	@JsonSetter("content")
	public final void setContentAsString(String contentString) {
		if (contentString == null) {
			this.setContent(null);
		} else {
			try {
				this.setContent(Hex.decodeHex(contentString));
			} catch (DecoderException ex) {
				this.setContent(contentString.getBytes(StandardCharsets.UTF_8));
			}
		}
	}

	@JsonGetter("contentMetadata")
	public final Map<String, Object> getContentMetadata() {
		return this.contentMetadata;
	}

	@JsonSetter("contentMetadata")
	public final void setContentMetadata(Map<String, Object> contentMetadata) {
		this.contentMetadata = contentMetadata;
	}

	/*
	 * Helper methods
	 */

	@JsonIgnore
	public static boolean isMediaType(ContentType mediaType) {
		return CONTENT_TYPE.getMimeType().equals(mediaType.getMimeType()) || LEGACY_CONTENT_TYPE.getMimeType().equals(mediaType.getMimeType());
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {
		return this.toJson();
	}
}
