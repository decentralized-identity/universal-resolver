package uniresolver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.representations.Representations;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpBindingUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ResolveResult fromHttpBodyResolveRepresentationResult(String httpBody) throws IOException {
        if (log.isDebugEnabled()) log.debug("Deserializing resolve result from HTTP body.");
        ResolveResult resolveResult = ResolveResult.build();
        Map<String, Object> json = objectMapper.readValue(httpBody, LinkedHashMap.class);
        Object didDocument = json.get("didDocument");
        Map<String, Object> didResolutionMetadata = json.containsKey("didResolutionMetadata") ? (Map<String, Object>) json.get("didResolutionMetadata") : new HashMap<>();
        Map<String, Object> didDocumentMetadata = json.containsKey("didDocumentMetadata") ? (Map<String, Object>) json.get("didDocumentMetadata") : new HashMap<>();
        resolveResult.setDidResolutionMetadata(didResolutionMetadata);
        resolveResult.setDidDocumentMetadata(didDocumentMetadata);
        byte[] didDocumentStream = null;
        if (didDocument instanceof Map) {
            didDocumentStream = objectMapper.writeValueAsBytes((Map<String, Object>) didDocument);
        } else if (didDocument instanceof String) {
            if (((String) didDocument).isEmpty()) {
                didDocumentStream = new byte[0];
            } else {
                try {
                    didDocumentStream = Hex.decodeHex((String) didDocument);
                } catch (DecoderException ex) {
                    didDocumentStream = ((String) didDocument).getBytes(StandardCharsets.UTF_8);
                }
            }
        } else if (didDocument == null) {
            didDocumentStream = new byte[0];
        }
        return ResolveResult.build(didResolutionMetadata, null, didDocumentStream, didDocumentMetadata);
    }

    public static ResolveResult fromHttpBodyDidDocument(byte[] httpBodyBytes, ContentType httpContentType) {
        if (log.isDebugEnabled()) log.debug("Deserializing DID document from HTTP body.");
        if (httpContentType == null) {
            if (log.isDebugEnabled()) log.warn("No content type. Assuming default " + Representations.DEFAULT_MEDIA_TYPE);
            httpContentType = ContentType.create(Representations.DEFAULT_MEDIA_TYPE);
        }
        String contentType = representationMediaTypeForMediaType(httpContentType.getMimeType());
        ResolveResult resolveResult = ResolveResult.build();
        resolveResult.setContentType(contentType);
        resolveResult.setDidDocumentStream(httpBodyBytes);
        return resolveResult;
    }

    public static String representationMediaTypeForMediaType(String mediaType) {
        if (mediaType == null) throw new NullPointerException();
        if ("application/ld+json".equals(mediaType)) mediaType = Representations.MEDIA_TYPE_JSONLD;
        if ("application/json".equals(mediaType)) mediaType = Representations.MEDIA_TYPE_JSON;
        if ("application/cbor".equals(mediaType)) mediaType = Representations.MEDIA_TYPE_CBOR;
        if (! Representations.MEDIA_TYPES.contains(mediaType)) return null;
        return mediaType;
    }

    public static boolean isResolveResultContentType(ContentType contentType) {
        ContentType resolveResultContentType = ContentType.parse(ResolveResult.MEDIA_TYPE);
        return resolveResultContentType.getMimeType().equals(contentType.getMimeType()) && resolveResultContentType.getParameter("profile").equals(contentType.getParameter("profile"));
     }

    public static boolean isResolveResultContent(String contentString) throws IOException {
        try {
            Map<String, Object> json = objectMapper.readValue(contentString, Map.class);
            if (! json.containsKey("didDocument")) return false;
        } catch (JsonProcessingException ex) {
            return false;
        }
        return true;
    }
}
