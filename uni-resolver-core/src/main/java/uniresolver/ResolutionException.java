package uniresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;

import java.util.Map;

public class ResolutionException extends Exception {

	public static final String ERROR_INVALIDDID = "invalidDid";
	public static final String ERROR_NOTFOUND = "notFound";
	public static final String ERROR_REPRESENTATIONNOTSUPPORTED = "representationNotSupported";
	public static final String ERROR_METHODNOTSUPPORTED = "methodNotSupported";
	public static final String ERROR_INTERNALERROR = "internalError";

	private static final Logger log = LoggerFactory.getLogger(ResolutionException.class);

	private final String error;
	private final Map<String, Object> didResolutionMetadata;

	private ResolveResult resolveResult;

	public ResolutionException(String error, String message, Map<String, Object> didResolutionMetadata, Throwable ex) {
		super(message, ex);
		this.error = error;
		this.didResolutionMetadata = didResolutionMetadata;
	}

	public ResolutionException(String error, String message, Map<String, Object> didResolutionMetadata) {
		super(message);
		this.error = error;
		this.didResolutionMetadata = didResolutionMetadata;
	}

	public ResolutionException(String error, String message, Throwable ex) {
		this(error, message, (Map<String, Object>) null, ex);
	}

	public ResolutionException(String error, String message) {
		this(error, message, (Map<String, Object>) null);
	}

	public ResolutionException(String message, Throwable ex) {
		this(ERROR_INTERNALERROR, message, ex);
	}

	public ResolutionException(String message) {
		this(ERROR_INTERNALERROR, message);
	}

	public ResolutionException(Throwable ex) {
		this(ex.getMessage(), ex);
	}

	public static ResolutionException fromResolveResult(ResolveResult resolveResult) {
		if (resolveResult != null && resolveResult.isErrorResult()) {
			ResolutionException resolutionException = new ResolutionException(resolveResult.getError(), resolveResult.getErrorMessage(), resolveResult.getDidResolutionMetadata());
			resolutionException.resolveResult = resolveResult;
			return resolutionException;
		} else {
			throw new IllegalArgumentException("No error result: " + resolveResult);
		}
	}

	/*
	 * Error methods
	 */

	public ResolveDataModelResult toErrorResolveDataModelResult() {
		if (this.resolveResult != null) {
            try {
                return this.resolveResult.toResolveDataModelResult();
			} catch (ResolutionException ex) {
				throw new IllegalStateException(ex.getMessage(), ex);
            }
        }
		ResolveDataModelResult resolveDataModelResult = ResolveDataModelResult.build();
		if (this.getError() != null) resolveDataModelResult.setError(this.getError());
		if (this.getMessage() != null) resolveDataModelResult.setErrorMessage(this.getMessage());
		if (this.getDidResolutionMetadata() != null) resolveDataModelResult.getDidResolutionMetadata().putAll(this.getDidResolutionMetadata());
		resolveDataModelResult.setDidDocument(null);
		if (log.isDebugEnabled()) log.debug("Created error resolve result: " + resolveDataModelResult);
		return resolveDataModelResult;
	}

	public ResolveRepresentationResult toErrorResolveRepresentationResult(String contentType) {
		if (this.resolveResult != null) {
            try {
                return this.resolveResult.toResolveRepresentationResult(contentType);
            } catch (ResolutionException ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        }
		ResolveRepresentationResult resolveRepresentationResult = ResolveRepresentationResult.build();
		if (this.getError() != null) resolveRepresentationResult.setError(this.getError());
		if (this.getMessage() != null) resolveRepresentationResult.setErrorMessage(this.getMessage());
		if (this.getDidResolutionMetadata() != null) resolveRepresentationResult.getDidResolutionMetadata().putAll(this.getDidResolutionMetadata());
		resolveRepresentationResult.setDidDocumentStream(new byte[0]);
		resolveRepresentationResult.setContentType(contentType);
		if (log.isDebugEnabled()) log.debug("Created error resolve representation result: " + resolveRepresentationResult);
		return resolveRepresentationResult;
	}

	/*
	 * Getters and setters
	 */

	public String getError() {
		return error;
	}

	public Map<String, Object> getDidResolutionMetadata() {
		return didResolutionMetadata;
	}
}
