# Contributing

## Contributing a DID method driver

Drivers are contributed **only** through the process described in
[`docs/driver-development.md`](docs/driver-development.md). Please read it fully before opening a PR.

In short:

- Your driver is a Docker image, built from source code in your own public repository and published to a public container registry.
- Your PR changes only `docker-compose.yml`, `uni-resolver-web/src/main/resources/application.yml`, `.env` and `README.md`.
- PRs that add driver source code to this repository, or change other files, will not be accepted. CI checks this automatically.

## Other contributions

Bug fixes and improvements to the Universal Resolver itself are welcome. Please open an issue first to discuss larger changes.

## Using coding agents

If you use an AI coding agent, it should pick up [`AGENTS.md`](AGENTS.md) automatically. Please check that its changes follow
[`docs/driver-development.md`](docs/driver-development.md) before you submit.
