# run-did-test-suite action

Developed with Node 14.x and without guarantee of backwards compatibility.

## env variables
`MODE`: 
- `server`(default) Run against a did-test-suite server
- `local` Create local files

`DRIVER_STATUS_REPORT`:
- Path to file, generated by [get-driver-status](https://github.com/decentralized-identity/universal-resolver/tree/driver-status-reporting/ci/get-driver-status) action

`OUTPUT_PATH`:
- In `server` mode: Path to write the did-test-suite report
- In `local` mode: Path to write the created files

`HOST`:
- Only for `server` mode and required
- Full url of deployed did-test-suite endpoint e.g. `https://did-test-suite.uniresolver.io/test-suite-manager/generate-report`

`GENERATE_DEFAULT_FILE`:
- Only for `local` mode and optional
- If `true` script automatically creates a `default.js` file with all the resolver related files in the `OUTPUT_PATH`

### Server mode to run against hosted did-test-suite example

```bash
node index.js --DRIVER_STATUS_REPORT=<path-to-folder>/driver-status-2021-05-19_15-08-15-UTC.json --HOST=https://did-test-suite.uniresolver.io/test-suite-manager/generate-report
```

### Local mode to create manual files example

```bash
node index.js --MODE=local --DRIVER_STATUS_REPORT=<path-to-folder>/driver-status-2021-05-19_15-08-15-UTC.json --OUTPUT_PATH=<path-to-output-folder> --GENERATE_DEFAULT_FILE=true
```

### Github action example

**Note:** Env variables for `driver_status_report` and `reports_folder` are set by previous step in this case. See [example](https://github.com/decentralized-identity/universal-resolver/blob/ccbbbf17946ff62b53f647734c382dc460661659/ci/get-driver-status/entrypoint.sh#L43) in `get-driver-status` action.

```yaml
- name: Run did-test-suite
  uses: ./ci/run-did-test-suite
  with:
    host: https://did-test-suite.uniresolver.io/test-suite-manager/generate-report
    driver_status_report: ${{ env.driver_status_report }}
    reports_folder: ${{ env.reports_folder }}
```
