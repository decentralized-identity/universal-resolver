package uniresolver.util;

import foundation.identity.did.DIDDocument;
import foundation.identity.did.representations.Representations;
import foundation.identity.did.representations.consumption.RepresentationConsumer;
import foundation.identity.did.representations.production.RepresentationProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResolveResultUtil {

    private static final Logger log = LoggerFactory.getLogger(ResolveResultUtil.class);

    public static ResolveResult convertToResolveResult(ResolveResult resolveRepresentationResult) throws ResolutionException {

        if (resolveRepresentationResult == null) throw new NullPointerException();

        // check result of resolveRepresentation()

        if (resolveRepresentationResult.getDidResolutionMetadata() == null) throw new IllegalArgumentException("No 'didResolutionMetadata' returned from resolveRepresentation().");
        if (resolveRepresentationResult.getDidDocumentMetadata() == null) throw new IllegalArgumentException("No 'didDocumentMetadata' returned from resolveRepresentation().");
        if (resolveRepresentationResult.getDidDocument() != null) throw new IllegalArgumentException("Unexpected 'didDocument' returned from resolveRepresentation().");
        if (resolveRepresentationResult.getDidDocumentStream() == null) throw new IllegalArgumentException("No 'didDocumentStream' returned from resolveRepresentation().");
        if (resolveRepresentationResult.isErrorResult() && resolveRepresentationResult.getDidDocumentStream().length > 0) throw new IllegalArgumentException("Unexpected 'didDocumentStream' despite error returned from resolveRepresentation().");

        if (resolveRepresentationResult.getContentType() == null) throw new IllegalArgumentException("No 'contentType' in 'didResolutionMetadata' returned from resolveRepresentation().");

        // create result of resolve()

        ResolveResult resolveResult = ResolveResult.build();
        resolveResult.setDidResolutionMetadata(new HashMap<>(resolveRepresentationResult.getDidResolutionMetadata()));
        resolveResult.getDidResolutionMetadata().remove("contentType");
        resolveResult.setDidDocumentMetadata(new HashMap<>(resolveRepresentationResult.getDidDocumentMetadata()));

        // convert didDocumentStream to didDocument

        RepresentationConsumer representationConsumer = null;
        DIDDocument didDocument;

        if (!resolveResult.isErrorResult()) {

            String contentType = (String) resolveRepresentationResult.getContentType();
            byte[] didDocumentStream = resolveRepresentationResult.getDidDocumentStream();

            representationConsumer = Representations.getConsumer(contentType);
            if (representationConsumer == null) throw new ResolutionException(ResolveResult.makeErrorResolveResult(ResolveResult.ERROR_REPRESENTATIONNOTSUPPORTED, "No consumer for " + contentType));

            try {
                RepresentationConsumer.Result result = representationConsumer.consume(didDocumentStream);
                Map<String, Object> map = new LinkedHashMap<>();
                map.putAll(result.representationSpecificEntries.get(contentType));
                map.putAll(result.didDocument);
                didDocument = DIDDocument.fromMap(map);
            } catch (IOException ex) {
                throw new ResolutionException("Problem during consumption of " + contentType + ": " + ex.getMessage(), ex);
            }
        } else {

            didDocument = null;
        }

        resolveResult.setDidDocument(didDocument);

        // done

        if (log.isDebugEnabled()) log.debug("Converted to resolve() result using " + representationConsumer.getClass().getSimpleName() + ": " + resolveResult);
        return resolveResult;
    }

    public static ResolveResult convertToResolveRepresentationResult(ResolveResult resolveResult, String mediaType) throws ResolutionException {

        if (resolveResult == null) throw new NullPointerException();
        if (mediaType == null) throw new IllegalArgumentException("No 'mediaType' provided.");

        // check result of resolve()

        if (resolveResult.getDidResolutionMetadata() == null) throw new IllegalArgumentException("No 'didResolutionMetadata' returned from resolve().");
        if (resolveResult.getDidDocumentMetadata() == null) throw new IllegalArgumentException("No 'didDocumentMetadata' returned from resolve().");
        if ((!resolveResult.isErrorResult()) && resolveResult.getDidDocument() == null) throw new IllegalArgumentException("No 'didDocument' returned from resolve().");
        if (resolveResult.isErrorResult() && resolveResult.getDidDocument() != null) throw new IllegalArgumentException("Unexpected 'didDocument' despite error returned from resolve().");
        if (resolveResult.getDidDocumentStream() != null) throw new IllegalArgumentException("Unexpected 'didDocumentStream' returned from resolve().");

        if (resolveResult.getContentType() != null) throw new IllegalArgumentException("Unexpected 'contentType' returned from resolve().");

        // create result of resolveRepresentation()

        ResolveResult resolveRepresentationResult = ResolveResult.build();
        resolveRepresentationResult.setDidResolutionMetadata(new HashMap<>(resolveResult.getDidResolutionMetadata()));
        resolveRepresentationResult.setDidDocumentMetadata(new HashMap<>(resolveResult.getDidDocumentMetadata()));

        // convert didDocument to didDocumentStream

        RepresentationProducer representationProducer = null;
        byte[] didDocumentStream;
        String contentType;

        if (!resolveResult.isErrorResult()) {

            DIDDocument didDocument = resolveResult.getDidDocument();

            representationProducer = Representations.getProducer(mediaType);
            if (representationProducer == null) throw new ResolutionException(ResolveResult.makeErrorResolveRepresentationResult(ResolveResult.ERROR_REPRESENTATIONNOTSUPPORTED, "No producer for " + mediaType, mediaType));

            try {
                RepresentationProducer.Result result = representationProducer.produce(didDocument.toMap(), null);
                didDocumentStream = result.representation;
                contentType = result.mediaType;
            } catch (IOException ex) {
                throw new ResolutionException("Problem during production of " + mediaType + ": " + ex.getMessage(), ex);
            }
        } else {

            didDocumentStream = new byte[0];
            contentType = mediaType;
        }

        resolveRepresentationResult.setContentType(contentType);
        resolveRepresentationResult.setDidDocumentStream(didDocumentStream);

        // done

        if (log.isDebugEnabled()) log.debug("Converted to resolveRepresentation() result using " + representationProducer.getClass().getSimpleName() + ": " + resolveRepresentationResult);
        return resolveRepresentationResult;
    }
}
