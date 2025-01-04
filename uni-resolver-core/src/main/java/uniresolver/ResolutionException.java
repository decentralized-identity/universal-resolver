package uniresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	public ResolveResult toErrorResolveResult() {
		if (this.resolveResult != null) return this.resolveResult;
		ResolveResult resolveResult = ResolveResult.build();
		if (this.getError() != null) resolveResult.setError(this.getError());
		if (this.getMessage() != null) resolveResult.setErrorMessage(this.getMessage());
		if (this.getDidResolutionMetadata() != null) resolveResult.getDidResolutionMetadata().putAll(this.getDidResolutionMetadata());
		resolveResult.setDidDocument(null);
		if (log.isDebugEnabled()) log.debug("Created error resolve result: " + resolveResult);
		return resolveResult;
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
