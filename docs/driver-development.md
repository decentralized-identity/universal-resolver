# Universal Resolver — Driver Development

## Introduction

The Universal Resolver's main function is wrapping an API around a number of colocated Docker containers running DID-method-specific resolvers. As such, the resolver supports all open-source drivers which have been contributed to-date. The Universal Resolver is meant to support additional DID methods as they are developed by members of various communities. The contribution for a new DID method consists of a Docker image which exposes an HTTP GET interface for resolving DIDs. Although drivers can be implemented in whichever language and architecture the contributor prefers, drivers that are contributed will share a few common characteristics.

Each driver is an implementation of the function `resolve(did) -> diddoc` for a given DID method and it exposes an HTTP GET interface. Your driver will be invoked via an HTTP GET call to:

`http://<your-image>:8080/1.0/identifiers/<your-did>`

In this call, your driver will receive an Accept header with the value `application/json+ld`, and it should return a valid JSON-LD [DID Document](https://w3c-ccg.github.io/did-resolution/#output-diddocument) or, optionally, a [DID Resolution Result](https://w3c-ccg.github.io/did-resolution/#output-didresolutionresult) (which contains the DID Document plus additional result data) in the HTTP body.

Depending on the DID method, oftentimes DID drivers will need to read some decentralized ledger or distributed filesystem (the "target system") in order to resolve a DID. Each driver may decide how it will communicate with its respective target system. For those drivers performing operations on DLT's, the driver may do so via web API, communicating with a remote node, running a full node, or another experimental configuration.

## Requirements
- driver must expose an HTTP GET interface for resolving a DID
- driver must return a valid JSON-LD DID Document or DID Resolution Result for at least 1 working example
- driver must be fully open-source under a permissive license (Apache 2.0 preferred)
- driver must run as a single Docker container
- driver image must be published on [Docker Hub](https://hub.docker.com/) with version tags
- driver must be tested as standalone Docker container
- driver must be tested by running the Universal Resolver with the `docker-compose` command
- contributor expectations:
    * follow the driver contributor guide below
    * add new DID method(s) to [DID method registry](https://w3c-ccg.github.io/did-method-registry/)
    * specify which DID method(s) supported
    * document at least one working example identifier that can be resolved

## How to contribute a driver

Contributing a driver to the Universal Resolver is a common process used to expand the functionality of the service as new DID methods are added and used in the real world. The basic process for contributing a DID driver will remain the same, and we will use a simple versioning system to keep track of all the drivers supported. 

New contributions are submitted as Pull Requests to the [main repository](https://github.com/decentralized-identity/universal-resolver). Since the Universal Resolver runs as a set of modular virtual containers, your implementation is expected to be well-documented, tested, and working before you submit a PR. 

In order to contribute a driver to the Universal Resolver, the driver must be published in a few different ways. As mentioned, all drivers are open-source and use permissive licensing. You may choose to publish your code, including your implementation as well as your Dockerfile, at the [Decentralized Identity Foundation](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers) or on another publicly available site (note: if hosting your code outside of the main repo, please provide a link). In addition, your Docker image must be published on [Docker Hub](https://hub.docker.com/) with version tags to allow configuration of your driver. Ideally, the same Docker Hub image is kept up-to-date to preserve versioning history.

The documentation for your driver should be clear enough to explain how to run your driver, how to troubleshoot it, and a process for updating the driver over time. There are several additional items that need to be documented upon contribution of your new driver.
Make sure you include the following in your PR:

- assign initial version number
- edit files in the Universal Resolver root directory:
  * .env
    * list environment variables (if any) with default values
  * config.json
  * docker-compose.yml
  * README.md (insert a line to the driver table)
    * driver name (e.g. `did-btcr`), with link to driver source code
    * driver version (e.g. `0.1`)
    * DID spec version that the driver conforms to, with link to DID spec
    * Docker image name (e.g. `universalresolver/driver-did-btcr`) with link to Docker image at Docker Hub
    * {optional} DID resolution spec version that the driver conforms to, with link to DID resolution spec
    * {optional} DID method spec version (e.g. BTCR), with link to DID method spec

## How to update a driver

As DID methods are developed and matured, the Universal Resolver should maintain its DID drivers with new changes. Contributors should keep their drivers up-to-date as changes happen to the DID method, DID spec, and DID Resolution spec. Contributors may only wish to direct users to the latest driver, or they may have a `stable` version, a `developer` version, etc. The driver version specified in the README.md file should be reflected in a Docker Hub image with a tag that matches the driver version. As always, the Docker image should be tested as a standalone container as well as a container cluster with `docker-compose`.

As with contributing a driver, there are a few documentation requirements that should be met before submitting a PR: 

- increment new Docker image version
- edit files in the Universal Resolver root directory:
  * config.json (update driver version)
  * docker-compose.yml (update driver version)
  * README.md (update driver version, DID spec version, DID Resolution spec version, Docker Hub link)

## How to test a driver locally with the Universal Resolver

Once your driver is implemented and published as a docker container on dockerhub, you may want to test that it is running properly within the universal resolver.

To do so, follow these steps:

- clone the universal resolver (this) repository:

  ```bash
  git clone https://github.com/decentralized-identity/universal-resolver
  cd universal-resolver/
  ```

- make the required changes mentioned above ("How to contribute a driver") to the `.env`, `config.json` and `docker-compose.yml` files.
- build uni-resolver-web locally:

  ```bash
  docker build -f ./resolver/java/uni-resolver-web/docker/Dockerfile . -t universalresolver/uni-resolver-web
  ```

- run the uni-resolver-web locally:

  ```bash
  docker-compose -f docker-compose.yml pull
  docker-compose -f docker-compose.yml up
  ```

You can now resolve DID Documents via `curl` commands as documented in the [Quick Start](https://github.com/decentralized-identity/universal-resolver#quick-start) notes.

