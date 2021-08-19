package uniresolver;

import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;

import java.util.Map;

public class ResolutionException extends Exception {

	private String error = null;
	private Map<String, Object> didResolutionMetadata = null;

	private ResolveRepresentationResult resolveRepresentationResult;

	public ResolutionException(String error, String message) {
		super(message);
		this.error = error;
	}

	public ResolutionException(String error, String message, Map<String, Object> didResolutionMetadata) {
		super(message);
		this.error = error;
		this.didResolutionMetadata = didResolutionMetadata;
	}

	public ResolutionException(String error, String message, Throwable ex) {
		super(message, ex);
		this.error = error;
	}

	public ResolutionException(String error, String message, Map<String, Object> didResolutionMetadata, Throwable ex) {
		super(message, ex);
		this.error = error;
		this.didResolutionMetadata = didResolutionMetadata;
	}

	public ResolutionException(String message) {
		this(ResolveResult.ERROR_INTERNALERROR, message);
	}

	public ResolutionException(String message, Map<String, Object> didResolutionMetadata) {
		this(ResolveResult.ERROR_INTERNALERROR, message, didResolutionMetadata);
	}

	public ResolutionException(String message, Throwable ex) {
		this(ResolveResult.ERROR_INTERNALERROR, message, ex);
	}

	public ResolutionException(String message, Map<String, Object> didResolutionMetadata, Throwable ex) {
		this(ResolveResult.ERROR_INTERNALERROR, message, didResolutionMetadata, ex);
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

	public Map<String, Object> getDidResolutionMetadata() {
		return didResolutionMetadata;
	}

	public void setDidResolutionMetadata(Map<String, Object> didResolutionMetadata) {
		this.didResolutionMetadata = didResolutionMetadata;
	}

	public ResolveRepresentationResult getResolveRepresentationResult() {
		return resolveRepresentationResult;
	}

	public void setResolveRepresentationResult(ResolveRepresentationResult resolveRepresentationResult) {
		this.resolveRepresentationResult = resolveRepresentationResult;
	}
}
