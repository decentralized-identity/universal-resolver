![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-resolver/master/docs/logo-dif.png)

# Universal Resolver Driver: did:work

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:work** identifiers.

## Specifications

* [Decentralized Identifiers](https://w3c.github.io/did-core/)
* [DID Method Specification](https://workday.github.io/work-did-method-spec/)

## Example DIDs

```
did:work:2UUHQCd4psvkPLZGnWY33L

```

## Build and Run (Docker)
```
docker build -f ./docker/Dockerfile . -t didwork/work-did-driver
docker run -p 8080:8080 didwork/work-did-driver
curl -X GET http://localhost:8080/1.0/identifiers/did:work:2UUHQCd4psvkPLZGnWY33L

```

## Build (native Java)

 1. First, build https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/java
 1. Then, `mvn clean install`

## Driver Environment Variables

`uniresolver_driver_did_work_apikey` an API Key to allow throttling
`uniresolver_driver_did_work_domain` the URI to call into the Workday Credentials platform

