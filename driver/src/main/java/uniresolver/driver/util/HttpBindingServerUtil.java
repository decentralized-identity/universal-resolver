package uniresolver.driver.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.representations.Representations;
import foundation.identity.did.representations.production.RepresentationProducerDID;
import foundation.identity.did.representations.production.RepresentationProducerDIDCBOR;
import foundation.identity.did.representations.production.RepresentationProducerDIDJSON;
import foundation.identity.did.representations.production.RepresentationProducerDIDJSONLD;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;
import uniresolver.result.Result;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpBindingServerUtil {

    public static final String RESOLVE_DEFAULT_ACCEPT = Representations.DEFAULT_MEDIA_TYPE;
    public static final String DEREFERENCE_DEFAULT_ACCEPT = "*/*";

    private static final Logger log = LoggerFactory.getLogger(HttpBindingServerUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);

    /*
     * HTTP status code
     */

    public static int httpStatusCodeForResult(Result result) {
        if (result.getFunctionMetadata() != null && result.getFunctionMetadata().containsKey("_http_code"))
            return (Integer) result.getFunctionMetadata().get("_http_code");
        if (ResolutionException.ERROR_NOT_FOUND.equals(result.getError()))
            return HttpStatus.SC_NOT_FOUND;
        else if (ResolutionException.ERROR_INVALID_DID.equals(result.getError()) || DereferencingException.ERROR_INVALID_DID_URL.equals(result.getError()))
            return HttpStatus.SC_BAD_REQUEST;
        else if (ResolutionException.ERROR_REPRESENTATION_NOT_SUPPORTED.equals(result.getError()) || DereferencingException.ERROR_CONTENT_TYPE_NOT_SUPPORTED.equals(result.getError()))
            return HttpStatus.SC_NOT_ACCEPTABLE;
        else if (ResolutionException.ERROR_METHOD_NOT_SUPPORTED.equals(result.getError()))
            return HttpStatus.SC_NOT_IMPLEMENTED;
        else if (result.isErrorResult())
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        else if (result.getFunctionContentMetadata() != null && Boolean.TRUE.equals(result.getFunctionContentMetadata().get("deactivated")))
            return HttpStatus.SC_GONE;
        else
            return HttpStatus.SC_OK;
    }

    /*
     * HTTP body
     */

    public static String httpBodyForResult(Result result) throws IOException {
        String functionContentProperty;
        String functionMetadataProperty;
        String functionContentMetadataProperty;

        if (result instanceof ResolveResult) {
            functionContentProperty = "didDocument";
            functionMetadataProperty = "didResolutionMetadata";
            functionContentMetadataProperty = "didDocumentMetadata";
        } else if (result instanceof DereferenceResult) {
            functionContentProperty = "content";
            functionMetadataProperty = "dereferencingMetadata";
            functionContentMetadataProperty = "contentMetadata";
        } else {
            throw new IllegalArgumentException("Invalid result: " + result.getClass());
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("@context", result.getDefaultContext());
        byte[] content = result.getFunctionContent();
        if (content == null || content.length == 0) {
            json.put(functionContentProperty, null);
        } else if (isContentJson(result)) {
            json.put(functionContentProperty, objectMapper.readValue(new ByteArrayInputStream(content), LinkedHashMap.class));
        } else if (isContentText(result)) {
            json.put(functionContentProperty, new String(content, StandardCharsets.UTF_8));
        } else {
            json.put(functionContentProperty, Base64.getEncoder().encodeToString(content));
        }
        json.put(functionMetadataProperty, result.getFunctionMetadata());
        json.put(functionContentMetadataProperty, result.getFunctionContentMetadata());

        String jsonString = objectMapper.writeValueAsString(json);
        if (log.isDebugEnabled()) log.debug("HTTP body for result: " + jsonString);
        return jsonString;
    }

    public static String httpContentTypeForResult(Result result) {
        if (result instanceof ResolveResult) {
            return ResolveResult.MEDIA_TYPE;
        } else if (result instanceof DereferenceResult) {
            return DereferenceResult.MEDIA_TYPE;
        } else {
            throw new IllegalArgumentException("Invalid result: " + result.getClass());
        }
    }

    /*
     * Media Type methods
     */

    public static String resolveAcceptForHttpAccepts(List<MediaType> httpAcceptMediaTypes) {
        for (MediaType httpAcceptMediaType : httpAcceptMediaTypes) {
            if (MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, ResolveResult.MEDIA_TYPE)) return RESOLVE_DEFAULT_ACCEPT;
            if (MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, DereferenceResult.MEDIA_TYPE)) return RESOLVE_DEFAULT_ACCEPT;

            ContentType mediaType = ContentType.parse(httpAcceptMediaType.toString());
            if (Representations.isProducibleMediaType(mediaType.getMimeType())) return mediaType.getMimeType();
            else if ("application/ld+json".equals(mediaType.getMimeType())) return RepresentationProducerDIDJSONLD.MEDIA_TYPE;
            else if ("application/json".equals(mediaType.getMimeType())) return RepresentationProducerDIDJSON.MEDIA_TYPE;
            else if ("application/cbor".equals(mediaType.getMimeType())) return RepresentationProducerDIDCBOR.MEDIA_TYPE;
        }
        return RESOLVE_DEFAULT_ACCEPT;
    }

    public static String dereferenceAcceptForHttpAccepts(List<MediaType> httpAcceptMediaTypes) {
        for (MediaType httpAcceptMediaType : httpAcceptMediaTypes) {
            if (MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, ResolveResult.MEDIA_TYPE)) return DEREFERENCE_DEFAULT_ACCEPT;
            if (MediaTypeUtil.isMediaTypeAcceptable(httpAcceptMediaType, DereferenceResult.MEDIA_TYPE)) return DEREFERENCE_DEFAULT_ACCEPT;

            return httpAcceptMediaType.toString();
        }
        return DEREFERENCE_DEFAULT_ACCEPT;
    }

    /*
     * Helper methods
     */

    private static boolean isContentJson(Result result) throws IOException {
        boolean isContentTypeJson = RepresentationProducerDID.MEDIA_TYPE.equals(result.getContentType()) || result.getContentType().contains("+json") || result.getContentType().contains("/json");
        boolean isContentJson = result.getFunctionContent()[0] == '{' || result.getFunctionContent()[0] == '[';
        return isContentTypeJson && isContentJson;
    }

    private static boolean isContentText(Result result) {
        return result.getContentType().startsWith("text/");
    }
}
