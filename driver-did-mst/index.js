const express = require('express');
const { ethers } = require('ethers');

const app = express();
const PORT = process.env.PORT || 8080;

// MST Testnet config
const MST_TESTNET_RPC = process.env.MST_RPC_URL; // set to actual MST testnet RPC endpoint
const REGISTRY_CONTRACT_ADDRESS = process.env.MST_REGISTRY_ADDRESS || "0xCdceF2336831C45202dCd739BDcF3D0661E342c7";
const CHAIN_ID = process.env.MST_CHAIN_ID || "mst-testnet"; // used inside blockchainAccountId (eip155:<chainId>)

// Standard ERC-1056 EthereumDIDRegistry ABI (only the read functions we need)
const REGISTRY_ABI = [
  "function identityOwner(address identity) view returns (address)",
  "function changed(address identity) view returns (uint256)",
  "function nonce(address identity) view returns (uint256)"
];

if (!MST_TESTNET_RPC) {
  console.warn("WARNING: MST_RPC_URL is not set. Set it to the MST Testnet JSON-RPC endpoint.");
}

const provider = new ethers.JsonRpcProvider(MST_TESTNET_RPC);
const registry = new ethers.Contract(REGISTRY_CONTRACT_ADDRESS, REGISTRY_ABI, provider);

app.get('/1.0/identifiers/:did', async (req, res) => {
  const did = req.params.did;

  // Expected format: did:mst:testnet:0xADDRESS
  const parts = did.split(':');
  if (parts[0] !== 'did' || parts[1] !== 'mst' || parts.length < 4) {
    return res.status(400).json({
      didResolutionMetadata: { error: 'invalidDid', message: `Unsupported DID format: ${did}` },
      didDocument: null,
      didDocumentMetadata: {}
    });
  }

  const network = parts[2];       // testnet / mainnet
  const address = parts[3];       // 0x... identity address

  if (!ethers.isAddress(address)) {
    return res.status(400).json({
      didResolutionMetadata: { error: 'invalidDid', message: `Invalid Ethereum-style address: ${address}` },
      didDocument: null,
      didDocumentMetadata: {}
    });
  }

  try {
    const owner = await registry.identityOwner(address);
    const lastChanged = await registry.changed(address);

    const didDocument = {
      "@context": [
        "https://www.w3.org/ns/did/v1",
        "https://w3id.org/security/suites/secp256k1recovery-2020/v2"
      ],
      id: did,
      controller: did,
      verificationMethod: [
        {
          id: `${did}#controller`,
          type: "EcdsaSecp256k1RecoveryMethod2020",
          controller: did,
          blockchainAccountId: `eip155:${CHAIN_ID}:${owner}`
        }
      ],
      authentication: [`${did}#controller`],
      assertionMethod: [`${did}#controller`]
    };

    return res.json({
      didDocument,
      didResolutionMetadata: {
        contentType: "application/did+ld+json",
        network,
        registry: REGISTRY_CONTRACT_ADDRESS
      },
      didDocumentMetadata: {
        // block number of the last change event on-chain, useful for auditing/caching
        lastChangedBlock: lastChanged.toString()
      }
    });

  } catch (err) {
    console.error(`Resolution failed for ${did}:`, err.message);
    return res.status(404).json({
      didResolutionMetadata: { error: 'notFound', message: err.message },
      didDocument: null,
      didDocumentMetadata: {}
    });
  }
});

// simple health check, useful once this sits behind Universal Resolver / docker-compose
app.get('/health', (req, res) => res.json({ status: 'ok' }));

app.listen(PORT, () => {
  console.log(`did:mst driver running on port ${PORT}`);
  console.log(`Registry: ${REGISTRY_CONTRACT_ADDRESS}`);
  console.log(`RPC: ${MST_TESTNET_RPC || '(not set!)'}`);
});