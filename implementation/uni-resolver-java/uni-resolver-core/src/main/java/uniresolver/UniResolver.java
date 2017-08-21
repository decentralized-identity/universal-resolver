package uniresolver;

import uniresolver.ddo.DDO;

public interface UniResolver {

	public DDO resolve(String identifier) throws ResolutionException;
}
