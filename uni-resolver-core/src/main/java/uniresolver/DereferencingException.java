package uniresolver;

import uniresolver.result.DereferenceResult;

import java.util.Map;

public class DereferencingException extends Exception {

	private String error = null;
	private Map<String, Object> dereferencingMetadata = null;

	private DereferenceResult dereferenceResult;

	public DereferencingException() {
		super();
	}

	public DereferencingException(String error, String message) {
		super(message);
		this.error = error;
	}

	public DereferencingException(String error, String message, Map<String, Object> dereferencingMetadata) {
		super(message);
		this.error = error;
		this.dereferencingMetadata = dereferencingMetadata;
	}

	public DereferencingException(String error, String message, Throwable ex) {
		super(message, ex);
		this.error = error;
	}

	public DereferencingException(String error, String message, Map<String, Object> dereferencingMetadata, Throwable ex) {
		super(message, ex);
		this.error = error;
		this.dereferencingMetadata = dereferencingMetadata;
	}

	public DereferencingException(String message) {
		this(DereferenceResult.ERROR_INTERNALERROR, message);
	}

	public DereferencingException(String message, Map<String, Object> dereferencingMetadata) {
		this(DereferenceResult.ERROR_INTERNALERROR, message, dereferencingMetadata);
	}

	public DereferencingException(String message, Throwable ex) {
		this(DereferenceResult.ERROR_INTERNALERROR, message, ex);
	}

	public DereferencingException(DereferenceResult dereferenceResult) {
		this(dereferenceResult.getError(), dereferenceResult.getErrorMessage());
		if (! dereferenceResult.isErrorResult()) throw new IllegalArgumentException("No error result: " + dereferenceResult);
		this.dereferenceResult = dereferenceResult;
	}

	/*
	 * Getters and setters
	 */

	public String getError() {
		return this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Map<String, Object> getDereferencingMetadata() {
		return dereferencingMetadata;
	}

	public void setDereferencingMetadata(Map<String, Object> dereferencingMetadata) {
		this.dereferencingMetadata = dereferencingMetadata;
	}

	public DereferenceResult getDereferenceResult() {
		return dereferenceResult;
	}

	public void setDereferenceResult(DereferenceResult dereferenceResult) {
		this.dereferenceResult = dereferenceResult;
	}
}

