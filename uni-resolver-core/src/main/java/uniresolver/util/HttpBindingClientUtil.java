package uniresolver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.representations.Representations;
import foundation.identity.did.representations.consumption.RepresentationConsumer;
import foundation.identity.did.representations.consumption.RepresentationConsumerDIDCBOR;
import foundation.identity.did.representations.consumption.RepresentationConsumerDIDJSON;
import foundation.identity.did.representations.consumption.RepresentationConsumerDIDJSONLD;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpBindingClientUtil {

    public static final String RESOLVE_RESULT_DEFAULT_CONTENT_TYPE = Representations.DEFAULT_MEDIA_TYPE;
    public static final String DEREFERENCE_RESULT_DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private static final Logger log = LoggerFactory.getLogger(HttpBindingClientUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * HTTP body
     */

    public static ResolveResult fromHttpBodyResolveResult(String httpBody) throws IOException {

        if (! isResolveResultHttpContent(httpBody)) throw new IllegalArgumentException("Invalid HTTP body: " + httpBody);
        if (log.isDebugEnabled()) log.debug("Deserializing resolve result from HTTP body.");

        // prepare result

        ResolveResult resolveResult = ResolveResult.build();
        Map<String, Object> json = objectMapper.readValue(httpBody, LinkedHashMap.class);

        if (json.get("didResolutionMetadata") instanceof Map) resolveResult.getDidResolutionMetadata().putAll((Map<String, Object>) json.get("didResolutionMetadata"));
        if (json.get("didDocumentMetadata") instanceof Map) resolveResult.getDidDocumentMetadata().putAll((Map<String, Object>) json.get("didDocumentMetadata"));

        // didDocument

        byte[] didDocumentBytes = null;
        Object didDocumentObject = json.get("didDocument");
        if (didDocumentObject == null) didDocumentObject = json.get("didDocumentStream");
        if (didDocumentObject instanceof Map) {
            didDocumentBytes = objectMapper.writeValueAsBytes(didDocumentObject);
        } else if (didDocumentObject instanceof String) {
            if (((String) didDocumentObject).isEmpty()) {
                didDocumentBytes = new byte[0];
            } else {
                try {
                    didDocumentBytes = Base64.getDecoder().decode((String) didDocumentObject);
                } catch (IllegalArgumentException ex) {
                    didDocumentBytes = ((String) didDocumentObject).getBytes(StandardCharsets.UTF_8);
                }
            }
        }

        // contentType

        String contentType = resolveResult.getContentType();
        if (contentType == null) {
            contentType = Representations.DEFAULT_MEDIA_TYPE;
            if (log.isDebugEnabled()) log.debug("Could not determine contentType metadata property. Assuming default DID document representation media type " + Representations.DEFAULT_MEDIA_TYPE);
            resolveResult.setContentType(contentType);
        }

        // error

        Object error = resolveResult.getDidResolutionMetadata().get("error");
        if (error instanceof String errorString) {
            String errorMessage = (String) resolveResult.getFunctionMetadata().get("errorMessage");
            String errorType = ResolutionException.determineErrorType(errorString);
            String errorTitle = ResolutionException.determineErrorTitle(errorType);
            String errorDetail = errorMessage;
            resolveResult.getDidResolutionMetadata().remove("error");
            resolveResult.setError(errorType, errorTitle);
            resolveResult.setErrorDetail(errorDetail);
            if (log.isDebugEnabled()) log.debug("Determined error metadata property from '" + error + "' and '" + errorMessage + "'.");
        }

        // finish result

        DIDDocument didDocument = didDocumentBytes == null ? null : RepresentationConsumer.consume(didDocumentBytes, contentType);
        resolveResult.setDidDocument(didDocument);

        // done

        return resolveResult;
    }

    public static DereferenceResult fromHttpBodyDereferenceResult(String httpBody) throws IOException {

        if (! isDereferenceResultHttpContent(httpBody)) throw new IllegalArgumentException("Invalid HTTP body: " + httpBody);
        if (log.isDebugEnabled()) log.debug("Deserializing dereference result from HTTP body.");

        // prepare result

        DereferenceResult dereferenceResult = DereferenceResult.build();
        Map<String, Object> json = objectMapper.readValue(httpBody, LinkedHashMap.class);

        if (json.get("dereferencingMetadata") instanceof Map) dereferenceResult.getDereferencingMetadata().putAll((Map<String, Object>) json.get("dereferencingMetadata"));
        if (json.get("contentMetadata") instanceof Map) dereferenceResult.getContentMetadata().putAll((Map<String, Object>) json.get("contentMetadata"));

        // content

        byte[] content = null;
        Object contentObject = json.get("content");
        if (contentObject == null) contentObject = json.get("contentStream");
        if (contentObject instanceof Map) {
            content = objectMapper.writeValueAsBytes(contentObject);
        } else if (contentObject instanceof String) {
            if (((String) contentObject).isEmpty()) {
                content = new byte[0];
            } else {
                try {
                    content = Base64.getDecoder().decode((String) contentObject);
                } catch (IllegalArgumentException ex) {
                    content = ((String) contentObject).getBytes(StandardCharsets.UTF_8);
                }
            }
        }

        // contentType

        String contentType = dereferenceResult.getContentType();
        if (contentType == null) {
            contentType = Representations.DEFAULT_MEDIA_TYPE;
            if (log.isDebugEnabled()) log.debug("Could not determine contentType metadata property. Assuming default DID document representation media type " + Representations.DEFAULT_MEDIA_TYPE);
            dereferenceResult.setContentType(contentType);
        }

        // error

        Object error = dereferenceResult.getDereferencingMetadata().get("error");
        if (error instanceof String errorString) {
            String errorMessage = (String) dereferenceResult.getDereferencingMetadata().get("errorMessage");
            String errorType = DereferencingException.determineErrorType(errorString);
            String errorTitle = DereferencingException.determineErrorTitle(errorType);
            String errorDetail = errorMessage;
            dereferenceResult.getDereferencingMetadata().remove("error");
            dereferenceResult.setError(errorType, errorTitle);
            dereferenceResult.setErrorDetail(errorDetail);
            if (log.isDebugEnabled()) log.debug("Determined error metadata property from '" + error + "' and '" + errorMessage + "'.");
        }

        // finish result

        dereferenceResult.setContent(content);

        // done

        return dereferenceResult;
    }

    public static ResolveResult fromHttpBodyDidDocument(byte[] httpBodyBytes, ContentType httpContentType) throws IOException {

        if (log.isDebugEnabled()) log.debug("Deserializing DID document from HTTP body.");

        // prepare result

        ResolveResult resolveResult = ResolveResult.build();

        // didDocument

        byte[] didDocumentBytes = httpBodyBytes;

        // contentType

        String contentType = null;
        if (httpContentType != null) {
            contentType = resolveResultContentTypeForHttpContentTypeAndContent(httpContentType.getMimeType(), didDocumentBytes);
            if (log.isDebugEnabled()) log.debug("No contentType metadata property. Based on HTTP Content-Type " + httpContentType + " and content, determined contentType: " + contentType);
        }
        if (contentType == null) {
            contentType = RESOLVE_RESULT_DEFAULT_CONTENT_TYPE;
            if (log.isDebugEnabled()) log.debug("Could not determine contentType metadata property. Assuming default " + RESOLVE_RESULT_DEFAULT_CONTENT_TYPE);
        }
        resolveResult.setContentType(contentType);

        // finish result

        DIDDocument didDocument = RepresentationConsumer.consume(didDocumentBytes, contentType);
        resolveResult.setDidDocument(didDocument);

        // done

        return resolveResult;
    }

    public static DereferenceResult fromHttpBodyContent(byte[] httpBodyBytes, ContentType httpContentType) throws IOException {

        if (log.isDebugEnabled()) log.debug("Deserializing content from HTTP body.");

        // prepare result

        DereferenceResult dereferenceResult = DereferenceResult.build();

        // content

        byte[] contentBytes = httpBodyBytes;

        // contentType

        String contentType = null;
        if (httpContentType != null) {
            contentType = dereferenceResultContentTypeForHttpContentTypeAndContent(httpContentType.getMimeType(), contentBytes);
            if (log.isDebugEnabled()) log.debug("No contentType metadata property. Based on HTTP Content-Type " + httpContentType + " and content, determined contentType: " + contentType);
        }
        if (contentType == null) {
            contentType = DEREFERENCE_RESULT_DEFAULT_CONTENT_TYPE;
            if (log.isDebugEnabled()) log.debug("Could not determine contentType metadata property. Assuming default " + DEREFERENCE_RESULT_DEFAULT_CONTENT_TYPE);
        }
        dereferenceResult.setContentType(contentType);

        // finish result

        dereferenceResult.setContent(contentBytes);

        // done

        return dereferenceResult;
    }

    /*
     * Media Type methods
     */

    public static String resolveResultContentTypeForHttpContentTypeAndContent(String mediaTypeString, byte[] content) {
        if (mediaTypeString == null) throw new NullPointerException();
        ContentType mediaType = ContentType.parse(mediaTypeString);
        String contentType = null;
        if (Representations.isConsumableMediaType(mediaType.getMimeType())) contentType = mediaType.getMimeType();
        else if ("application/ld+json".equals(mediaType.getMimeType())) contentType = RepresentationConsumerDIDJSONLD.MEDIA_TYPE;
        else if ("application/json".equals(mediaType.getMimeType())) contentType = isJsonLdHttpContent(content) ? RepresentationConsumerDIDJSONLD.MEDIA_TYPE : RepresentationConsumerDIDJSON.MEDIA_TYPE;
        else if ("application/cbor".equals(mediaType.getMimeType())) contentType = RepresentationConsumerDIDCBOR.MEDIA_TYPE;
        return contentType;
    }

    public static String dereferenceResultContentTypeForHttpContentTypeAndContent(String mediaTypeString, byte[] content) {
        if (mediaTypeString == null) throw new NullPointerException();
        ContentType mediaType = ContentType.parse(mediaTypeString);
        String contentType = mediaType.getMimeType();
        return contentType;
    }

    /*
     * Helper methods
     */

    public static boolean isResolveResultHttpContent(String httpContentString) {
        try {
            Map<String, Object> json = objectMapper.readValue(httpContentString, Map.class);
            return json.containsKey("didDocument") || json.containsKey("didDocumentStream");
        } catch (JsonProcessingException ex) {
            return false;
        }
    }

    public static boolean isDereferenceResultHttpContent(String httpContentString) {
        try {
            Map<String, Object> json = objectMapper.readValue(httpContentString, Map.class);
            return json.containsKey("content") || json.containsKey("contentStream");
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
}
