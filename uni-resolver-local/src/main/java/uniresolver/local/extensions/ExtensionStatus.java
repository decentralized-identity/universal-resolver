package uniresolver.local.extensions;

public class ExtensionStatus {

	public static final ExtensionStatus DEFAULT = new ExtensionStatus();

	public static final ExtensionStatus SKIP_BEFORE_RESOLVE = new ExtensionStatus(true, false, false, false, false, false, false);
	public static final ExtensionStatus SKIP_RESOLVE = new ExtensionStatus(false, true, false, false, false, false, false);
	public static final ExtensionStatus SKIP_AFTER_RESOLVE = new ExtensionStatus(false, false, true, false, false, false, false);
	public static final ExtensionStatus SKIP_BEFORE_DEREFERENCE = new ExtensionStatus(false, false, false, true, false, false, false);
	public static final ExtensionStatus SKIP_DEREFERENCE_PRIMARY = new ExtensionStatus(false, false, false, false, true, false, false);
	public static final ExtensionStatus SKIP_DEREFERENCE_SECONDARY = new ExtensionStatus(false, false, false, false, false, true, false);
	public static final ExtensionStatus SKIP_AFTER_DEREFERENCE = new ExtensionStatus(false, false, false, false, false, false, true);

	private boolean skipBeforeResolve;
	private boolean skipResolve;
	private boolean skipAfterResolve;
	private boolean skipBeforeDereference;
	private boolean skipDereferencePrimary;
	private boolean skipDereferenceSecondary;
	private boolean skipAfterDereference;

	public ExtensionStatus(boolean skipBeforeResolve, boolean skipResolve, boolean skipAfterResolve, boolean skipBeforeDereference, boolean skipDereferencePrimary, boolean skipDereferenceSecondary, boolean skipAfterDereference) {

		this.skipBeforeResolve = skipBeforeResolve;
		this.skipResolve = skipResolve;
		this.skipAfterResolve = skipAfterResolve;
		this.skipBeforeDereference = skipBeforeDereference;
		this.skipDereferencePrimary = skipDereferencePrimary;
		this.skipDereferenceSecondary = skipDereferenceSecondary;
		this.skipAfterDereference = skipAfterDereference;
	}

	public ExtensionStatus() {

		this(false, false, false, false, false, false, false);
	}

	public void or(ExtensionStatus extensionStatus) {

		if (extensionStatus == null) return;

		this.skipBeforeResolve |= extensionStatus.skipBeforeResolve;
		this.skipResolve |= extensionStatus.skipResolve;
		this.skipAfterResolve |= extensionStatus.skipAfterResolve;
		this.skipBeforeDereference |= extensionStatus.skipBeforeDereference;
		this.skipDereferencePrimary |= extensionStatus.skipDereferencePrimary;
		this.skipDereferenceSecondary |= extensionStatus.skipDereferenceSecondary;
		this.skipAfterDereference |= extensionStatus.skipAfterDereference;
	}

	public boolean skip(String extensionStage) {
		return switch (extensionStage) {
			case "beforeResolve" -> this.skipBeforeResolve;
			case "resolve" -> this.skipResolve;
			case "afterResolve" -> this.skipAfterResolve;
			case "beforeDereference" -> this.skipBeforeDereference;
			case "dereferencePrimary" -> this.skipDereferencePrimary;
			case "dereferenceSecondary" -> this.skipDereferenceSecondary;
			case "afterDereference" -> this.skipAfterDereference;
			default -> throw new IllegalStateException("Unexpected extension stage: " + extensionStage);
		};
	}

	public boolean skipBeforeResolve() {
		return this.skipBeforeResolve;
	}

	public boolean skipResolve() {
		return this.skipResolve;
	}

	public boolean skipAfterResolve() {
		return this.skipAfterResolve;
	}

	public boolean skipBeforeDereference() {
		return skipBeforeDereference;
	}

	public boolean skipDereferencePrimary() {
		return skipDereferencePrimary;
	}

	public boolean skipDereferenceSecondary() {
		return skipDereferenceSecondary;
	}

	public boolean skipAfterDereference() {
		return skipAfterDereference;
	}
}
