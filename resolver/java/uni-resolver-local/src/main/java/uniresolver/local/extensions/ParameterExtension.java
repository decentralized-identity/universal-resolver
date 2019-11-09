package uniresolver.local.extensions;

public interface ParameterExtension extends Extension {

	public String[] handlesParameters();

	public abstract static class AbstractParameterExtension extends AbstractExtension implements ParameterExtension {

		@Override
		public String[] handlesParameters() {

			return new String[0];
		}
	}
}
