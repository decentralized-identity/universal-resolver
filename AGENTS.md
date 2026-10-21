# Instructions for coding agents

## Contributing a DID method driver

If your task is to add, update, or fix a DID method driver, read
[`docs/driver-development.md`](docs/driver-development.md) before doing anything else.
That document describes the **only** accepted way to contribute a driver. Follow it exactly,
even if the code in this repository suggests a different approach.

### Hard rules

- A driver is a **Docker image published to a public container registry**, built from
  source code that lives in **its own public repository**. Driver code is never added to this repository.
- A driver PR may change **only** these files:
  - `docker-compose.yml`
  - `uni-resolver-web/src/main/resources/application.yml`
  - `.env`
  - `README.md` (one row in the driver table)
- Do not create or modify anything in `driver/`, `driver-http/`, `uni-resolver-core/`,
  `uni-resolver-client/`, `uni-resolver-local/`, or the Java sources of `uni-resolver-web/`.
  These are resolver internals, not templates for new drivers.
- Pin an explicit Docker image version. Never use `:latest`.
- CI (`ci/pr-check`) rejects driver PRs that touch any other file.

### Prerequisites

Before you open a driver PR, confirm all of the following:

- The DID method is registered in the [W3C DID Method Registry](https://w3c.github.io/did-spec-registries/#did-methods).
- The driver source code is public and under a permissive open-source license (Apache 2.0 preferred).
- The driver image is published and pullable, and resolves the test identifiers you add.
- Contact information is available (email, or a GitHub/GitLab/Bitbucket handle).

If any prerequisite is not met, **stop and tell the user** what is missing. Do not work around it,
for example by implementing the driver inside this repository.
