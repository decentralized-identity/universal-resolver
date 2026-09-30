# pr-check

Checks the files contributors usually edit when adding a driver for malformed content and non-public images. It is used by the
[Pull Request check](../../.github/workflows/pr-check.yml) workflow, which posts the report as a PR comment
via [pr-check-comment.yml](../../.github/workflows/pr-check-comment.yml).

The files are only parsed, never executed. Images referenced in `docker-compose.yml` are resolved against their
registries (Docker Hub, GitHub Container Registry, Quay, ...) without credentials to make sure everyone can pull them.

| File | Errors (fail the check) | Warnings |
|------|-------------------------|----------|
| `uni-resolver-web/src/main/resources/application.yml` | Invalid YAML, duplicate keys, tab indentation, missing `uniresolver.drivers`, driver without `pattern`/`url`, wrong value types, duplicate patterns | Unknown driver keys (typos), pattern not compilable, test identifiers not matching the pattern |
| `docker-compose.yml` | Invalid YAML, duplicate keys, errors from `docker compose config`, services without `image`/`build`, images that do not exist or need a login to pull | Warnings from `docker compose config` (except the obsolete `version` attribute), `${VAR}` not defined in `.env`, driver hosts without a service, images that could not be verified (registry timeout, rate limit, server error) |
| `.env` | Lines not in `KEY=value` format, unterminated quotes | Duplicate keys, leading whitespace |
| `README.md` | Missing `## Drivers` section or driver table, table rows with wrong column count | |

## Run locally with Docker

    docker build -t pr-check ci/pr-check
    docker run --rm -v "$PWD":/repo pr-check --path /repo

## Run locally with Python

Needs Python 3 with the dependencies from `app/requirements.txt` and `docker compose` for the compose validation
(skipped with a warning if not available).

    pip install -r ci/pr-check/app/requirements.txt
    python ci/pr-check/app/pr_check.py --path . --report report.md

## Use action in GitHub workflow

    - name: Run PR check
      id: check
      uses: $/ci/pr-check
      with:
        path: .

Outputs: `result` (`success`/`failure`), `errors`, `warnings` and `report` (Markdown). The action always exits
successfully so the report can be published; fail the job based on the `result` output.
