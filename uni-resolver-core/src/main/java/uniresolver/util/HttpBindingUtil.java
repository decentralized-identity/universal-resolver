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

    public static ResolveRepresentationResult fromHttpBodyResolveRepresentationResult(String httpBody, ContentType httpContentType) throws IOException {
        if (log.isDebugEnabled()) log.debug("Deserializing resolve representation result from HTTP body.");

        ResolveRepresentationResult resolveRepresentationResult = ResolveRepresentationResult.build();
        Map<String, Object> json = objectMapper.readValue(httpBody, LinkedHashMap.class);

        if (json.get("didResolutionMetadata") instanceof Map) resolveRepresentationResult.setDidResolutionMetadata((Map<String, Object>) json.get("didResolutionMetadata"));
        if (json.get("didDocumentMetadata") instanceof Map) resolveRepresentationResult.setDidDocumentMetadata((Map<String, Object>) json.get("didDocumentMetadata"));

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

        if (resolveRepresentationResult.getContentType() == null) {
            String contentType = null;
            if (httpContentType != null) {
                contentType = representationMediaTypeForMediaType(httpContentType.getMimeType());
                if (log.isDebugEnabled()) log.debug("No content type in resolution result. Assuming HTTP header is content type " + httpContentType + ", which corresponds to DID document representation media type " + contentType);
            }
            if (contentType == null) {
                contentType = Representations.DEFAULT_MEDIA_TYPE;
                if (log.isDebugEnabled()) log.debug("No (valid) content type in resolution result or HTTP header. Assuming default DID document representation media type " + Representations.DEFAULT_MEDIA_TYPE);
            }
            resolveRepresentationResult.setContentType(contentType);
        }

        resolveRepresentationResult.setDidDocumentStream(didDocumentStream);
        return resolveRepresentationResult;
    }

    public static DereferenceResult fromHttpBodyDereferenceResult(String httpBody, ContentType httpContentType) throws IOException {
        if (log.isDebugEnabled()) log.debug("Deserializing dereference result from HTTP body.");

        DereferenceResult dereferenceResult = DereferenceResult.build();
        Map<String, Object> json = objectMapper.readValue(httpBody, LinkedHashMap.class);

        if (json.get("dereferencingMetadata") instanceof Map) dereferenceResult.setDereferencingMetadata((Map<String, Object>) json.get("dereferencingMetadata"));
        if (json.get("contentMetadata") instanceof Map) dereferenceResult.setContentMetadata((Map<String, Object>) json.get("contentMetadata"));

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

        if (dereferenceResult.getContentType() == null) {
            String contentType = null;
            if (httpContentType != null) {
                contentType = representationMediaTypeForMediaType(httpContentType.getMimeType());
                if (log.isDebugEnabled()) log.debug("No content type in dereference result. Assuming HTTP header is content type " + httpContentType + ", which corresponds to DID document representation media type " + contentType);
            }
            if (contentType == null) {
                contentType = Representations.DEFAULT_MEDIA_TYPE;
                if (log.isDebugEnabled()) log.debug("No (valid) content type in dereference result or HTTP header. Assuming default DID document representation media type " + Representations.DEFAULT_MEDIA_TYPE);
            }
            dereferenceResult.setContentType(contentType);
        }

        dereferenceResult.setContentStream(contentStream);
        return dereferenceResult;
    }

    public static ResolveRepresentationResult fromHttpBodyDidDocument(byte[] httpBodyBytes, ContentType httpContentType) {
        if (log.isDebugEnabled()) log.debug("Deserializing DID document from HTTP body.");

        ResolveRepresentationResult resolveRepresentationResult = ResolveRepresentationResult.build();

        String contentType = null;
        if (httpContentType != null) {
            contentType = representationMediaTypeForMediaType(httpContentType.getMimeType());
            if (log.isDebugEnabled()) log.debug("No content type. Assuming HTTP header is content type " + httpContentType + ", which corresponds to DID document representation media type " + contentType);
        }
        if (contentType == null) {
            contentType = Representations.DEFAULT_MEDIA_TYPE;
            if (log.isDebugEnabled()) log.debug("No (valid) content type in HTTP header. Assuming default DID document representation media type " + Representations.DEFAULT_MEDIA_TYPE);
        }
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
        if (Representations.isRepresentationMediaType(mediaType.getMimeType())) representationMediaType = mediaType.getMimeType();
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
