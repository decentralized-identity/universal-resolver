package uniresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;

import java.util.Map;

public class ResolutionException extends Exception {

	/*
	 * From DID Core
	 */

	public static final String ERROR_INVALIDDID = "invalidDid";
	public static final String ERROR_NOTFOUND = "notFound";
	public static final String ERROR_REPRESENTATIONNOTSUPPORTED = "representationNotSupported";

	/*
	 * From DID Resolution
	 */

	public static final String ERROR_METHODNOTSUPPORTED = "methodNotSupported";
	public static final String ERROR_INTERNALERROR = "internalError";

	private static final Logger log = LoggerFactory.getLogger(ResolutionException.class);

	private final String error;
	private final Map<String, Object> didResolutionMetadata;

	private final ResolveResult resolveResult;

	public ResolutionException(String error, String message, Map<String, Object> didResolutionMetadata, Throwable ex) {
		super(message, ex);
		this.error = error;
		this.didResolutionMetadata = didResolutionMetadata;
		this.resolveResult = null;
	}

	public ResolutionException(String error, String message, Map<String, Object> didResolutionMetadata) {
		super(message);
		this.error = error;
		this.didResolutionMetadata = didResolutionMetadata;
		this.resolveResult = null;
	}

	public ResolutionException(String error, String message, Throwable ex) {
		this(error, message, null, ex);
	}

	public ResolutionException(String error, String message) {
		this(error, message, null, null);
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

	public ResolutionException(ResolveResult resolveResult) {
		super(resolveResult.getErrorMessage());
		if (! resolveResult.isErrorResult()) throw new IllegalArgumentException("No error result: " + resolveResult);
		this.error = resolveResult.getError();
		this.didResolutionMetadata = resolveResult.getDidResolutionMetadata();
		this.resolveResult = resolveResult;
	}

	/*
	 * Error methods
	 */

	public ResolveDataModelResult toErrorResult() {
		if (this.getResolveResult() != null) {
			try {
				return this.getResolveResult().toResolveDataModelResult();
			} catch (ResolutionException ex) {
				throw new IllegalStateException(ex.getMessage(), ex);
			}
		} else {
			ResolveDataModelResult resolveDataModelResult = ResolveDataModelResult.build();
			if (this.getError() != null) resolveDataModelResult.setError(this.getError());
			if (this.getMessage() != null) resolveDataModelResult.setErrorMessage(this.getMessage());
			if (this.getDidResolutionMetadata() != null) resolveDataModelResult.getDidResolutionMetadata().putAll(this.getDidResolutionMetadata());
			resolveDataModelResult.setDidDocument(null);
			if (log.isDebugEnabled()) log.debug("Created error resolve result: " + resolveDataModelResult);
			return resolveDataModelResult;
		}
	}

	public ResolveRepresentationResult toErrorResult(String contentType) {
		if (this.getResolveResult() != null) {
			try {
				return this.getResolveResult().toResolveRepresentationResult(contentType);
			} catch (ResolutionException ex) {
				throw new IllegalStateException(ex.getMessage(), ex);
			}
		} else {
			ResolveRepresentationResult resolveRepresentationResult = ResolveRepresentationResult.build();
			if (this.getError() != null) resolveRepresentationResult.setError(this.getError());
			if (this.getMessage() != null) resolveRepresentationResult.setErrorMessage(this.getMessage());
			if (this.getDidResolutionMetadata() != null) resolveRepresentationResult.setDidResolutionMetadata(this.getDidResolutionMetadata());
			resolveRepresentationResult.setDidDocumentStream(new byte[0]);
			resolveRepresentationResult.setContentType(contentType);
			if (log.isDebugEnabled()) log.debug("Created error resolve result: " + resolveRepresentationResult);
			return resolveRepresentationResult;
		}
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

	public ResolveResult getResolveResult() {
		return resolveResult;
	}
}
