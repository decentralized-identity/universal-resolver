![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/decentralized-identity.github.io/master/images/logo-small.png)

# Universal Resolver Driver: did:ccp

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:ccp** identifiers.

More info: 

- [Solution Homepage](https://cloud.baidu.com/solution/digitalIdentity.html)
- [Docs Homepage](https://did.baidu.com)

## Specifications

* [Decentralized Identifiers](https://w3c-ccg.github.io/did-spec/)
* [Baidu Cloud DID Method Specification](https://did.baidu.com/did-spec/)

## Example DIDs

```
did:ccp:ceNobbK6Me9F5zwyE3MKY88QZLw
did:ccp:3CzQLF3qfFVQ1CjGVzVRZaFXrjAd
```

## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t hello2mao/driver-did-ccp
docker run -p 8080:8080 hello2mao/driver-did-ccp
curl -X GET http://localhost:8080/1.0/identifiers/did:ccp:ceNobbK6Me9F5zwyE3MKY88QZLw
```

## Build (native Java)

 1. First, build https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/java

Then run:

	mvn clean install

## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `version`: The DID version.
* `proof`: Some proof info about the DID document.
* `created`: The DID create time.
* `updated`: The DID document last update time.


