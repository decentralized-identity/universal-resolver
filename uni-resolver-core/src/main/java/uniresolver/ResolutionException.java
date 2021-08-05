package uniresolver;

import uniresolver.result.DereferenceResult;
import uniresolver.result.ResolveResult;

public class ResolutionException extends Exception {

	private String error;
	private ResolveResult resolveRepresentationResult;

	public ResolutionException(String error, String message) {
		super(message);
		this.error = error;
	}

	public ResolutionException(String error, String message, Throwable ex) {
		super(message, ex);
		this.error = error;
	}

	public ResolutionException(String message) {
		this(DereferenceResult.ERROR_INTERNALERROR, message);
	}

	public ResolutionException(String message, Throwable ex) {
		this(DereferenceResult.ERROR_INTERNALERROR, message, ex);
	}

	public ResolutionException(ResolveResult resolveRepresentationResult) {
		this(resolveRepresentationResult.getError(), resolveRepresentationResult.getErrorMessage());
		if (! resolveRepresentationResult.isErrorResult()) throw new IllegalArgumentException("No error result: " + resolveRepresentationResult);
		this.resolveRepresentationResult = resolveRepresentationResult;
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

	public ResolveResult getResolveRepresentationResult() {
		return resolveRepresentationResult;
	}

	public void setResolveRepresentationResult(ResolveResult resolveRepresentationResult) {
		this.resolveRepresentationResult = resolveRepresentationResult;
	}
}
