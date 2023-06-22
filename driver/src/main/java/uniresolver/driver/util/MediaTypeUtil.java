package uniresolver.driver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Objects;

public class MediaTypeUtil {

    private static final Logger log = LoggerFactory.getLogger(MediaTypeUtil.class);

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
        if (! MediaType.ALL.equals(acceptMediaType)) {
            if (acceptMediaType.getParameters() != null) {
                acceptable &= Objects.equals(acceptMediaType.getParameter("profile"), mediaType.getParameter("profile"));
            }
        }
        if (log.isDebugEnabled()) log.debug("Checking if media type " + mediaType + " is acceptable for " + acceptMediaType + ": " + acceptable);
        return acceptable;
    }

    public static boolean isMediaTypeAcceptable(List<MediaType> acceptMediaTypes, String mediaTypeString) {
        for (MediaType acceptMediaType : acceptMediaTypes) {
            if (isMediaTypeAcceptable(acceptMediaType, mediaTypeString)) {
                return true;
            }
        }
        return false;
    }
}
