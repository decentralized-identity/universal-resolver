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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpBindingClientUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingClientUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * HTTP body
     */

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
                    didDocumentStream = Base64.getDecoder().decode((String) didDocument);
                } catch (IllegalArgumentException ex) {
                    didDocumentStream = ((String) didDocument).getBytes(StandardCharsets.UTF_8);
                }
            }
        }

        if (resolveRepresentationResult.getContentType() == null) {
            String contentType = null;
            if (httpContentType != null) {
                contentType = contentTypeForHttpContentTypeAndContent(httpContentType.getMimeType(), didDocumentStream);
                if (log.isDebugEnabled()) log.debug("No contentType metadata property in resolution result. Based on HTTP Content-Type " + httpContentType + " and content, determined contentType: " + contentType);
            }
            if (contentType == null) {
                contentType = Representations.DEFAULT_MEDIA_TYPE;
                if (log.isDebugEnabled()) log.debug("Could not determine contentType metadata property. Assuming default DID document representation media type " + Representations.DEFAULT_MEDIA_TYPE);
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
                contentType = contentTypeForHttpContentTypeAndContent(httpContentType.getMimeType(), contentStream);
                if (log.isDebugEnabled()) log.debug("No contentType metadata property in dereference result. Based on HTTP Content-Type " + httpContentType + " and content, determined contentType: " + contentType);
            }
            if (contentType == null) {
                contentType = Representations.DEFAULT_MEDIA_TYPE;
                if (log.isDebugEnabled()) log.debug("Could not determine contentType metadata property. Assuming default DID document representation media type " + Representations.DEFAULT_MEDIA_TYPE);
            }
            dereferenceResult.setContentType(contentType);
        }

        dereferenceResult.setContentStream(contentStream);
        return dereferenceResult;
    }

    public static ResolveRepresentationResult fromHttpBodyDidDocument(byte[] httpBodyBytes, ContentType httpContentType) {
        if (log.isDebugEnabled()) log.debug("Deserializing DID document from HTTP body.");

        byte[] didDocumentStream = httpBodyBytes;

        ResolveRepresentationResult resolveRepresentationResult = ResolveRepresentationResult.build();

        String contentType = null;
        if (httpContentType != null) {
            contentType = contentTypeForHttpContentTypeAndContent(httpContentType.getMimeType(), didDocumentStream);
            if (log.isDebugEnabled()) log.debug("No contentType metadata property. Based on HTTP Content-Type " + httpContentType + " and content, determined contentType: " + contentType);
        }
        if (contentType == null) {
            contentType = Representations.DEFAULT_MEDIA_TYPE;
            if (log.isDebugEnabled()) log.debug("Could not determine contentType metadata property. Assuming default DID document representation media type " + Representations.DEFAULT_MEDIA_TYPE);
        }
        resolveRepresentationResult.setContentType(contentType);

        resolveRepresentationResult.setDidDocumentStream(didDocumentStream);
        return resolveRepresentationResult;
    }

    public static boolean isResolveResultHttpContent(String httpContentString) {
        try {
            Map<String, Object> json = objectMapper.readValue(httpContentString, Map.class);
            return json.containsKey("didDocument");
        } catch (JsonProcessingException ex) {
            return false;
        }
    }

    public static boolean isJsonLdHttpContent(byte[] httpContentBytes) {
        try {
            Map<String, Object> json = objectMapper.readValue(httpContentBytes, Map.class);
            return json.containsKey("@context");
        } catch (IOException ex) {
            return false;
        }
    }

    /*
     * HTTP headers
     */

    public static String contentTypeForHttpContentTypeAndContent(String mediaTypeString, byte[] contentBytes) {
        if (mediaTypeString == null) throw new NullPointerException();
        ContentType mediaType = ContentType.parse(mediaTypeString);
        String representationMediaType = null;
        if (Representations.isRepresentationMediaType(mediaType.getMimeType())) representationMediaType = mediaType.getMimeType();
        else if ("application/ld+json".equals(mediaType.getMimeType())) representationMediaType = Representations.MEDIA_TYPE_JSONLD;
        else if ("application/json".equals(mediaType.getMimeType())) representationMediaType = isJsonLdHttpContent(contentBytes) ? Representations.MEDIA_TYPE_JSONLD : Representations.MEDIA_TYPE_JSON;
        else if ("application/cbor".equals(mediaType.getMimeType())) representationMediaType = Representations.MEDIA_TYPE_CBOR;
        return representationMediaType;
    }
}
