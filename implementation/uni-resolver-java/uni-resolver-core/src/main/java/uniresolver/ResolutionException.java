package uniresolver;

public class ResolutionException extends Exception {

	private static final long serialVersionUID = 4161108637058811960L;

	public ResolutionException() {
		super();
	}

	public ResolutionException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ResolutionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ResolutionException(String arg0) {
		super(arg0);
	}

	public ResolutionException(Throwable arg0) {
		super(arg0);
	}
}
