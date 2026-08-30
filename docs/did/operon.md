# did:operon

- **Specification**: https://operon.cloud/did-method/operon
- **Resolver**: https://did.operon.cloud/1.0/identifiers/{did}
- **Driver image**: ghcr.io/operonmaster/driver-did-operon:latest
- **Test identifier**: `did:operon:root`

This driver proxies Universal Resolver requests to the Operon DID resolver and returns DID Documents in `application/did+json` format.
