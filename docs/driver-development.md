## Introduction

The Universal Resolver's main function is wrapping an API around a number of colocated virtual containers running DID-method-specific operations. Although each driver can be implemented in whichever language and architecture the contributor decides, drivers that are contributed still share a few common characteristics.

Each driver is an implementation of the function `resolve(did) -> diddoc` and it exposes an HTTP GET interface. Your driver will be invoked via `http://<your-image>:8080/1.0/identifiers/<your-did>`. Your driver will receive an HTTP GET including an Accept header with the value `application/json+ld`, and it should return a valid DID Document in the HTTP body.

Beyond this interface, each driver may implement its own way to communicate with its respective decentralized ledgers or distributed filesystems (the "target system"). For those drivers performing operations on DLT's, the driver may do so via web API, communicating with a remote node, running a full node, or another experimental configuration.

In addition to contributing a driver, developers may wish to build native applications that locally run the Universal Resolver. The core implementation of the Resolver is a [Java library](https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/java) which can be forked to add functionality for additional DID methods. Although the most common interface for new DID methods are language-agnostic drivers which expose an HTTP interface, the option remains for expansion of native applications in Java or a contributed implementation in another language.


## Requirements
- driver must be fully open-source under a permissive license (Apache 2.0 preferred)
- driver must run as a single Docker container
- driver image must be published on [Dockerhub](https://hub.docker.com/) with version tags
- driver must be tested as standalone Docker container
- driver must be tested by running the Universal Resolver `docker-compose`
- contributor expectations:
    * follow the driver contributor guide below
    * add new DID method(s) to [DID method registry](https://w3c-ccg.github.io/did-method-registry/)
    * specify DID method(s) supported
    * document at least one working example identifier that can be resolved

## How to contribute a driver

Contributing a driver to the Universal Resolver is a common process used to expand the functionality of the service as new DID methods are added and used in the real world. The basic process for contributing a DID driver will remain the same, and we will use a simple versioning system to keep track of all the drivers supported. 

New contributions are submitted as Pull Requests to the [main repository](https://github.com/decentralized-identity/universal-resolver). Since the Universal Resolver runs as a set of modular virtual containers, your implementation is expected to be well-documented, tested, and working before you submit a PR. 

In order to contribute a driver to the Universal Resolver, the driver must be published in a few different ways. As mentioned, all drivers are open-source and use permissive licensing. You may choose to publish your code at the [Decentralized Identity Foundation](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers) or on another publicly available site (note: if hosting your code outside of the main repo, please provide a link). In addition, your Docker image must be published on [Dockerhub](https://hub.docker.com/) with version tags to allow configuration of your driver. Ideally, the same Dockerhub image is kept up-to-date to preserve versioning history.

The documentation should be clear enough to explain how to run your driver, how to debug it, and how to update the driver over time. There are several items that need to be documented upon contribution of your new driver. Include the following in your PR:

- assign initial version number
- edit files in the Universal Resolver root directory:
  * .env
    * List environment variables (if any) with default values
  * config.json
  * docker-compose.yml
  * README.md (insert a line to the driver table)
    * driver name (e.g. did-btcr), with link to driver source code
    * driver version (e.g. 0.1)
    * DID spec version that the driver conforms to, with link to DID spec
    * Docker image name (e.g. universalresolver/driver-did-btcr) with link to Docker image at Dockerhub
    * {optional} DID resolution spec version that the driver conforms to, with link to DID resolution spec
    * {optional} DID method spec version (e.g. BTCR), with link to DID method spec

## How to update a driver

As DID methods are developed and matured, the Universal Resolver should do the same. Contributors should keep their drivers up-to-date as changes happen to the Driver, DID spec, and DID Resolution spec. Contributors may only wish to direct users to the latest driver, or they may have a `stable` version, a `developer` version, etc. The driver version specified in the README.md file should be reflected in a Dockerhub image with a tag that matches the driver version. As always, the Docker image should be tested as a standalone container as well as a container cluster with `docker-compose`.

As with contributing a driver, there are a few documentation requirements that should be met before submitting a PR: 

- increment new Docker image version
- edit files in the Universal Resolver root directory:
  * config.json (update driver version)
  * docker-compose.yml (update driver version)
  * README.md (update driver version, DID spec version, DID Resolution spec version, Dockerhub link)

