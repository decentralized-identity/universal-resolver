## Description

<!-- What does this PR change, and why? -->

## Driver contribution checklist

<!--
Only for PRs that add or update a DID method driver. Delete this section otherwise.
Drivers are contributed ONLY as described in docs/driver-development.md:
https://github.com/decentralized-identity/universal-resolver/blob/main/docs/driver-development.md
-->

- [ ] I have read [`docs/driver-development.md`](https://github.com/decentralized-identity/universal-resolver/blob/main/docs/driver-development.md)
- [ ] This PR changes only `docker-compose.yml`, `uni-resolver-web/src/main/resources/application.yml`, `.env` and `README.md`
- [ ] The driver source code lives in its own public repository under a permissive open-source license
- [ ] The DID method is registered in the [W3C DID Method Registry](https://w3c.github.io/did-spec-registries/#did-methods)
- [ ] The Docker image is publicly available and uses a pinned version (not `:latest`)
- [ ] The test identifiers in `application.yml` resolve with this image
- [ ] Contact information is included in the `README.md` driver table
