package uniresolver.driver.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.representations.Representations;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.result.*;
import uniresolver.util.HttpBindingUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HttpBindingServerUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingServerUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);

    public static String toHttpBodyStreamResult(StreamResult streamResult) throws IOException {
        if (streamResult instanceof ResolveRepresentationResult) {
            return toHttpBodyResolveRepresentationResult((ResolveRepresentationResult) streamResult);
        } else if (streamResult instanceof DereferenceResult) {
            return toHttpBodyDereferenceResult((DereferenceResult) streamResult);
        } else {
            throw new IllegalArgumentException("Invalid stream result: " + streamResult.getClass());
        }
    }

    public static String toHttpBodyResolveRepresentationResult(ResolveRepresentationResult resolveRepresentationResult) throws IOException {
        if (log.isDebugEnabled()) log.debug("Serializing resolve result to HTTP body.");
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("didResolutionMetadata", resolveRepresentationResult.getDidResolutionMetadata());
        if (resolveRepresentationResult.getDidDocumentStream() == null || resolveRepresentationResult.getDidDocumentStream().length == 0) {
            json.put("didDocument", null);
        } else if (isJson(resolveRepresentationResult.getDidDocumentStream())) {
            json.put("didDocument", objectMapper.readValue(new ByteArrayInputStream(resolveRepresentationResult.getDidDocumentStream()), LinkedHashMap.class));
        } else {
            json.put("didDocument", Hex.encodeHexString(resolveRepresentationResult.getDidDocumentStream()));
        }
        json.put("didDocumentMetadata", resolveRepresentationResult.getDidDocumentMetadata());
        return objectMapper.writeValueAsString(json);
    }

    public static String toHttpBodyDereferenceResult(DereferenceResult dereferenceResult) throws IOException {
        if (log.isDebugEnabled()) log.debug("Serializing dereference result to HTTP body.");
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("dereferencingMetadata", dereferenceResult.getDereferencingMetadata());
        if (dereferenceResult.getContentStream() == null || dereferenceResult.getContentStream().length == 0) {
            json.put("content", null);
        } else if (isJson(dereferenceResult.getContentStream())) {
            json.put("content", objectMapper.readValue(new ByteArrayInputStream(dereferenceResult.getContentStream()), LinkedHashMap.class));
        } else {
            json.put("content", Hex.encodeHexString(dereferenceResult.getContentStream()));
        }
        json.put("contentMetadata", dereferenceResult.getContentMetadata());
        return objectMapper.writeValueAsString(json);
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
        if (acceptMediaType.includes(mediaType))
            acceptable = true;
        else if (acceptMediaType.getType().equals(mediaType.getType()) && mediaType.getSubtype().endsWith("+" + acceptMediaType.getSubtype()))
            acceptable = true;
        if (!acceptMediaType.isWildcardType() && !acceptMediaType.isWildcardSubtype()) {
            if (acceptMediaType.getParameters() != null) {
                acceptable &= Objects.equals(acceptMediaType.getParameter("profile"), mediaType.getParameter("profile"));
            }
        }
        if (log.isDebugEnabled()) log.debug("Checking if media type " + mediaType + " is acceptable for " + acceptMediaType + ": " + acceptable);
        return acceptable;
    }

    public static int httpStatusCodeForResult(Result result) {
        if (ResolveResult.ERROR_NOTFOUND.equals(result.getError()))
            return HttpStatus.SC_NOT_FOUND;
        else if (ResolveResult.ERROR_INVALIDDID.equals(result.getError()))
            return HttpStatus.SC_BAD_REQUEST;
        else if (ResolveResult.ERROR_REPRESENTATIONNOTSUPPORTED.equals(result.getError()))
            return HttpStatus.SC_NOT_ACCEPTABLE;
        else if (result.isErrorResult())
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        else
            return HttpStatus.SC_OK;
    }

    /*
     * Helper methods
     */

    private static boolean isJson(byte[] bytes) {
        try {
            return objectMapper.getFactory().createParser(bytes).readValueAsTree() != null;
        } catch (IOException ex) {
            return false;
        }
    }
}
