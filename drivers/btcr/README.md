![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/decentralized-identity.github.io/master/images/logo-small.png)

# Universal Resolver Driver: did:btcr

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:btcr** identifiers.

## Specifications

* [Decentralized Identifiers](https://w3c-ccg.github.io/did-spec/)
* [BTCR DID Method 0.1](https://w3c-ccg.github.io/didm-btcr)

## Example DIDs

```
did:btcr:xz35-jzv2-qqs2-9wjt
did:btcr:x705-jzv2-qqaz-7vuz
did:btcr:xkrn-xzcr-qqlv-j6sl
```

## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t universalresolver/driver-did-btcr
docker run -p 8080:8080 universalresolver/driver-did-btcr
curl -X GET http://localhost:8080/1.0/identifiers/did:btcr:xz35-jzv2-qqs2-9wjt
```

## Build (native)

 1. First, build https://github.com/decentralized-identity/universal-resolver-java
 1. Then, `mvn clean install`

## Driver Environment Variables

The driver recognizes the following environment variables:

### `uniresolver_driver_did_btcr_bitcoinConnection`

 * Specifies how the driver interacts with the Bitcoin blockchain.
 * Possible values: 
   * `bitcoind`: Connects to a [bitcoind](https://bitcoin.org/en/full-node) instance via JSON-RPC
   * `btcd`: Connects to a [btcd](https://github.com/btcsuite/btcd) instance via JSON-RPC
   * `bitcoinj`: Connects to Bitcoin using a local [bitcoinj](https://bitcoinj.github.io/) client
   * `blockcypherapi`: Connects to [BlockCypher's API](https://www.blockcypher.com/dev/bitcoin/)
 * Default value: `blockcypherapi`

### `uniresolver_driver_did_btcr_rpcUrlMainnet`

 * Specifies the JSON-RPC URL of a bitcoind/btcd instance running on Mainnet.
 * Default value: `http://user:pass@localhost:8332/`

### `uniresolver_driver_did_btcr_rpcUrlTestnet`

 * Specifies the JSON-RPC URL of a bitcoind/btcd instance running on Testnet.
 * Default value: `http://user:pass@localhost:18332/`

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `fragmentUri`: ...
* `chain`: ...
* `blockHeight`: ...
* `blockIndex`: ...
