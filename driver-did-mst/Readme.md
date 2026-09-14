# did:mst Driver

This driver resolves `did:mst` identifiers by reading DID documents from the MST blockchain testnet registry contract.

## Example

```
did:mst:testnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736
```

## Build and Run

This driver is implemented in Node.js.

```bash
docker build -t driver-did-mst .
docker run -p 8080:8080 \
  -e MST_RPC_URL=https://testnetrpc.mstblockchain.com \
  -e MST_REGISTRY_ADDRESS=0xCdceF2336831C45202dCd739BDcF3D0661E342c7 \
  -e MST_CHAIN_ID=4545 \
  driver-did-mst
```

## Docker Image

A prebuilt image is available on Docker Hub:

```
navinjoshimongoosetech/driver-did-mst:latest
```

## Environment Variables

| Variable | Description |
|---|---|
| `MST_RPC_URL` | RPC endpoint for the MST testnet |
| `MST_REGISTRY_ADDRESS` | Address of the DID registry smart contract |
| `MST_CHAIN_ID` | Chain ID of the MST testnet |

## Resolving

```
curl http://localhost:8080/1.0/identifiers/did:mst:testnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736
```

Example response:

```json
{
  "didDocument": {
    "@context": [
      "https://www.w3.org/ns/did/v1",
      "https://w3id.org/security/suites/secp256k1recovery-2020/v2"
    ],
    "id": "did:mst:testnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736",
    "controller": "did:mst:testnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736",
    "verificationMethod": [
      {
        "id": "did:mst:testnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736#controller",
        "type": "EcdsaSecp256k1RecoveryMethod2020",
        "controller": "did:mst:testnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736",
        "blockchainAccountId": "eip155:4545:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736"
      }
    ],
    "authentication": [
      "did:mst:testnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736#controller"
    ],
    "assertionMethod": [
      "did:mst:testnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736#controller"
    ]
  },
  "didResolutionMetadata": {
    "contentType": "application/did+ld+json",
    "network": "testnet",
    "registry": "0xCdceF2336831C45202dCd739BDcF3D0661E342c7"
  },
  "didDocumentMetadata": {
    "lastChangedBlock": "0"
  }
}
```

## Network Support

Currently supports **testnet** only. Mainnet support may be added in a future release.

## Driver Environment Variables

This driver uses the following environment variables in `docker-compose.yml`:

```yaml
driver-did-mst:
  image: navinjoshimongoosetech/driver-did-mst:latest
  ports:
    - "8086:8080"
  environment:
    - MST_RPC_URL=${uniresolver_driver_did_mst_rpc_url}
    - MST_REGISTRY_ADDRESS=${uniresolver_driver_did_mst_registry_address}
    - MST_CHAIN_ID=${uniresolver_driver_did_mst_chain_id}
```
