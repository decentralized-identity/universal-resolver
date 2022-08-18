package uniresolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.result.DereferenceResult;

import java.util.Map;

public class DereferencingException extends Exception {

	/*
	 * From DID Core
	 */

	public static final String ERROR_INVALIDDIDURL = "invalidDidUrl";
	public static final String ERROR_NOTFOUND = "notFound";

	/*
	 * From DID Resolution
	 */

	public static final String ERROR_CONTENTTYEPNOTSUPPORTED = "contentTypeNotSupported";
	public static final String ERROR_INTERNALERROR = "internalError";

	private static final Logger log = LoggerFactory.getLogger(DereferencingException.class);

	private final String error;
	private final Map<String, Object> dereferencingMetadata;

	private final DereferenceResult dereferenceResult;

	public DereferencingException(String error, String message, Map<String, Object> dereferencingMetadata, Throwable ex) {
		super(message, ex);
		this.error = error;
		this.dereferencingMetadata = dereferencingMetadata;
		this.dereferenceResult = null;
	}

	public DereferencingException(String error, String message, Map<String, Object> dereferencingMetadata) {
		super(message);
		this.error = error;
		this.dereferencingMetadata = dereferencingMetadata;
		this.dereferenceResult = null;
	}

	public DereferencingException(String error, String message, Throwable ex) {
		this(error, message, null, ex);
	}

	public DereferencingException(String error, String message) {
		this(error, message, null, null);
	}

	public DereferencingException(String message, Throwable ex) {
		this(ERROR_INTERNALERROR, message, ex);
	}

	public DereferencingException(String message) {
		this(ERROR_INTERNALERROR, message);
	}

	public DereferencingException(DereferenceResult dereferenceResult) {
		super(dereferenceResult.getErrorMessage());
		if (! dereferenceResult.isErrorResult()) throw new IllegalArgumentException("No error result: " + dereferenceResult);
		this.error = dereferenceResult.getError();
		this.dereferencingMetadata = dereferenceResult.getDereferencingMetadata();
		this.dereferenceResult = dereferenceResult;
	}

	/*
	 * Error methods
	 */

	public DereferenceResult toErrorResult(String contentType) {
		if (this.getDereferenceResult() != null) {
			return this.getDereferenceResult();
		} else {
			DereferenceResult dereferenceResult = DereferenceResult.build();
			if (this.getError() != null) dereferenceResult.setError(this.getError());
			if (this.getMessage() != null) dereferenceResult.setErrorMessage(this.getMessage());
			if (this.getDereferencingMetadata() != null) dereferenceResult.getDereferencingMetadata().putAll(this.getDereferencingMetadata());
			dereferenceResult.setContentStream(new byte[0]);
			dereferenceResult.setContentType(contentType);
			if (log.isDebugEnabled()) log.debug("Created error dereference result: " + dereferenceResult);
			return dereferenceResult;
		}
	}

	/*
	 * Getters and setters
	 */

	public String getError() {
		return this.error;
	}

	public Map<String, Object> getDereferencingMetadata() {
		return dereferencingMetadata;
	}

	public DereferenceResult getDereferenceResult() {
		return dereferenceResult;
	}
}

