package uniresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.ResolveResult;

import java.util.Map;

public class ResolutionException extends Exception {

	public static final String ERROR_INVALID_DID = "INVALID_DID";
	public static final String ERROR_INVALID_OPTIONS = "INVALID_OPTIONS";
	public static final String ERROR_NOT_FOUND = "NOT_FOUND";
	public static final String ERROR_REPRESENTATION_NOT_SUPPORTED = "representationNotSupported";
	public static final String ERROR_METHOD_NOT_SUPPORTED = "methodNotSupported";
	public static final String ERROR_INTERNAL_ERROR = "internalError";

	public static final Map<String, String> ERROR_TITLES = Map.of(
			ERROR_INVALID_DID, "Invalid DID.",
			ERROR_INVALID_OPTIONS, "Invalid DID resolution options.",
			ERROR_NOT_FOUND, "The DID or DID document was not found.",
			ERROR_REPRESENTATION_NOT_SUPPORTED, "The representation is not supported.",
			ERROR_METHOD_NOT_SUPPORTED, "The DID method is not supported.",
			ERROR_INTERNAL_ERROR, "An internall error has occurred."
	);

	private static final Logger log = LoggerFactory.getLogger(ResolutionException.class);

	private final String errorType;
	private final String errorTitle;
	private final Map<String, Object> errorMetadata;

	private ResolveResult resolveResult;

	public ResolutionException(String errorType, String errorTitle, String errorDetail, Map<String, Object> errorMetadata, Throwable ex) {
		super(errorDetail, ex);
		this.errorType = errorType;
		this.errorTitle = errorTitle;
		this.errorMetadata = errorMetadata;
	}

	public ResolutionException(String errorType, String errorTitle, String errorDetail, Map<String, Object> errorMetadata) {
		super(errorDetail);
		this.errorType = errorType;
		this.errorTitle = errorTitle;
		this.errorMetadata = errorMetadata;
	}

	public ResolutionException(String errorType, String errorTitle, String errorDetail, Throwable ex) {
		this(errorType, errorTitle, errorDetail, (Map<String, Object>) null, ex);
	}

	public ResolutionException(String errorType, String errorTitle, String errorDetail) {
		this(errorType, errorTitle, errorDetail, (Map<String, Object>) null);
	}

	public ResolutionException(String errorDetail, Throwable ex) {
		this(ERROR_INTERNAL_ERROR, null, errorDetail, ex);
	}

	public ResolutionException(String errorDetail) {
		this(ERROR_INTERNAL_ERROR, null, errorDetail);
	}

	public static ResolutionException fromResolveResult(ResolveResult resolveResult) {
		if (resolveResult != null && resolveResult.isErrorResult()) {
			ResolutionException resolutionException = new ResolutionException(
					resolveResult.getError(),
					resolveResult.getErrorMessage(),
					resolveResult.getDidResolutionMetadata());
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
		if (this.getErrorMetadata() != null) resolveResult.getDidResolutionMetadata().putAll(this.getErrorMetadata());
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

	public Map<String, Object> getErrorMetadata() {
		return errorMetadata;
	}
}
