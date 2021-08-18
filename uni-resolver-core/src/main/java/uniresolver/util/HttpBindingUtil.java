package uniresolver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.representations.Representations;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpBindingUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ResolveRepresentationResult fromHttpBodyResolveRepresentationResult(String httpBody) throws IOException {
        if (log.isDebugEnabled()) log.debug("Deserializing resolve representation result from HTTP body.");

        ResolveRepresentationResult resolveRepresentationResult = ResolveRepresentationResult.build();
        Map<String, Object> json = objectMapper.readValue(httpBody, LinkedHashMap.class);

        if (json.containsKey("didResolutionMetadata")) resolveRepresentationResult.setDidResolutionMetadata((Map<String, Object>) json.get("didResolutionMetadata"));
        if (json.containsKey("didDocumentMetadata")) resolveRepresentationResult.setDidDocumentMetadata((Map<String, Object>) json.get("didDocumentMetadata"));

        if (resolveRepresentationResult.getDidResolutionMetadata() == null) resolveRepresentationResult.setDidResolutionMetadata(new LinkedHashMap<>());
        if (resolveRepresentationResult.getDidDocumentMetadata() == null) resolveRepresentationResult.setDidDocumentMetadata(new LinkedHashMap<>());

        byte[] didDocumentStream = new byte[0];
        Object didDocument = json.get("didDocument");
        if (didDocument instanceof Map) {
            didDocumentStream = objectMapper.writeValueAsBytes(didDocument);
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
        }

        resolveRepresentationResult.setDidDocumentStream(didDocumentStream);
        return resolveRepresentationResult;
    }

    public static DereferenceResult fromHttpBodyDereferenceResult(String httpBody) throws IOException {
        if (log.isDebugEnabled()) log.debug("Deserializing dereference result from HTTP body.");

        DereferenceResult dereferenceResult = DereferenceResult.build();
        Map<String, Object> json = objectMapper.readValue(httpBody, LinkedHashMap.class);

        if (json.containsKey("dereferencingMetadata")) dereferenceResult.setDereferencingMetadata((Map<String, Object>) json.get("dereferencingMetadata"));
        if (json.containsKey("contentMetadata")) dereferenceResult.setContentMetadata((Map<String, Object>) json.get("contentMetadata"));

        if (dereferenceResult.getDereferencingMetadata() == null) dereferenceResult.setDereferencingMetadata(new LinkedHashMap<>());
        if (dereferenceResult.getContentMetadata() == null) dereferenceResult.setContentMetadata(new LinkedHashMap<>());

        byte[] contentStream = new byte[0];
        Object content = json.get("content");
        if (content instanceof Map) {
            contentStream = objectMapper.writeValueAsBytes(content);
        } else if (content instanceof String) {
            if (((String) content).isEmpty()) {
                contentStream = new byte[0];
            } else {
                try {
                    contentStream = Hex.decodeHex((String) content);
                } catch (DecoderException ex) {
                    contentStream = ((String) content).getBytes(StandardCharsets.UTF_8);
                }
            }
        }

        dereferenceResult.setContentStream(contentStream);
        return dereferenceResult;
    }

    public static ResolveRepresentationResult fromHttpBodyDidDocument(byte[] httpBodyBytes, ContentType httpContentType) {
        if (log.isDebugEnabled()) log.debug("Deserializing DID document from HTTP body.");

        if (httpContentType == null) {
            if (log.isDebugEnabled()) log.warn("No content type. Assuming default " + Representations.DEFAULT_MEDIA_TYPE);
            httpContentType = ContentType.create(Representations.DEFAULT_MEDIA_TYPE);
        }

        String contentType = representationMediaTypeForMediaType(httpContentType.getMimeType());

        ResolveRepresentationResult resolveRepresentationResult = ResolveRepresentationResult.build();
        resolveRepresentationResult.setContentType(contentType);
        resolveRepresentationResult.setDidDocumentStream(httpBodyBytes);
        return resolveRepresentationResult;
    }

    public static String representationMediaTypeForMediaType(String mediaTypeString) {
        if (mediaTypeString == null) throw new NullPointerException();
        ContentType mediaType = mediaTypeString == null ? null : ContentType.parse(mediaTypeString);
        String representationMediaType = null;
        if ("application/ld+json".equals(mediaType.getMimeType())) representationMediaType = Representations.MEDIA_TYPE_JSONLD;
        if ("application/json".equals(mediaType.getMimeType())) representationMediaType = Representations.MEDIA_TYPE_JSON;
        if ("application/cbor".equals(mediaType.getMimeType())) representationMediaType = Representations.MEDIA_TYPE_CBOR;
        if (Representations.MEDIA_TYPES.contains(mediaType.getMimeType())) representationMediaType = mediaType.getMimeType();
        return representationMediaType;
    }

    private static final ContentType RESOLVE_RESULT_CONTENT_TYPE = ContentType.parse(ResolveResult.MEDIA_TYPE);

    public static boolean isResolveResultContentType(ContentType contentType) {
        boolean isResolveResultMimeTypeEquals = RESOLVE_RESULT_CONTENT_TYPE.getMimeType().equals(contentType.getMimeType());
        boolean isResolveResultProfileEquals = RESOLVE_RESULT_CONTENT_TYPE.getParameter("profile").equals(contentType.getParameter("profile"));
        return isResolveResultMimeTypeEquals && isResolveResultProfileEquals;
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
