package uniresolver;

import uniresolver.result.DereferenceResult;

public class DereferencingException extends Exception {

	private DereferenceResult dereferenceResult;

	public DereferencingException() {
		super();
	}

	public DereferencingException(DereferenceResult dereferenceResult) {
		super(dereferenceResult.getErrorMessage());
		if (!dereferenceResult.isErrorDereferenceResult()) throw new IllegalArgumentException("Not an error result: " + dereferenceResult);
		this.dereferenceResult = dereferenceResult;
	}

	public DereferencingException(String message, Throwable ex) {
		super(message, ex);
	}

	public DereferencingException(String message) {
		super(message);
	}

	public DereferencingException(Throwable ex) {
		super(ex);
	}
}
