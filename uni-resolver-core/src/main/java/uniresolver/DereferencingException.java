package uniresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.DereferenceResult;

import java.util.Map;

public class DereferencingException extends Exception {

	public static final String ERROR_INVALID_DID_URL = "INVALID_DID_URL";
	public static final String ERROR_INVALID_OPTIONS = "INVALID_OPTIONS";
	public static final String ERROR_NOT_FOUND = "NOT_FOUND";
	public static final String ERROR_REPRESENTATION_NOT_SUPPORTED = "REPRESENTATION_NOT_SUPPORTED";
	public static final String ERROR_INTERNAL_ERROR = "INTERNAL_ERROR";

	public static final Map<String, String> ERROR_TITLES = Map.of(
			ERROR_INVALID_DID_URL, "Invalid DID URL.",
			ERROR_NOT_FOUND, "The DID URL or DID URL resource was not found.",
			ERROR_REPRESENTATION_NOT_SUPPORTED, "The representation is not supported.",
			ERROR_INTERNAL_ERROR, "An internal error has occurred."
	);

	public static final String DEFAULT_ERROR_TITLE = "DID URL Dereferencing error.";

	private static final Logger log = LoggerFactory.getLogger(DereferencingException.class);

	private final String errorType;
	private final String errorTitle;
	private final Map<String, Object> errorMetadata;
	private final Map<String, Object> dereferencingMetadata;

	private DereferenceResult dereferenceResult;

	public DereferencingException(String errorType, String errorTitle, String errorDetail, Map<String, Object> errorMetadata, Map<String, Object> dereferencingMetadata, Throwable ex) {
		super(errorDetail, ex);
		this.errorType = determineErrorType(errorType);
		this.errorTitle = determineErrorTitle(errorType, errorTitle);
		this.errorMetadata = errorMetadata;
		this.dereferencingMetadata = dereferencingMetadata;
	}

	public DereferencingException(String errorType, String errorTitle, String errorDetail, Map<String, Object> errorMetadata, Map<String, Object> dereferencingMetadata) {
		super(errorDetail);
		this.errorType = determineErrorType(errorType);
		this.errorTitle = determineErrorTitle(errorType, errorTitle);
		this.errorMetadata = errorMetadata;
		this.dereferencingMetadata = dereferencingMetadata;
	}

	public DereferencingException(String errorType, String errorTitle, String errorDetail, Throwable ex) {
		this(errorType, errorTitle, errorDetail, (Map<String, Object>) null, (Map<String, Object>) null, ex);
	}

	public DereferencingException(String errorType, String errorTitle, String errorDetail) {
		this(errorType, errorTitle, errorDetail, (Map<String, Object>) null, (Map<String, Object>) null);
	}

	public DereferencingException(String errorType, String errorDetail, Map<String, Object> errorMetadata, Map<String, Object> dereferencingMetadata, Throwable ex) {
		this(errorType, null, errorDetail, errorMetadata, dereferencingMetadata, ex);
	}

	public DereferencingException(String errorType, String errorDetail, Map<String, Object> errorMetadata, Map<String, Object> dereferencingMetadata) {
		this(errorType, null, errorDetail, errorMetadata, dereferencingMetadata);
	}

	public DereferencingException(String errorType, String errorDetail, Throwable ex) {
		this(errorType, null, errorDetail, (Map<String, Object>) null, (Map<String, Object>) null, ex);
	}

	public DereferencingException(String errorType, String errorDetail) {
		this(errorType, null, errorDetail, (Map<String, Object>) null, (Map<String, Object>) null);
	}

	public DereferencingException(String errorDetail, Throwable ex) {
		this(ERROR_INTERNAL_ERROR, errorDetail, ex);
	}

	public DereferencingException(String errorDetail) {
		this(ERROR_INTERNAL_ERROR, errorDetail);
	}

	public static DereferencingException fromDereferenceResult(DereferenceResult dereferenceResult) {
		if (dereferenceResult != null && dereferenceResult.isErrorResult()) {
			DereferencingException dereferencingException = new DereferencingException(
					dereferenceResult.getErrorType(),
					dereferenceResult.getErrorTitle(),
					dereferenceResult.getErrorDetail(),
					dereferenceResult.getErrorMetadata(),
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
		dereferenceResult.setError(this.getErrorType(), this.getErrorTitle());
		if (this.getErrorDetail() != null) dereferenceResult.setErrorDetail(this.getErrorDetail());
		if (this.getErrorMetadata() != null) dereferenceResult.setErrorMetadata(this.getErrorMetadata());
		dereferenceResult.setContent(null);
		if (this.getDereferencingMetadata() != null) dereferenceResult.getDereferencingMetadata().putAll(this.getDereferencingMetadata());
		if (log.isDebugEnabled()) log.debug("Created error dereference result: " + dereferenceResult);
		return dereferenceResult;
	}

	/*
	 * Helper methods
	 */

	public static String determineErrorType(String errorType) {
		for (String determineErrorType : ERROR_TITLES.keySet()) {
			if (determineErrorType.equalsIgnoreCase(errorType)) return determineErrorType;
			if (determineErrorType.replace("_", "").equalsIgnoreCase(errorType)) return determineErrorType;
		}
		if (errorType != null) return errorType;
		return ERROR_INTERNAL_ERROR;
	}

	public static String determineErrorTitle(String errorType, String errorTitle) {
		if (errorTitle != null) return errorTitle;
		errorTitle = ERROR_TITLES.get(errorType);
		if (errorTitle == null) errorTitle = DEFAULT_ERROR_TITLE;
		return errorTitle;
	}

	public static String determineErrorTitle(String errorType) {
		return determineErrorTitle(errorType, null);
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

	public Map<String, Object> getDereferencingMetadata() {
		return this.dereferencingMetadata;
	}
}
