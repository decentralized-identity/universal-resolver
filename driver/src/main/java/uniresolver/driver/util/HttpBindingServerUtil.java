package uniresolver.driver.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.representations.Representations;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.DereferencingException;
import uniresolver.ResolutionException;
import uniresolver.result.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpBindingServerUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingServerUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);

    /*
     * HTTP body
     */

    public static String toHttpBodyStreamResult(StreamResult streamResult) throws IOException {
        String functionContentProperty;
        String functionContentStreamProperty;
        String functionMetadataProperty;
        String functionContentMetadataProperty;

        if (streamResult instanceof ResolveRepresentationResult) {
            functionContentProperty = "didDocument";
            functionContentStreamProperty = "didDocumentStream";
            functionMetadataProperty = "didResolutionMetadata";
            functionContentMetadataProperty = "didDocumentMetadata";
        } else if (streamResult instanceof DereferenceResult) {
            functionContentProperty = "content";
            functionContentStreamProperty = "contentStream";
            functionMetadataProperty = "dereferencingMetadata";
            functionContentMetadataProperty = "contentMetadata";
        } else {
            throw new IllegalArgumentException("Invalid stream result: " + streamResult.getClass());
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("@context", ResolveResult.DEFAULT_JSONLD_CONTEXT);
        if (streamResult.getFunctionContentStream() == null || streamResult.getFunctionContentStream().length == 0) {
            json.put(functionContentProperty, null);
        } else if (isContentTypeJson(streamResult)) {
            json.put(functionContentProperty, objectMapper.readValue(new ByteArrayInputStream(streamResult.getFunctionContentStream()), LinkedHashMap.class));
        } else if (isContentTypeText(streamResult)) {
            json.put(functionContentProperty, new String(streamResult.getFunctionContentStream(), StandardCharsets.UTF_8));
        } else {
            json.put(functionContentProperty, Base64.getEncoder().encodeToString(streamResult.getFunctionContentStream()));
        }
        json.put(functionMetadataProperty, streamResult.getFunctionMetadata());
        json.put(functionContentMetadataProperty, streamResult.getFunctionContentMetadata());

        String jsonString = objectMapper.writeValueAsString(json);
        if (log.isDebugEnabled()) log.debug("HTTP body for stream result: " + jsonString);
        return jsonString;
    }

    /*
     * HTTP headers
     */

    public static String acceptForHttpAccepts(List<MediaType> httpAcceptMediaTypes) {
        for (ContentType httpAcceptMediaType : httpAcceptMediaTypes.stream().map((x) -> ContentType.parse(x.toString())).collect(Collectors.toList())) {
            if (ResolveResult.isResolveResultMediaType(httpAcceptMediaType)) {
                return Representations.DEFAULT_MEDIA_TYPE;
            }
            String accept = acceptForHttpAccepts(httpAcceptMediaType.getMimeType());
            if (accept != null) return accept;
        }
        return Representations.DEFAULT_MEDIA_TYPE;
    }

    public static String acceptForHttpAccepts(String httpAcceptMediaType) {
        if (httpAcceptMediaType == null) throw new NullPointerException();
        ContentType mediaType = ContentType.parse(httpAcceptMediaType);
        String accept = null;
        if (Representations.isRepresentationMediaType(mediaType.getMimeType())) accept = mediaType.getMimeType();
        else if ("application/ld+json".equals(mediaType.getMimeType())) accept = Representations.MEDIA_TYPE_JSONLD;
        else if ("application/json".equals(mediaType.getMimeType())) accept = Representations.MEDIA_TYPE_JSON;
        else if ("application/cbor".equals(mediaType.getMimeType())) accept = Representations.MEDIA_TYPE_CBOR;
        return accept;
    }

    /*
     * HTTP status code
     */

    public static int httpStatusCodeForResult(Result result) {
        if (result.getFunctionMetadata() != null && result.getFunctionMetadata().containsKey("_http_code"))
            return (Integer) result.getFunctionMetadata().get("_http_code");
        if (ResolutionException.ERROR_NOTFOUND.equals(result.getError()))
            return HttpStatus.SC_NOT_FOUND;
        else if (ResolutionException.ERROR_INVALIDDID.equals(result.getError()) || DereferencingException.ERROR_INVALIDDIDURL.equals(result.getError()))
            return HttpStatus.SC_BAD_REQUEST;
        else if (ResolutionException.ERROR_REPRESENTATIONNOTSUPPORTED.equals(result.getError()) || DereferencingException.ERROR_CONTENTTYEPNOTSUPPORTED.equals(result.getError()))
            return HttpStatus.SC_NOT_ACCEPTABLE;
        else if (ResolutionException.ERROR_METHODNOTSUPPORTED.equals(result.getError()))
            return HttpStatus.SC_NOT_IMPLEMENTED;
        else if (result.isErrorResult())
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        else if (result.getFunctionContentMetadata() != null && Boolean.TRUE.equals(result.getFunctionContentMetadata().get("deactivated")))
            return HttpStatus.SC_GONE;
        else
            return HttpStatus.SC_OK;
    }

    /*
     * Helper methods
     */

    private static boolean isContentTypeJson(StreamResult streamResult) {
        final MediaType acceptMediaType = MediaType.valueOf("application/json");
        return MediaTypeUtil.isMediaTypeAcceptable(acceptMediaType, streamResult.getContentType());
    }

    private static boolean isContentTypeText(StreamResult streamResult) {
        final MediaType acceptMediaType = MediaType.valueOf("text/*");
        return MediaTypeUtil.isMediaTypeAcceptable(acceptMediaType, streamResult.getContentType());
    }
}
