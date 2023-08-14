package uniresolver.result;

import foundation.identity.did.DIDDocument;
import foundation.identity.did.representations.Representations;
import foundation.identity.did.representations.consumption.RepresentationConsumer;
import foundation.identity.did.representations.production.RepresentationProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

class Conversion {

    private static final Logger log = LoggerFactory.getLogger(Conversion.class);

    static ResolveDataModelResult convertToResolveDataModelResult(ResolveRepresentationResult resolveRepresentationResult) throws ResolutionException {

        if (resolveRepresentationResult == null) throw new NullPointerException();

        // check resolveRepresentation() result

        if (resolveRepresentationResult.getDidResolutionMetadata() == null) throw new IllegalArgumentException("No 'didResolutionMetadata' returned from resolveRepresentation().");
        if (resolveRepresentationResult.getDidDocumentMetadata() == null) throw new IllegalArgumentException("No 'didDocumentMetadata' returned from resolveRepresentation().");
        if (resolveRepresentationResult.getDidDocumentStream() == null) throw new IllegalArgumentException("No 'didDocumentStream' returned from resolveRepresentation().");
        if (resolveRepresentationResult.isErrorResult() && resolveRepresentationResult.getDidDocumentStream().length > 0) throw new IllegalArgumentException("Unexpected 'didDocumentStream' despite error returned from resolveRepresentation().");

        if (resolveRepresentationResult.getContentType() == null) throw new IllegalArgumentException("No 'contentType' in 'didResolutionMetadata' returned from resolveRepresentation().");

        // convert didDocumentStream to didDocument

        RepresentationConsumer representationConsumer = null;
        DIDDocument didDocument;

        if (! resolveRepresentationResult.isErrorResult()) {

            String contentType = resolveRepresentationResult.getContentType();
            byte[] didDocumentStream = resolveRepresentationResult.getDidDocumentStream();

            representationConsumer = Representations.getConsumer(contentType);
            if (representationConsumer == null) throw new ResolutionException(ResolutionException.ERROR_REPRESENTATIONNOTSUPPORTED, "No consumer for " + contentType);

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

        // create resolve() result

        ResolveDataModelResult resolveDataModelResult = ResolveDataModelResult.build();
        resolveDataModelResult.setDidResolutionMetadata(new LinkedHashMap<>(resolveRepresentationResult.getDidResolutionMetadata()));
        resolveDataModelResult.getDidResolutionMetadata().remove("contentType");
        resolveDataModelResult.setDidDocument(didDocument);
        resolveDataModelResult.setDidDocumentMetadata(new LinkedHashMap<>(resolveRepresentationResult.getDidDocumentMetadata()));

        resolveDataModelResult.resolveRepresentationResults.put(resolveRepresentationResult.getContentType(), resolveRepresentationResult);

        // done

        if (log.isTraceEnabled()) log.trace("Converted to resolve() result using consumer " + (representationConsumer == null ? null : representationConsumer.getClass()) + ": " + resolveRepresentationResult + " --------> " + resolveDataModelResult);
        return resolveDataModelResult;
    }

    static ResolveRepresentationResult convertToResolveRepresentationResult(ResolveDataModelResult resolveDataModelResult, String mediaType) throws ResolutionException {

        if (resolveDataModelResult == null) throw new NullPointerException();
        if (mediaType == null) throw new IllegalArgumentException("No 'mediaType' provided.");

        // check resolve() result

        if (resolveDataModelResult.getDidResolutionMetadata() == null) throw new IllegalArgumentException("No 'didResolutionMetadata' returned from resolve().");
        if (resolveDataModelResult.getDidDocumentMetadata() == null) throw new IllegalArgumentException("No 'didDocumentMetadata' returned from resolve().");
        if ((!resolveDataModelResult.isErrorResult()) && resolveDataModelResult.getDidDocument() == null) throw new IllegalArgumentException("No 'didDocument' returned from resolve().");
        if (resolveDataModelResult.isErrorResult() && resolveDataModelResult.getDidDocument() != null) throw new IllegalArgumentException("Unexpected 'didDocument' despite error returned from resolve().");

        // convert didDocument to didDocumentStream

        RepresentationProducer representationProducer = null;
        byte[] didDocumentStream;
        String contentType;

        if (! resolveDataModelResult.isErrorResult()) {

            DIDDocument didDocument = resolveDataModelResult.getDidDocument();

            representationProducer = Representations.getProducer(mediaType);
            if (representationProducer == null) throw new ResolutionException(ResolutionException.ERROR_REPRESENTATIONNOTSUPPORTED, "No producer for " + mediaType);

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

        // create resolveRepresentation() result

        ResolveRepresentationResult resolveRepresentationResult = ResolveRepresentationResult.build();
        resolveRepresentationResult.setDidResolutionMetadata(new LinkedHashMap<>(resolveDataModelResult.getDidResolutionMetadata()));
        resolveRepresentationResult.setContentType(contentType);
        resolveRepresentationResult.setDidDocumentStream(didDocumentStream);
        resolveRepresentationResult.setDidDocumentMetadata(new LinkedHashMap<>(resolveDataModelResult.getDidDocumentMetadata()));

        resolveRepresentationResult.resolveDataModelResult = resolveDataModelResult;

        // done

        if (log.isTraceEnabled()) log.trace("Converted to resolveRepresentation() result using producer " + (representationProducer == null ? null : representationProducer.getClass()) + ": " + resolveDataModelResult + " --------> " + resolveRepresentationResult);
        return resolveRepresentationResult;
    }
}
