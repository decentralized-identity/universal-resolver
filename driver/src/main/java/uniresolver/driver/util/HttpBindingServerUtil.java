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
import uniresolver.util.HttpBindingUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class HttpBindingServerUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingServerUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);

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
        } else if (isContentJson(streamResult)) {
            json.put(functionContentProperty, objectMapper.readValue(new ByteArrayInputStream(streamResult.getFunctionContentStream()), LinkedHashMap.class));
        } else if (isContentText(streamResult)) {
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

    public static String acceptForHttpAcceptMediaTypes(List<MediaType> httpAcceptMediaTypes) {
        for (ContentType httpAcceptMediaType : httpAcceptMediaTypes.stream().map((x) -> ContentType.parse(x.toString())).collect(Collectors.toList())) {
            if (HttpBindingUtil.isResolveResultContentType(httpAcceptMediaType)) {
                return Representations.DEFAULT_MEDIA_TYPE;
            }
            String representationMediaType = HttpBindingUtil.representationMediaTypeForMediaType(httpAcceptMediaType.getMimeType());
            if (representationMediaType != null) return representationMediaType;
        }
        return Representations.DEFAULT_MEDIA_TYPE;
    }

    public static boolean isMediaTypeAcceptable(MediaType acceptMediaType, String mediaTypeString) {
        if (mediaTypeString == null) throw new NullPointerException();
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        boolean acceptable = false;
        if (acceptMediaType.includes(mediaType)) {
            acceptable = true;
        }
        if (acceptMediaType.getType().equals(mediaType.getType()) && mediaType.getSubtype().endsWith("+" + acceptMediaType.getSubtype())) {
            acceptable = true;
        }
        if (!MediaType.ALL.equals(acceptMediaType)) {
            if (acceptMediaType.getParameters() != null) {
                acceptable &= Objects.equals(acceptMediaType.getParameter("profile"), mediaType.getParameter("profile"));
            }
        }
        if (log.isDebugEnabled()) log.debug("Checking if media type " + mediaType + " is acceptable for " + acceptMediaType + ": " + acceptable);
        return acceptable;
    }

    public static int httpStatusCodeForResult(Result result) {
        if (ResolutionException.ERROR_NOTFOUND.equals(result.getError()))
            return HttpStatus.SC_NOT_FOUND;
        else if (ResolutionException.ERROR_INVALIDDID.equals(result.getError()) || DereferencingException.ERROR_INVALIDDIDURL.equals(result.getError()))
            return HttpStatus.SC_BAD_REQUEST;
        else if (ResolutionException.ERROR_REPRESENTATIONNOTSUPPORTED.equals(result.getError()))
            return HttpStatus.SC_NOT_ACCEPTABLE;
        else if (result.isErrorResult())
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        else
            return HttpStatus.SC_OK;
    }

    /*
     * Helper methods
     */

    private static boolean isContentJson(StreamResult streamResult) {
        final MediaType acceptMediaType = MediaType.valueOf("application/json");
        return isMediaTypeAcceptable(acceptMediaType, streamResult.getContentType());
    }

    private static boolean isContentText(StreamResult streamResult) {
        final MediaType acceptMediaType = MediaType.valueOf("text/*");
        return isMediaTypeAcceptable(acceptMediaType, streamResult.getContentType());
    }
}
