package uniresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.DereferenceResult;

import java.util.Map;

public class DereferencingException extends Exception {

	public static final String ERROR_INVALID_DID_URL = "INVALID_DID_URL";
	public static final String ERROR_NOT_FOUND = "NOT_FOUND";
	public static final String ERROR_CONTENT_TYPE_NOT_SUPPORTED = "CONTENT_TYPE_NOT_SUPPORTED";
	public static final String ERROR_INTERNAL_ERROR = "INTERNAL_ERROR";

	public static final Map<String, String> ERROR_TITLES = Map.of(
			ERROR_INVALID_DID_URL, "Invalid DID URL.",
			ERROR_NOT_FOUND, "The DID URL or DID URL resource was not found.",
			ERROR_CONTENT_TYPE_NOT_SUPPORTED, "The content type is not supported.",
			ERROR_INTERNAL_ERROR, "An internal error has occurred."
	);

	private static final Logger log = LoggerFactory.getLogger(DereferencingException.class);

	private final String errorType;
	private final String errorTitle;
	private final Map<String, Object> errorMetadata;

	private DereferenceResult dereferenceResult;

	public DereferencingException(String errorType, String errorTitle, String errorDetail, Map<String, Object> errorMetadata, Throwable ex) {
		super(errorDetail, ex);
		this.errorType = errorType;
		this.errorTitle = errorTitle;
		this.errorMetadata = errorMetadata;
	}

	public DereferencingException(String errorType, String errorTitle, String errorDetail, Map<String, Object> errorMetadata) {
		super(errorDetail);
		this.errorType = errorType;
		this.errorTitle = errorTitle;
		this.errorMetadata = errorMetadata;
	}

	public DereferencingException(String errorType, String errorTitle, String errorDetail, Throwable ex) {
		this(errorType, errorTitle, errorDetail, (Map<String, Object>) null, ex);
	}

	public DereferencingException(String errorType, String errorTitle, String errorDetail) {
		this(errorType, errorTitle, errorDetail, (Map<String, Object>) null);
	}

	public DereferencingException(String errorDetail, Throwable ex) {
		this(ERROR_INTERNAL_ERROR, null, errorDetail, ex);
	}

	public DereferencingException(String errorDetail) {
		this(ERROR_INTERNAL_ERROR, null, errorDetail);
	}

	public static DereferencingException fromDereferenceResult(DereferenceResult dereferenceResult) {
		if (dereferenceResult != null && dereferenceResult.isErrorResult()) {
			DereferencingException dereferencingException = new DereferencingException(
					dereferenceResult.getError(),
					dereferenceResult.getErrorMessage(),
					dereferenceResult.getDereferencingMetadata());
			dereferencingException.dereferenceResult = dereferenceResult;
			return dereferencingException;
		} else {
			throw new IllegalArgumentException("No error result: " + dereferenceResult);
		}
	}

	/*
	 * Error methods
	 */

	public DereferenceResult toErrorDereferenceResult() {
		if (this.dereferenceResult != null) return this.dereferenceResult;
		DereferenceResult dereferenceResult = DereferenceResult.build();
		if (this.getErrorType() != null) dereferenceResult.setError(this.getErrorType());
		if (this.getMessage() != null) dereferenceResult.setErrorMessage(this.getMessage());
		if (this.getErrorMetadata() != null) dereferenceResult.getDereferencingMetadata().putAll(this.getErrorMetadata());
		dereferenceResult.setContent(null);
		if (log.isDebugEnabled()) log.debug("Created error dereference result: " + dereferenceResult);
		return dereferenceResult;
	}

	/*
	 * Getters and setters
	 */

	public String getErrorType() {
		return this.errorType;
	}

	public Map<String, Object> getErrorMetadata() {
		return errorMetadata;
	}
}

