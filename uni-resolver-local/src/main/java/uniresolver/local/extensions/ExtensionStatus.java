package uniresolver.local.extensions;

public class ExtensionStatus {

	public static final ExtensionStatus DEFAULT = new ExtensionStatus();

	public static final ExtensionStatus SKIP_BEFORE_RESOLVE = new ExtensionStatus(true, false, false, false, false, false);
	public static final ExtensionStatus SKIP_RESOLVE = new ExtensionStatus(false, true, false, false, false, false);
	public static final ExtensionStatus SKIP_AFTER_RESOLVE = new ExtensionStatus(false, false, true, false, false, false);
	public static final ExtensionStatus SKIP_BEFORE_DEREFERENCE = new ExtensionStatus(false, false, false, true, false, false);
	public static final ExtensionStatus SKIP_DEREFERENCE = new ExtensionStatus(false, false, false, false, true, false);
	public static final ExtensionStatus SKIP_AFTER_DEREFERENCE = new ExtensionStatus(false, false, false, false, false, true);

	private boolean skipBeforeResolve;
	private boolean skipResolve;
	private boolean skipAfterResolve;
	private boolean skipBeforeDereference;
	private boolean skipDereference;
	private boolean skipAfterDereference;

	public ExtensionStatus(boolean skipBeforeResolve, boolean skipResolve, boolean skipAfterResolve, boolean skipBeforeDereference, boolean skipDereference, boolean skipAfterDereference) {

		this.skipBeforeResolve = skipBeforeResolve;
		this.skipResolve = skipResolve;
		this.skipAfterResolve = skipAfterResolve;
		this.skipBeforeDereference = skipBeforeDereference;
		this.skipDereference = skipDereference;
		this.skipAfterDereference = skipAfterDereference;
	}

	public ExtensionStatus() {

		this(false, false, false, false, false, false);
	}

	public void or(ExtensionStatus extensionStatus) {

		if (extensionStatus == null) return;

		this.skipBeforeResolve |= extensionStatus.skipBeforeResolve;
		this.skipResolve |= extensionStatus.skipResolve;
		this.skipAfterResolve |= extensionStatus.skipAfterResolve;
		this.skipBeforeDereference |= extensionStatus.skipBeforeDereference;
		this.skipDereference |= extensionStatus.skipDereference;
		this.skipAfterDereference |= extensionStatus.skipAfterDereference;
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

	public boolean skipDereference() {
		return skipDereference;
	}

	public boolean skipAfterDereference() {
		return skipAfterDereference;
	}
}
