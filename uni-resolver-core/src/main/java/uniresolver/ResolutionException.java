package uniresolver;

import uniresolver.result.ResolveResult;

public class ResolutionException extends Exception {

	private ResolveResult resolveResult;

	public ResolutionException() {
		super();
	}

	public ResolutionException(ResolveResult resolveResult) {
		super(resolveResult.getErrorMessage());
		if (!resolveResult.isErrorResult()) throw new IllegalArgumentException("Not an error result: " + resolveResult);
		this.resolveResult = resolveResult;
	}

	public ResolutionException(String message, Throwable ex) {
		super(message, ex);
	}

	public ResolutionException(String message) {
		super(message);
	}

	public ResolutionException(Throwable ex) {
		super(ex);
	}

	/*
	 * Getters and setters
	 */

	public ResolveResult getResolveResult() {
		return this.resolveResult;
	}

	public void setResolveResult(ResolveResult resolveResult) {
		this.resolveResult = resolveResult;
	}
}
