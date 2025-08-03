package uniresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.ResolveResult;

import java.util.Map;

public class ResolutionException extends Exception {

	public static final String ERROR_INVALID_DID = "INVALID_DID";
	public static final String ERROR_INVALID_DID_DOCUMENT = "INVALID_DID_DOCUMENT";
	public static final String ERROR_INVALID_OPTIONS = "INVALID_OPTIONS";
	public static final String ERROR_NOT_FOUND = "NOT_FOUND";
	public static final String ERROR_METHOD_NOT_SUPPORTED = "METHOD_NOT_SUPPORTED";
	public static final String ERROR_REPRESENTATION_NOT_SUPPORTED = "REPRESENTATION_NOT_SUPPORTED";
	public static final String ERROR_INTERNAL_ERROR = "INTERNAL_ERROR";

	public static final Map<String, String> ERROR_TITLES = Map.of(
			ERROR_INVALID_DID, "Invalid DID.",
			ERROR_INVALID_DID_DOCUMENT, "Invalid DID document.",
			ERROR_INVALID_OPTIONS, "Invalid DID resolution options.",
			ERROR_NOT_FOUND, "The DID or DID document was not found.",
			ERROR_METHOD_NOT_SUPPORTED, "The DID method is not supported.",
			ERROR_REPRESENTATION_NOT_SUPPORTED, "The representation is not supported.",
			ERROR_INTERNAL_ERROR, "An internall error has occurred."
	);

	public static final String DEFAULT_ERROR_TITLE = "DID Resolution error.";

	private static final Logger log = LoggerFactory.getLogger(ResolutionException.class);

	private final String errorType;
	private final String errorTitle;
	private final Map<String, Object> errorMetadata;

	private ResolveResult resolveResult;

	public ResolutionException(String errorType, String errorTitle, String errorDetail, Map<String, Object> errorMetadata, Throwable ex) {
		super(errorDetail, ex);
		this.errorType = errorType(errorType);
		this.errorTitle = errorTitle(errorType, errorTitle);
		this.errorMetadata = errorMetadata;
	}

	public ResolutionException(String errorType, String errorTitle, String errorDetail, Map<String, Object> errorMetadata) {
		super(errorDetail);
		this.errorType = errorType(errorType);
		this.errorTitle = errorTitle(errorType, errorTitle);
		this.errorMetadata = errorMetadata;
	}

	public ResolutionException(String errorType, String errorTitle, String errorDetail, Throwable ex) {
		this(errorType, errorTitle, errorDetail, (Map<String, Object>) null, ex);
	}

	public ResolutionException(String errorType, String errorTitle, String errorDetail) {
		this(errorType, errorTitle, errorDetail, (Map<String, Object>) null);
	}

	public ResolutionException(String errorType, String errorDetail, Map<String, Object> errorMetadata, Throwable ex) {
		this(errorType, null, errorDetail, errorMetadata, ex);
	}

	public ResolutionException(String errorType, String errorDetail, Map<String, Object> errorMetadata) {
		this(errorType, null, errorDetail, errorMetadata);
	}

	public ResolutionException(String errorType, String errorDetail, Throwable ex) {
		this(errorType, null, errorDetail, (Map<String, Object>) null, ex);
	}

	public ResolutionException(String errorType, String errorDetail) {
		this(errorType, null, errorDetail, (Map<String, Object>) null);
	}

	public ResolutionException(String errorDetail, Throwable ex) {
		this(ERROR_INTERNAL_ERROR, errorDetail, ex);
	}

	public ResolutionException(String errorDetail) {
		this(ERROR_INTERNAL_ERROR, errorDetail);
	}

	public static ResolutionException fromResolveResult(ResolveResult resolveResult) {
		if (resolveResult != null && resolveResult.isErrorResult()) {
			ResolutionException resolutionException = new ResolutionException(
					resolveResult.getErrorType(),
					resolveResult.getErrorTitle(),
					resolveResult.getErrorDetail(),
					resolveResult.getErrorMetadata());
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
		resolveResult.setError(this.getErrorType(), this.getErrorTitle());
		if (this.getErrorDetail() != null) resolveResult.setErrorDetail(this.getErrorDetail());
		if (this.getErrorMetadata() != null) resolveResult.setErrorMetadata(this.getErrorMetadata());
		resolveResult.setDidDocument(null);
		if (log.isDebugEnabled()) log.debug("Created error resolve result: " + resolveResult);
		return resolveResult;
	}

	/*
	 * Helper methods
	 */

	private static String errorType(String errorType) {
		if (errorType != null) return errorType;
		return ERROR_INTERNAL_ERROR;
	}

	private static String errorTitle(String errorType, String errorTitle) {
		if (errorTitle != null) return errorTitle;
		errorTitle = ERROR_TITLES.get(errorType);
		if (errorTitle == null) errorTitle = DEFAULT_ERROR_TITLE;
		return errorTitle;
	}

	/*
	 * Getters and setters
	 */

	public String getErrorType() {
		return this.errorType;
	}

	public String getErrorTitle() {
		return this.errorTitle;
	}

	public String getErrorDetail() {
		return this.getMessage();
	}

	public Map<String, Object> getErrorMetadata() {
		return this.errorMetadata;
	}
}
