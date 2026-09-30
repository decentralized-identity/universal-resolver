# pr-check

Checks the files contributors usually edit when adding a driver for malformed content and non-public images. It is used by the
[Pull Request check](../../.github/workflows/pr-check.yml) workflow, which posts the report as a PR comment
via [pr-check-comment.yml](../../.github/workflows/pr-check-comment.yml).

The files are only parsed, never executed. Images referenced in `docker-compose.yml` are resolved against their
registries (Docker Hub, GitHub Container Registry, Quay, ...) without credentials to make sure everyone can pull them.

| File | Errors (fail the check) | Warnings |
|------|-------------------------|----------|
| Changed files (with `--pr` or `--changed-files`) | Any changed file other than the 4 files below: a driver pull request may only edit these, the driver code belongs in its own repository | |
| `uni-resolver-web/src/main/resources/application.yml` | Invalid YAML, duplicate keys, tab indentation, missing `uniresolver.drivers`, driver without `pattern`/`url`, wrong value types, duplicate patterns, drivers without `testIdentifiers`, services added to `docker-compose.yml` by the pull request without a driver whose `url` points to them (with `--pr` or `--base-compose`; services other services depend on are exempt) | Unknown driver keys (typos), pattern not compilable, test identifiers not matching the pattern |
| `docker-compose.yml` | Invalid YAML, duplicate keys, errors from `docker compose config`, services without `image`/`build`, images that do not exist or need a login to pull | Warnings from `docker compose config` (except the obsolete `version` attribute and unset variables), `${VAR}` not defined in `.env`, driver hosts without a service, images that could not be verified (registry timeout, rate limit, server error) |
| `.env` | Lines not in `KEY=value` format, unterminated quotes | Duplicate keys, leading whitespace |
| `README.md` | Missing `## Drivers` section or driver table, table rows with wrong column count | |

## Run locally with Docker

Build the image the action uses:

    docker build -t pr-check ci/pr-check

Check a pull request, the files are downloaded from GitHub (no checkout needed):

    docker run --rm pr-check --pr 578

Check the files of your local working copy:

    docker run --rm -v "$PWD":/repo pr-check --path /repo

Write the report to a file with `--report`. The path is inside the container, so mount a folder for it:

    docker run --rm -v "$PWD":/out pr-check --pr 578 --report /out/report.md

Options:

| Option | Description |
|--------|-------------|
| `--path DIR` | Repository root containing the files to check (default: `.`) |
| `--pr NUMBER` | Download the files of this pull request's head commit from GitHub and check them (instead of `--path`) |
| `--changed-files FILE` | File listing the files changed by the pull request, one per line (with `--path`; `--pr` gets them from GitHub) |
| `--base-compose FILE` | `docker-compose.yml` of the base branch, to find services added by the pull request (with `--path`; `--pr` downloads it) |
| `--repository OWNER/NAME` | GitHub repository of the pull request (default: `decentralized-identity/universal-resolver`) |
| `--report FILE` | Also write the Markdown report to this file |

The exit code is `0` whether the check passed or failed (see the report heading), `1` for invalid options and `2`
if the pull request could not be downloaded.

## Run locally with Node.js

Needs Node.js 24 (runs the TypeScript sources directly, no build step) and `docker-compose` on the `PATH` for the
compose validation (skipped with a warning if not available).

    cd ci/pr-check
    npm ci
    node src/main.ts --pr 578

## Development

    npm test            # unit tests (node:test)
    npm run typecheck   # tsc, type checking only

| Path | Content |
|------|---------|
| `src/main.ts` | Entry point: parses the arguments, loads the input, runs the checks, writes the report |
| `src/cli.ts` | Command line options |
| `src/config.ts` | Checked files and defaults |
| `src/input/` | Where the checked files come from: a local directory or a pull request on GitHub |
| `src/checks/` | One module per check, `index.ts` runs them all |
| `src/report/` | Findings per file and the Markdown rendering |
| `src/lib/` | YAML parsing, `docker compose config`, container registry and GitHub clients, GitHub Actions outputs |
| `test/` | Unit tests per module |

## Use action in GitHub workflow

    - name: Run PR check
      id: check
      uses: $/ci/pr-check
      with:
        path: .
        changed-files: .pr-check/changed-files.txt        # optional
        base-compose: .pr-check/base/docker-compose.yml   # optional

Outputs: `result` (`success`/`failure`), `errors`, `warnings` and `report` (Markdown). The action always exits
successfully so the report can be published; fail the job based on the `result` output.
