package uniresolver.driver;

import uniresolver.ResolutionException;
import uniresolver.ddo.DDO;

public interface Driver {

	public DDO resolve(String identifier) throws ResolutionException;
}
