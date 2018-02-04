package uniresolver.result;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uniresolver.did.DIDDocument;

@JsonPropertyOrder({ "result", "metadata" })
public class ResolutionResult {

	public static final String MIME_TYPE = "application/json";

	public static final String JSON_PROPERTY_RESULT = "result";
	public static final String JSON_PROPERTY_RESOLVERMETADATA = "resolvermetadata";
	public static final String JSON_PROPERTY_DRIVERMETADATA = "drivermetadata";

	@JsonProperty
	private DIDDocument result;

	@JsonProperty
	private Map<String, Object> resolverMetadata;

	@JsonProperty
	private Map<String, Object> driverMetadata;

	private ResolutionResult(DIDDocument result, Map<String, Object> resolverMetadata, Map<String, Object> driverMetadata) {

		this.result = result;
		this.resolverMetadata = resolverMetadata;
		this.driverMetadata = driverMetadata;
	}

	/*
	 * Factory methods
	 */

	public static ResolutionResult build(DIDDocument result, Map<String, Object> resolverMetadata, Map<String, Object> driverMetadata) {

		return new ResolutionResult(result, resolverMetadata, driverMetadata);
	}

	public static ResolutionResult build(DIDDocument result) {

		return new ResolutionResult(result, new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	public static ResolutionResult build() {

		return new ResolutionResult(DIDDocument.build(new HashMap<String, Object> ()), new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	/*
	 * Serialization
	 */

	public static ResolutionResult fromJson(String json) throws JsonParseException, JsonMappingException, IOException {

		return new ObjectMapper().readValue(json, ResolutionResult.class);
	}

	public String toJson() throws JsonProcessingException {

		return new ObjectMapper().writeValueAsString(this);
	}

	/*
	 * Getters and setters
	 */

	@JsonRawValue
	public final DIDDocument getResult() {

		return this.result;
	}

	public final void setResult(DIDDocument result) {

		this.result = result;
	}

	@JsonSetter
	public final void setResult(Map<String, Object> jsonLdObject) {

		this.result = DIDDocument.build(jsonLdObject);
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
	public final Map<String, Object> getDriverMetadata() {

		return this.driverMetadata;
	}

	@JsonSetter
	public final void setDriverMetadata(Map<String, Object> driverMetadata) {

		this.driverMetadata = driverMetadata;
	}
}
