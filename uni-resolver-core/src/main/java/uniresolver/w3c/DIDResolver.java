package uniresolver.w3c;

import uniresolver.ResolutionException;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveRepresentationResult;

import java.util.Map;

public interface DIDResolver {

	public ResolveDataModelResult resolve(String didString, Map<String, Object> resolutionOptions) throws ResolutionException;
	public ResolveRepresentationResult resolveRepresentation(String didString, Map<String, Object> resolutionOptions) throws ResolutionException;
}
