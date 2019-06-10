![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/decentralized-identity.github.io/master/images/logo-small.png)

# Universal Resolver Driver: did:sov

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:sov** identifiers.

## Specifications

* [Decentralized Identifiers](https://w3c-ccg.github.io/did-spec/)
* [Sovrin DID Method Specification](https://sovrin-foundation.github.io/sovrin/spec/did-method-spec-template.html)

## Example DIDs

```
did:sov:WRfXPg8dantKVubE3HX8pw
did:sov:stn:WRfXPg8dantKVubE3HX8pw
```

## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t universalresolver/driver-did-sov
docker run -p 8080:8080 universalresolver/driver-did-sov
curl -X GET http://localhost:8080/1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw
```

## Build (native Java)

 1. First, build https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/java
 1. Then, build https://github.com/hyperledger/indy-sdk/tree/master/wrappers/java

Then run:

	mvn clean install

## Driver Environment Variables

The driver recognizes the following environment variables:

### `uniresolver_driver_did_sov_libIndyPath`

 * Specifies the path to the Indy SDK library.
 * Default value: (empty string)

### `uniresolver_driver_did_sov_poolConfigs`

 * Specifies a semi-colon-separated list of Indy network names and pool configuration files. The default network is `_`.
 * Default value: `_;./sovrin/mainnet.txn;staging;./sovrin/stagingnet.txn;builder;./sovrin/buildernet.txn;danube;./sovrin/danube.txn`

### `uniresolver_driver_did_sov_poolVersions`

 * Specifies a semi-colon-separated list of Indy network names and pool protocol versions. The default network is `_`.
 * Default value: `_;2;staging;2;builder;2;danube;2`

### `uniresolver_driver_did_sov_walletName`

 * Specifies the name of the Indy wallet.
 * Default value: `default`

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `nymResponse`: Response to the Indy `GET_NYM` operation, including `txnTime`, `state_proof`, and other information.
* `attrResponse`: Response to the Indy `GET_ATTR` operation, including `txnTime`, `state_proof`, and other information.
