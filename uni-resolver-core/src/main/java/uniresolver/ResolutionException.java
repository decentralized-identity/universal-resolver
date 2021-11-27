package uniresolver;

import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;

import java.util.Map;

public class ResolutionException extends Exception {

	private String error = null;

	private ResolveRepresentationResult resolveRepresentationResult;

	public ResolutionException(String error, String message) {
		super(message);
		this.error = error;
	}

	public ResolutionException(String error, String message, Throwable ex) {
		super(message, ex);
		this.error = error;
	}

	public ResolutionException(String message) {
		this(ResolveResult.ERROR_INTERNALERROR, message);
	}

	public ResolutionException(String message, Throwable ex) {
		this(ResolveResult.ERROR_INTERNALERROR, message, ex);
	}

	public ResolutionException(ResolveRepresentationResult resolveRepresentationResult) {
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

	public ResolveRepresentationResult getResolveRepresentationResult() {
		return resolveRepresentationResult;
	}

	public void setResolveRepresentationResult(ResolveRepresentationResult resolveRepresentationResult) {
		this.resolveRepresentationResult = resolveRepresentationResult;
	}
}
