![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/decentralized-identity.github.io/master/images/logo-small.png)

# Universal Resolver Driver: did:erc725

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:erc725** identifiers.

## Specifications

* [Decentralized Identifiers](https://w3c-ccg.github.io/did-spec/)
* [ERC725 DID Method Specification](https://github.com/WebOfTrustInfo/rebooting-the-web-of-trust-spring2018/blob/master/topics-and-advance-readings/DID-Method-erc725.md)

## Example DIDs

```
did:erc725:ropsten:2F2B37C890824242Cb9B0FE5614fA2221B79901E
```

## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t universalresolver/driver-did-erc725
docker run -p 8080:8080 universalresolver/driver-did-erc725
curl -X GET http://localhost:8080/1.0/identifiers/did:erc725:ropsten:2F2B37C890824242Cb9B0FE5614fA2221B79901E
```

## Build (native Java)

 1. First, build https://github.com/decentralized-identity/universal-resolver-java
 1. Then, `mvn clean install`

## Driver Environment Variables

The driver recognizes the following environment variables:

### `uniresolver_driver_did_erc725_ethereumConnection`

 * Specifies how the driver interacts with the Ethereum blockchain.
 * Possible values: 
   * `jsonrpc`: Connects to a [geth](https://geth.ethereum.org/downloads/) instance via JSON-RPC
   * `hybrid`: Connects to a [geth](https://geth.ethereum.org/downloads/) instance via JSON-RPC as well as to the [EtherScan API](https://etherscan.io/apis)
 * Default value: `jsonrpc`

### `uniresolver_driver_did_erc725_rpcUrlMainnet`

 * Specifies the JSON-RPC URL of a geth instance running on Mainnet.
 * Default value: `https://mainnet.infura.io/9W2fvWQMP6cJCMH3ESqP`

### `uniresolver_driver_did_erc725_rpcUrlRopsten`

 * Specifies the JSON-RPC URL of a geth instance running on Ropsten.
 * Default value: `https://ropsten.infura.io/9W2fvWQMP6cJCMH3ESqP`

### `uniresolver_driver_did_erc725_rpcUrlRinkeby`

 * Specifies the JSON-RPC URL of a geth instance running on Rinkeby.
 * Default value: `https://rinkeby.infura.io/9W2fvWQMP6cJCMH3ESqP`

### `uniresolver_driver_did_erc725_rpcUrlKovan`

 * Specifies the JSON-RPC URL of a geth instance running on Kovan.
 * Default value: `https://kovan.infura.io/9W2fvWQMP6cJCMH3ESqP`

### `uniresolver_driver_did_erc725_etherscanApiMainnet`

 * Specifies the URL of the EtherScan API for Mainnet.
 * Default value: `http://api.etherscan.io/api`

### `uniresolver_driver_did_erc725_etherscanApiRopsten`

 * Specifies the URL of the EtherScan API for Ropsten.
 * Default value: `http://api-ropsten.etherscan.io/api`

### `uniresolver_driver_did_erc725_etherscanApiRinkeby`

 * Specifies the URL of the EtherScan API for Rinkeby.
 * Default value: `http://api-rinkeby.etherscan.io/api`

### `uniresolver_driver_did_erc725_etherscanApiKovan`

 * Specifies the URL of the EtherScan API for Kovan.
 * Default value: `http://api-kovan.etherscan.io/api`

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `managementKeys`: MANAGEMENT keys as defined by ERC725.
* `managementAddresses`: MANAGEMENT key addresses as defined by ERC725.
* `actionKeys`: ACTION keys as defined by ERC725.
* `actionAddresses`: ACTION key addresses as defined by ERC725.
* `claimKeys`: CLAIM keys as defined by ERC725.
* `claimAddresses`: CLAIM key addresses as defined by ERC725.
* `encryptionKeys`: ENCRYPTION keys as defined by ERC725.
* `encryptionAddresses`: ENCRYPTION key addresses as defined by ERC725.
