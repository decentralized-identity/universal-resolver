# DIDLint Entry Disable Reports

The nightly DIDLint workflow writes `result.json` to the `did-lint-reports` branch. It also computes report-only entry disable artifacts from that DIDLint output:

- `entry-disable-state.json` tracks consecutive unresponsive runs by `application.yml` driver entry id.
- `entry-disable-report.json` explains the latest per-entry decision.
- `disabled-entries.txt` contains the comma-separated entry ids that would be disabled after the configured threshold.

Only DIDLint `no response` results count as failures for auto-disable purposes. DIDLint `compliant`, `partially compliant`, and `non-compliant` statuses are treated as responding, because they indicate the driver answered even if the returned DID document has compliance issues.

This workflow is intentionally report-only. It does not update `UNIRESOLVER_DISABLED_ENTRIES` or trigger a Kubernetes deployment.
