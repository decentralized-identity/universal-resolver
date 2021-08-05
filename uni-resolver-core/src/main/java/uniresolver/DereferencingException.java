package uniresolver;

import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

public class DereferencingException extends Exception {

	private String error;
	private DereferenceResult dereferenceResult;

	public DereferencingException() {
		super();
	}

	public DereferencingException(String error, String message) {
		super(message);
		this.error = error;
	}

	public DereferencingException(String error, String message, Throwable ex) {
		super(message, ex);
		this.error = error;
	}

	public DereferencingException(String message) {
		this(DereferenceResult.ERROR_INTERNALERROR, message);
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

	public DereferenceResult getDereferenceResult() {
		return dereferenceResult;
	}

	public void setDereferenceResult(DereferenceResult dereferenceResult) {
		this.dereferenceResult = dereferenceResult;
	}
}

