package uniresolver.result;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonPropertyOrder({ "didResolutionMetadata", "didDocument", "didDocumentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class ResolveDataModelResult extends ResolveResult implements Result {

	private static final Logger log = LoggerFactory.getLogger(ResolveDataModelResult.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty("didDocument")
	private DIDDocument didDocument;

	private ResolveDataModelResult(Map<String, Object> didResolutionMetadata, DIDDocument didDocument, Map<String, Object> didDocumentMetadata) {
		super(didResolutionMetadata, didDocumentMetadata);
		this.didDocument = didDocument;
	}

	@Override
	@JsonIgnore
	public boolean isComplete() {
		return this.getDidDocument() != null;
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static ResolveDataModelResult build(@JsonProperty(value="didResolutionMetadata") Map<String, Object> didResolutionMetadata, @JsonProperty(value="didDocument") DIDDocument didDocument, @JsonProperty(value="didDocumentMetadata") Map<String, Object> didDocumentMetadata) {
		return new ResolveDataModelResult(didResolutionMetadata, didDocument, didDocumentMetadata);
	}

	public static ResolveDataModelResult build() {
		return new ResolveDataModelResult(new LinkedHashMap<>(), null, new LinkedHashMap<>());
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
	 * Conversion
	 */

	@Override
	public void updateConversion() throws ResolutionException {

		for (Map.Entry<String, ResolveRepresentationResult> resolveRepresentationResultEntry : this.resolveRepresentationResults.entrySet()) {

			String mediaType = resolveRepresentationResultEntry.getKey();
			ResolveRepresentationResult resolveRepresentationResult = resolveRepresentationResultEntry.getValue();
			ResolveRepresentationResult newResolveRepresentationResult = Conversion.convertToResolveRepresentationResult(this, mediaType);
			resolveRepresentationResult.setDidDocumentStream(newResolveRepresentationResult.getDidDocumentStream());
		}
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

	/*
	 * Object methods
	 */

	@Override
	public String toString() {
		return this.toJson();
	}
}
