package uniresolver.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.representations.Representations;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.ResolveResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpBindingUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ResolveResult fromHttpBodyResolveResult(String httpBody) throws IOException {
        if (log.isDebugEnabled()) log.debug("Deserizalizing resolve result from HTTP body.");
        ResolveResult resolveResult = ResolveResult.build();
        Map<String, Object> json = objectMapper.readValue(httpBody, Map.class);
        Object didDocument = json.get("didDocument");
        Map<String, Object> didResolutionMetadata = (Map<String, Object>) json.get("didResolutionMetadata");
        Map<String, Object> didDocumentMetadata = (Map<String, Object>) json.get("didDocumentMetadata");
        if (didResolutionMetadata != null) resolveResult.setDidResolutionMetadata(didResolutionMetadata);
        if (didDocumentMetadata != null) resolveResult.setDidDocumentMetadata(didDocumentMetadata);
        byte[] didDocumentStream = null;
        if (didDocument instanceof Map) {
            didDocumentStream = objectMapper.writeValueAsBytes((Map<String, Object>) didDocument);
        } else if (didDocument instanceof String) {
            try {
                didDocumentStream = Hex.decodeHex((String) didDocument);
            } catch (DecoderException ex) {
                didDocumentStream = ((String) didDocument).getBytes(StandardCharsets.UTF_8);
            }
        }
        return ResolveResult.build(didResolutionMetadata, null, didDocumentStream, didDocumentMetadata);
    }

    public static String toHttpBodyResolveResult(ResolveResult resolveResult) throws IOException {
        if (log.isDebugEnabled()) log.debug("Serializing resolve result to HTTP body.");
        if (resolveResult.getDidDocument() != null) throw new IllegalArgumentException("Cannot serialize abstract DID document.");
        Map<String, Object> json = new HashMap<>();
        json.put("didResolutionMetadata", resolveResult.getDidResolutionMetadata());
        json.put("didDocumentMetadata", resolveResult.getDidDocumentMetadata());
        if (isJson(resolveResult.getDidDocumentStream())) {
            json.put("didDocument", objectMapper.readValue(new ByteArrayInputStream(resolveResult.getDidDocumentStream()), Map.class));
        } else {
            json.put("didDocument", Hex.encodeHexString(resolveResult.getDidDocumentStream()));
        }
        return objectMapper.writeValueAsString(json);
    }

    public static ResolveResult fromHttpBodyDidDocument(byte[] httpBodyBytes, ContentType contentType) {
        if (log.isDebugEnabled()) log.debug("Deserizalizing resolve result from HTTP body.");
        if (contentType == null) {
            if (log.isDebugEnabled()) log.warn("Driver received no content type. Assuming default " + Representations.DEFAULT_MEDIA_TYPE);
            contentType = ContentType.create(Representations.DEFAULT_MEDIA_TYPE);
        }
        ResolveResult resolveResult = ResolveResult.build();
        resolveResult.getDidResolutionMetadata().put("contentType", contentType.getMimeType());
        resolveResult.setDidDocumentStream(httpBodyBytes);
        return resolveResult;
    }

    /*
     * Helper methods
     */

    private static boolean isJson(byte[] bytes) throws IOException {
        try {
            return objectMapper.getFactory().createParser(bytes).readValueAsTree() != null;
        } catch (JsonParseException ex) {
            return false;
        }
    }
}
