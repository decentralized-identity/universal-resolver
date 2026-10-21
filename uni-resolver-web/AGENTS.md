# Not the place for new DID method drivers

`uni-resolver-web/` is part of the Universal Resolver itself. It is **not** a template for adding a DID method driver.

The only file in this module a driver PR may change is `src/main/resources/application.yml`
(adding or updating the driver entry). Do not change anything else here.

Drivers are contributed **only** as described in [`docs/driver-development.md`](../docs/driver-development.md):
a Docker image built from a separate public repository, registered by editing `docker-compose.yml`,
`uni-resolver-web/src/main/resources/application.yml`, `.env` and `README.md`.
See [`AGENTS.md`](../AGENTS.md) for the full rules.
