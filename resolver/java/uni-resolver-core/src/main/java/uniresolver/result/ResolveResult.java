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

	public static final String MIME_TYPE = "application/json";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private DIDDocument didDocument;

	@JsonProperty
	private Map<String, Object> resolverMetadata;

	@JsonProperty
	private Map<String, Object> methodMetadata;

	private ResolveResult(DIDDocument didDocument, Map<String, Object> resolverMetadata, Map<String, Object> methodMetadata) {

		this.didDocument = didDocument;
		this.resolverMetadata = resolverMetadata;
		this.methodMetadata = methodMetadata;
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static ResolveResult build(@JsonProperty(value="didDocument", required=true) DIDDocument didDocument, @JsonProperty(value="resolverMetadata", required=true) Map<String, Object> resolverMetadata, @JsonProperty(value="methodMetadata", required=true) Map<String, Object> methodMetadata) {

		return new ResolveResult(didDocument, resolverMetadata, methodMetadata);
	}

	public static ResolveResult build(DIDDocument didDocument) {

		return new ResolveResult(didDocument, new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	public static ResolveResult build(Map<String, Object> didDocument) {

		return new ResolveResult(DIDDocument.build(didDocument), new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	public static ResolveResult build() {

		return new ResolveResult(null, new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	public ResolveResult copy() {

		return new ResolveResult(this.getDidDocument(), this.getResolverMetadata(), this.getMethodMetadata());
	}

	public void reset() {

		this.setDidDocument((DIDDocument) null);
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
