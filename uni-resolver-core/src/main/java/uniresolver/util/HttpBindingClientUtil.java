package uniresolver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.representations.Representations;
import foundation.identity.did.representations.consumption.RepresentationConsumer;
import foundation.identity.did.representations.production.RepresentationProducerDID;
import foundation.identity.did.representations.production.RepresentationProducerDIDCBOR;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;
import uniresolver.result.Result;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

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

        String contentType = resolveResultContentTypeFromHttpBodyResolveResult(resolveResult);
        resolveResult.setContentType(contentType);

        // error

        resultError(resolveResult, ResolutionException::determineErrorType, ResolutionException::determineErrorTitle);

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

        String contentType = dereferenceResultContentTypeFromHttpBodyDereferenceResult(dereferenceResult);
        dereferenceResult.setContentType(contentType);

        // error

        resultError(dereferenceResult, DereferencingException::determineErrorType, DereferencingException::determineErrorTitle);

        // finish result

        dereferenceResult.setContent(content);

        // done

        return dereferenceResult;
    }

    public static ResolveResult fromHttpBodyDidDocument(ContentType httpContentType, byte[] httpBodyBytes) throws IOException {

        if (log.isDebugEnabled()) log.debug("Deserializing DID document from HTTP body.");

        // prepare result

        ResolveResult resolveResult = ResolveResult.build();

        // contentType

        String contentType = resolveResultContentTypeFromHttpBodyDidDocument(httpContentType, httpBodyBytes);
        resolveResult.setContentType(contentType);

        // didDocument

        byte[] didDocumentBytes = httpBodyBytes;
        DIDDocument didDocument = RepresentationConsumer.consume(didDocumentBytes, contentType);
        resolveResult.setDidDocument(didDocument);

        // done

        return resolveResult;
    }

    public static DereferenceResult fromHttpBodyContent(ContentType httpContentType, byte[] httpBodyBytes) throws IOException {

        if (log.isDebugEnabled()) log.debug("Deserializing content from HTTP body.");

        // prepare result

        DereferenceResult dereferenceResult = DereferenceResult.build();

        // contentType

        String contentType = dereferenceResultContentTypeFromHttpBodyContent(httpContentType, httpBodyBytes);
        dereferenceResult.setContentType(contentType);

        // content

        byte[] contentBytes = httpBodyBytes;
        dereferenceResult.setContent(contentBytes);

        // done

        return dereferenceResult;
    }

    /*
     * Media Type methods
     */

    private static String resolveResultContentTypeFromHttpBodyResolveResult(ResolveResult resolveResult) {
        String contentType = resolveResult.getContentType();
        String determinedContentType;
        if (contentType == null) {
            determinedContentType = Representations.DEFAULT_MEDIA_TYPE;
        } else {
            determinedContentType = switch (contentType) {
                case "application/did+ld+json",
                     "application/did+json",
                     "application/ld+json",
                     "application/json" ->
                        RepresentationProducerDID.MEDIA_TYPE;
                case "application/cbor" ->
                        RepresentationProducerDIDCBOR.MEDIA_TYPE;
                default -> contentType;
            };
        }
        if (log.isDebugEnabled()) log.debug("Based on content type " + contentType + " and content, determined resolve result 'contentType': " + determinedContentType);
        return determinedContentType;
    }

    private static String dereferenceResultContentTypeFromHttpBodyDereferenceResult(DereferenceResult dereferenceResult) {
        String contentType = dereferenceResult.getContentType();
        String determinedContentType;
        if (contentType == null) {
            determinedContentType = Representations.DEFAULT_MEDIA_TYPE;
        } else {
            determinedContentType = contentType;
        }
        if (log.isDebugEnabled()) log.debug("Based on content type " + contentType + " and content, determined dereference result 'contentType': " + determinedContentType);
        return determinedContentType;
    }

    private static String resolveResultContentTypeFromHttpBodyDidDocument(ContentType httpContentType, byte[] httpBodyBytes) {
        String contentType = httpContentType.getMimeType();
        String determinedContentType;
        if (contentType == null) {
            determinedContentType = Representations.DEFAULT_MEDIA_TYPE;
        } else {
            determinedContentType = switch (contentType) {
                case "application/did+ld+json",
                     "application/did+json",
                     "application/ld+json",
                     "application/json" ->
                        RepresentationProducerDID.MEDIA_TYPE;
                case "application/cbor" ->
                        RepresentationProducerDIDCBOR.MEDIA_TYPE;
                default -> contentType;
            };
        }
        if (log.isDebugEnabled()) log.debug("Based on HTTP Content-Type " + httpContentType + " and content, determined resolve result 'contentType': " + determinedContentType);
        return determinedContentType;
    }

    private static String dereferenceResultContentTypeFromHttpBodyContent(ContentType httpContentType, byte[] httpBodyBytes) {
        String determinedContentType = null;
        if (httpContentType != null) {
            determinedContentType = httpContentType.getMimeType();
        }
        if (determinedContentType == null) determinedContentType = DEREFERENCE_RESULT_DEFAULT_CONTENT_TYPE;
        if (log.isDebugEnabled()) log.debug("Based on HTTP Content-Type " + httpContentType + " and content, determined dereference result 'contentType': " + determinedContentType);
        return determinedContentType;
    }

    private static void resultError(Result result, Function<String, String> determineErrorType, Function<String, String> determineErrorTitle) {
        Object error = result.getFunctionMetadata().get("error");
        if (error instanceof String errorString) {
            String errorMessage = (String) result.getFunctionMetadata().get("errorMessage");
            if (errorMessage == null) errorMessage = (String) result.getFunctionMetadata().get("message");
            String errorType = determineErrorType.apply(errorString);
            String errorTitle = determineErrorTitle.apply(errorType);
            String errorDetail = errorMessage;
            result.getFunctionMetadata().remove("error");
            result.getFunctionMetadata().remove("errorMessage");
            result.setError(errorType, errorTitle);
            if (errorDetail != null) result.setErrorDetail(errorDetail);
            if (log.isDebugEnabled()) log.debug("Based on error '" + error + "' and '" + errorMessage + "', determined error object: " + result.getFunctionMetadata().get("error"));
        }
    }

    /*
     * Helper methods
     */

    public static boolean isResolveResultContentType(ContentType contentType) {
        boolean isResolveResultContentType = false;
        if (Objects.equals(ResolveResult.CONTENT_TYPE.getMimeType(), contentType.getMimeType()) &&
                Objects.equals(ResolveResult.CONTENT_TYPE.getParameter("profile"), contentType.getParameter("profile"))) isResolveResultContentType = true;
        if (Objects.equals(ResolveResult.LEGACY_CONTENT_TYPE.getMimeType(), contentType.getMimeType()) &&
                Objects.equals(ResolveResult.LEGACY_CONTENT_TYPE.getParameter("profile"), contentType.getParameter("profile"))) isResolveResultContentType = true;
        if (log.isDebugEnabled()) log.debug("isResolveResultContentType({}): {}", contentType, isResolveResultContentType);
        return isResolveResultContentType;
    }

    public static boolean isDereferenceResultContentType(ContentType contentType) {
        boolean isDereferenceResultContentType = false;
        if (Objects.equals(DereferenceResult.CONTENT_TYPE.getMimeType(), contentType.getMimeType()) &&
                Objects.equals(DereferenceResult.CONTENT_TYPE.getParameter("profile"), contentType.getParameter("profile"))) isDereferenceResultContentType = true;
        if (Objects.equals(DereferenceResult.LEGACY_CONTENT_TYPE.getMimeType(), contentType.getMimeType()) &&
                Objects.equals(DereferenceResult.LEGACY_CONTENT_TYPE.getParameter("profile"), contentType.getParameter("profile"))) isDereferenceResultContentType = true;
        if (log.isDebugEnabled()) log.debug("isDereferenceResultContentType({}): {}", contentType, isDereferenceResultContentType);
        return isDereferenceResultContentType;
    }

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
