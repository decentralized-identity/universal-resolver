package uniresolver.driver.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.representations.Representations;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uniresolver.result.ResolveResult;
import uniresolver.util.HttpBindingUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HttpBindingServerUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingServerUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);
    }

    public static String toHttpBodyResolveResult(ResolveResult resolveResult) throws IOException {
        if (log.isDebugEnabled()) log.debug("Serializing resolve result to HTTP body.");
        if (resolveResult.getDidDocument() != null) throw new IllegalArgumentException("Cannot serialize abstract DID document.");
        Map<String, Object> json = new HashMap<>();
        json.put("didResolutionMetadata", resolveResult.getDidResolutionMetadata());
        json.put("didDocumentMetadata", resolveResult.getDidDocumentMetadata());
        if (resolveResult.getDidDocumentStream() == null || resolveResult.getDidDocumentStream().length == 0) {
            json.put("didDocument", null);
        } else if (isJson(resolveResult.getDidDocumentStream())) {
            json.put("didDocument", objectMapper.readValue(new ByteArrayInputStream(resolveResult.getDidDocumentStream()), LinkedHashMap.class));
        } else {
            json.put("didDocument", Hex.encodeHexString(resolveResult.getDidDocumentStream()));
        }
        return objectMapper.writeValueAsString(json);
    }

    public static String acceptForHttpAcceptMediaTypes(List<MediaType> httpAcceptMediaTypes) {
        for (ContentType httpAcceptMediaType : httpAcceptMediaTypes.stream().map((x) -> ContentType.parse(x.toString())).collect(Collectors.toList())) {
            if (HttpBindingUtil.isResolveResultContentType(httpAcceptMediaType)) {
                return Representations.MEDIA_TYPE_JSONLD;
            }
            String representationMediaType = HttpBindingUtil.representationMediaTypeForMediaType(httpAcceptMediaType.getMimeType());
            if (representationMediaType != null) return representationMediaType;
        }
        return Representations.DEFAULT_MEDIA_TYPE;
    }

    public static boolean isMediaTypeAcceptable(MediaType acceptMediaType, MediaType contentType) {
        boolean acceptable = false;
        if (acceptMediaType.includes(contentType))
            acceptable = true;
        else if (acceptMediaType.getType().equals(contentType.getType()) && contentType.getSubtype().endsWith("+" + acceptMediaType.getSubtype()))
            acceptable = true;
        if (!acceptMediaType.isWildcardType() && !acceptMediaType.isWildcardSubtype()) {
            if (acceptMediaType.getParameters() != null) {
                acceptable &= Objects.equals(acceptMediaType.getParameter("profile"), contentType.getParameter("profile"));
            }
        }
        if (log.isDebugEnabled()) log.debug("Checking if content type " + contentType + " is acceptable for " + acceptMediaType + ": " + acceptable);
        return acceptable;
    }

    public static int httpStatusCodeForResolveResult(ResolveResult resolveResult) {
        if (ResolveResult.ERROR_NOTFOUND.equals(resolveResult.getError()))
            return HttpStatus.SC_NOT_FOUND;
        else if (ResolveResult.ERROR_INVALIDDID.equals(resolveResult.getError()))
            return HttpStatus.SC_BAD_REQUEST;
        else if (ResolveResult.ERROR_REPRESENTATIONNOTSUPPORTED.equals(resolveResult.getError()))
            return HttpStatus.SC_NOT_ACCEPTABLE;
        else if (resolveResult.isErrorResult())
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
