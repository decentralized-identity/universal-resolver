package uniresolver.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({ "didDocument", "content", "contentType", "didResolutionMetadata", "didDocumentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class ResolveResult {

	public static final String MIME_TYPE = "application/ld+json;profile=\"https://w3id.org/did-resolution\"";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private DIDDocument didDocument;

	@JsonProperty
	private Object content;

	@JsonProperty
	private String contentType;

	@JsonProperty
	private Map<String, Object> didResolutionMetadata;

	@JsonProperty
	private Map<String, Object> didDocumentMetadata;

	private ResolveResult(DIDDocument didDocument, Object content, String contentType, Map<String, Object> didResolutionMetadata, Map<String, Object> didDocumentMetadata) {

		this.didDocument = didDocument;
		this.content = content;
		this.contentType = contentType;
		this.didResolutionMetadata = didResolutionMetadata;
		this.didDocumentMetadata = didDocumentMetadata;
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static ResolveResult build(@JsonProperty(value="didDocument", required=true) DIDDocument didDocument, @JsonProperty(value="content", required=false) Object content, @JsonProperty(value="contentType", required=false) String contentType, @JsonProperty(value="resolverMetadata", required=false) Map<String, Object> resolverMetadata, @JsonProperty(value="methodMetadata", required=false) Map<String, Object> methodMetadata) {

		return new ResolveResult(didDocument, content, contentType, resolverMetadata, methodMetadata);
	}

	public static ResolveResult build(DIDDocument didDocument, Map<String, Object> resolverMetadata, Map<String, Object> methodMetadata) {

		return new ResolveResult(didDocument, null, null, resolverMetadata, methodMetadata);
	}

	public static ResolveResult build(DIDDocument didDocument) {

		return new ResolveResult(didDocument, null, null, new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	public static ResolveResult build() {

		return new ResolveResult(null, null, null, new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	public ResolveResult copy() {

		return new ResolveResult(this.getDidDocument(), this.getContent(), this.getContentType(), this.getDidResolutionMetadata(), this.getDidDocumentMetadata());
	}

	public void reset() {

		this.setDidDocument(null);
		this.setContent(null);
		this.setContentType(null);
		this.setDidResolutionMetadata(new HashMap<String, Object> ());
		this.setDidDocumentMetadata(new HashMap<String, Object> ());
	}

	/*
	 * Serialization
	 */

	public static ResolveResult fromJson(String json) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(json, ResolveResult.class);
	}

	public static ResolveResult fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(reader, ResolveResult.class);
	}

	public String toJson() throws JsonProcessingException {

		return objectMapper.writeValueAsString(this);
	}

	/*
	 * Getters and setters
	 */

	@JsonRawValue
	public final DIDDocument getDidDocument() {

		return this.didDocument;
	}

	@JsonSetter
	public final void setDidDocument(DIDDocument didDocument) {

		this.didDocument = didDocument;
	}

	@JsonGetter
	public Object getContent() {

		return this.content;
	}

	@JsonSetter
	public void setContent(Object content) {

		this.content = content;
	}

	@JsonGetter
	public String getContentType() {

		return this.contentType;
	}

	@JsonSetter
	public void setContentType(String contentType) {

		this.contentType = contentType;
	}

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
