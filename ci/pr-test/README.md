# pr-test

Tests the drivers a pull request changes: starts them together with `uni-resolver-web` and resolves their test
identifiers. It is used by the [Pull Request test](../../.github/workflows/pr-test.yml) workflow, which posts the
report as a PR comment via [pr-test-comment.yml](../../.github/workflows/pr-test-comment.yml).

## What is tested

The pull request's files are compared with the commit it branched from (the merge base, as in GitHub's
"Files changed"), so changes on the base branch since then don't count:

| Change | Tested |
|--------|--------|
| A driver entry in `application.yml` is new or changed (e.g. `testIdentifiers` or `url`) | That driver |
| A service in `docker-compose.yml` is new or changed (e.g. image tag or variable names; also changed values in `.env`) | All drivers whose `url` points to that service |

Only `uni-resolver-web` and the services of the tested drivers (with their `depends_on`) are started.
`uni-resolver-web` runs with the pull request's `application.yml`, so new drivers are known to it. A driver whose
`url` points to an external host is tested without starting a container.

## How it runs

1. Pull and start the containers, then wait 10 s. A driver counts as started if its container is running, even if
   the application inside logs errors. The complete startup log of each driver is added to the report.
2. Resolve each test identifier with `uni-resolver-web` (timeout 120 s). A response with a DID document that has an
   `id` is a success; the DID document isn't validated against the spec.
3. For a failed test identifier, add the log lines of the driver and of `uni-resolver-web` written during that
   request, reduced to warnings, errors and exception messages (no stack traces), at most 20 lines per container.
4. Remove the containers, network and volumes.

The run fails if any container doesn't start or any test identifier fails.

The services come from an untrusted pull request, so services with host access (`privileged`, host namespaces,
`cap_add`, `devices`, bind mounts) are not started.

## Run locally with Docker

Build the image (the GitHub Action runs the sources directly on the runner, the image is for local runs):

    docker build -t pr-test ci/pr-test

Test a pull request, the files of the pull request and its base branch are downloaded from GitHub. The container
starts the test containers on your Docker daemon, so it needs the Docker socket:

    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock pr-test --pr 578

Drivers only need to provide `linux/amd64` images, the platform of the GitHub runners. On other machines, e.g. a
Mac with Apple silicon, an image without a variant for the machine's platform runs as `linux/amd64` through
emulation (Docker Desktop does this automatically when the platform is requested); images with a native variant run
natively. The test logs which images run emulated.

Write the report to a file with `--report`; the path is inside the container, so mount a folder for it:

    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v "$PWD":/out pr-test --pr 578 --report /out/report.md

Options:

| Option | Description |
|--------|-------------|
| `--pr NUMBER` | Download the files of this pull request and its base branch from GitHub |
| `--repository OWNER/NAME` | GitHub repository of the pull request (default: `decentralized-identity/universal-resolver`) |
| `--path DIR` | Directory with `application.yml`, `docker-compose.yml` and `.env` of the pull request, at their repository paths (instead of `--pr`) |
| `--base-path DIR` | Directory with the same files on the base branch (with `--path`) |
| `--report FILE` | Also write the Markdown report to this file |
| `--startup-wait SECONDS` | Time the containers get to start (default: 10) |
| `--timeout SECONDS` | Timeout per test identifier (default: 120) |

The progress and the complete startup logs are written to stderr, the report to stdout. The exit code is `0` when
the test ran, whether it passed or failed (see the report heading), `1` for invalid options and `2` if the pull
request could not be downloaded or the test could not run.

## Run locally with Node.js

Needs Node.js 24 and Docker with docker compose.

    cd ci/pr-test
    npm ci
    node src/main.ts --pr 578

## Use action in GitHub workflow

    - name: Run PR test
      id: test
      uses: $/ci/pr-test
      with:
        path: .pr-test/head          # files of the pull request
        base-path: .pr-test/base     # files of the base branch

Outputs: `result` (`success`/`failure`), `failed` (number of failed test identifiers) and `report` (Markdown). The
action always exits successfully so the report can be published; fail the job based on the `result` output.

## Development

    npm test            # unit tests (node:test)
    npm run typecheck   # tsc, type checking only

| Path | Content |
|------|---------|
| `src/main.ts` | Entry point: parses the arguments, loads the input, runs the test, writes the report |
| `src/run.ts` | The test run: plan, start, wait, resolve, clean up |
| `src/cli.ts`, `src/config.ts` | Command line options; files, timeouts and limits |
| `src/input/` | Where the files come from: local directories or a pull request on GitHub |
| `src/plan/` | Which drivers changed and what to start |
| `src/docker/` | docker compose project, test override, safety check, platform fallback, running in a container |
| `src/resolver/` | Client for `uni-resolver-web` and the success criterion |
| `src/logs/` | Reducing a request's log to the relevant lines |
| `src/report/` | Test results and the Markdown rendering |
| `test/` | Unit tests per module |
