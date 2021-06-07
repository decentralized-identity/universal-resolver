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
import java.util.Map;

public class ResolveResultUtil {

    private static final Logger log = LoggerFactory.getLogger(ResolveResultUtil.class);

    public static ResolveResult convertToResolveResult(ResolveResult resolveRepresentationResult) throws ResolutionException {

        if (resolveRepresentationResult.getDidResolutionMetadata() == null) throw new IllegalArgumentException("No 'didResolutionMetadata' returned from resolveRepresentation().");
        if (resolveRepresentationResult.getDidDocumentMetadata() == null) throw new IllegalArgumentException("No 'didDocumentMetadata' returned from resolveRepresentation().");
        if (resolveRepresentationResult.getDidDocument() != null) throw new ResolutionException("Unexpected 'didDocument' returned from resolveRepresentation().");
        if (resolveRepresentationResult.getDidDocumentStream() == null) throw new ResolutionException("No 'didDocumentStream' returned from resolveRepresentation().");

        if (resolveRepresentationResult.getDidResolutionMetadata().get("contentType") == null) throw new ResolutionException("No 'contentType' in 'didResolutionMetadata' returned from resolveRepresentation().");

        String contentType = (String) resolveRepresentationResult.getDidResolutionMetadata().get("contentType");
        byte[] didDocumentStream = resolveRepresentationResult.getDidDocumentStream();

        RepresentationConsumer representationConsumer = Representations.getConsumer(contentType);
        if (representationConsumer == null) throw new ResolutionException(ResolveResult.makeErrorResult(ResolveResult.Error.representationNotSupported));
        if (log.isDebugEnabled()) log.debug("Converting to resolve() result using " + representationConsumer.getClass().getSimpleName() + ": " + resolveRepresentationResult);

        RepresentationConsumer.Result result;
        DIDDocument didDocument;
        try {
            result = representationConsumer.consume(didDocumentStream);
            Map<String, Object> map = new HashMap<>();
            map.putAll(result.didDocument);
            map.putAll(result.representationSpecificEntries.get(contentType));
            didDocument = DIDDocument.fromMap(map);
        } catch (IOException ex) {
            throw new ResolutionException("Problem during consumption of " + contentType + ": " + ex.getMessage(), ex);
        }

        ResolveResult resolveResult = ResolveResult.build();
        resolveResult.setDidResolutionMetadata(new HashMap<>(resolveRepresentationResult.getDidResolutionMetadata()));
        resolveResult.getDidResolutionMetadata().remove("contentType");
        resolveResult.setDidDocument(didDocument);
        resolveResult.setDidDocumentMetadata(new HashMap<>(resolveRepresentationResult.getDidDocumentMetadata()));

        if (log.isDebugEnabled()) log.debug("Converted to resolve() result using " + representationConsumer.getClass().getSimpleName() + ": " + resolveResult);
        return resolveResult;
    }

    public static ResolveResult convertToResolveRepresentationResult(ResolveResult resolveResult, String mediaType) throws ResolutionException {

        if (resolveResult.getDidResolutionMetadata() == null) throw new IllegalArgumentException("No 'didResolutionMetadata' returned from resolve().");
        if (resolveResult.getDidDocumentMetadata() == null) throw new IllegalArgumentException("No 'didDocumentMetadata' returned from resolve().");
        if (resolveResult.getDidDocument() == null) throw new ResolutionException("No 'didDocument' returned from resolve().");
        if (resolveResult.getDidDocumentStream() != null) throw new ResolutionException("Unexpected 'didDocumentStream' returned from resolve().");

        if (resolveResult.getDidResolutionMetadata().get("contentType") != null) throw new ResolutionException("Unexpected 'contentType' returned from resolve().");

        if (mediaType == null) throw new IllegalArgumentException("No 'mediaType' provided.");

        DIDDocument didDocument = resolveResult.getDidDocument();

        RepresentationProducer representationProducer = Representations.getProducer(mediaType);
        if (representationProducer == null) throw new ResolutionException(ResolveResult.makeErrorResult(ResolveResult.Error.representationNotSupported));
        if (log.isDebugEnabled()) log.debug("Converting to resolveRepresentation() result using " + representationProducer.getClass().getSimpleName() + ": " + resolveResult);

        RepresentationProducer.Result result;
        byte[] didDocumentStream;
        String contentType;
        try {
            result = representationProducer.produce(resolveResult.getDidDocument().toMap(), null);
            didDocumentStream = result.representation;
            contentType = result.mediaType;
        } catch (IOException ex) {
            throw new ResolutionException("Problem during production of " + mediaType + ": " + ex.getMessage(), ex);
        }

        ResolveResult resolveRepresentationResult = ResolveResult.build();
        resolveRepresentationResult.setDidResolutionMetadata(new HashMap<>(resolveResult.getDidResolutionMetadata()));
        resolveRepresentationResult.getDidResolutionMetadata().put("contentType", contentType);
        resolveRepresentationResult.setDidDocumentStream(didDocumentStream);
        resolveRepresentationResult.setDidDocumentMetadata(new HashMap<>(resolveResult.getDidDocumentMetadata()));

        if (log.isDebugEnabled()) log.debug("Converted to resolveRepresentation() result using " + representationProducer.getClass().getSimpleName() + ": " + resolveRepresentationResult);
        return resolveRepresentationResult;
    }
}
