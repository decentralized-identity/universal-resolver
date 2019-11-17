package uniresolver.result;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import did.DIDDocument;

@JsonPropertyOrder({ "didDocument", "resolverMetadata", "methodMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class ResolveResult {

	public static final String MIME_TYPE = "application/ld+json;profile=\"https://w3c-ccg.github.io/did-resolution/\"";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private DIDDocument didDocument;

	@JsonProperty
	private Object content;

	@JsonProperty
	private String contentType;

	@JsonProperty
	private Map<String, Object> resolverMetadata;

	@JsonProperty
	private Map<String, Object> methodMetadata;

	private ResolveResult(DIDDocument didDocument, Object content, String contentType, Map<String, Object> resolverMetadata, Map<String, Object> methodMetadata) {

		this.didDocument = didDocument;
		this.content = content;
		this.contentType = contentType;
		this.resolverMetadata = resolverMetadata;
		this.methodMetadata = methodMetadata;
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static ResolveResult build(@JsonProperty(value="didDocument", required=true) DIDDocument didDocument, @JsonProperty(value="content", required=false) Object content, @JsonProperty(value="contentType", required=false) String contentType, @JsonProperty(value="resolverMetadata", required=true) Map<String, Object> resolverMetadata, @JsonProperty(value="methodMetadata", required=true) Map<String, Object> methodMetadata) {

		return new ResolveResult(didDocument, content, contentType, resolverMetadata, methodMetadata);
	}

	public static ResolveResult build(DIDDocument didDocument, Map<String, Object> resolverMetadata, Map<String, Object> methodMetadata) {

		return new ResolveResult(didDocument, null, DIDDocument.MIME_TYPE, resolverMetadata, methodMetadata);
	}

	public static ResolveResult build(DIDDocument didDocument) {

		return new ResolveResult(didDocument, null, DIDDocument.MIME_TYPE, new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	public static ResolveResult build(Map<String, Object> didDocument) {

		return new ResolveResult(DIDDocument.build(didDocument), null, DIDDocument.MIME_TYPE, new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	public static ResolveResult build() {

		return new ResolveResult(null, null, null, new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	public ResolveResult copy() {

		return new ResolveResult(this.getDidDocument(), this.getContent(), this.getContentType(), this.getResolverMetadata(), this.getMethodMetadata());
	}

	public void reset() {

		this.setDidDocument(null);
		this.setContent(null);
		this.setContentType(null);
		this.setResolverMetadata(new HashMap<String, Object> ());
		this.setMethodMetadata(new HashMap<String, Object> ());
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
	public final Map<String, Object> getResolverMetadata() {

		return this.resolverMetadata;
	}

	@JsonSetter
	public final void setResolverMetadata(Map<String, Object> resolverMetadata) {

		this.resolverMetadata = resolverMetadata;
	}

	@JsonGetter
	public final Map<String, Object> getMethodMetadata() {

		return this.methodMetadata;
	}

	@JsonSetter
	public final void setMethodMetadata(Map<String, Object> methodMetadata) {

		this.methodMetadata = methodMetadata;
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
