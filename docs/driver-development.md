# Universal Resolver — Driver Development

## Introduction

The Universal Resolver's function is wrapping an API around a number of co-located Docker containers running DID-method-specific drivers. The Universal Resolver is designed to support additional DID methods as they are developed by the community. The contribution for a new DID method driver consists of a Docker image which exposes an HTTP interface for resolving DIDs. New contributions are submitted as Pull Requests to the Universal Resolver (this) repository.

Your driver will be invoked via an HTTP GET call to:

`http://<your-image>:8080/1.0/identifiers/<your-did>`

Your driver will receive an `Accept` header with the value `application/ld+json`, and it should return either a valid [DID Document](https://w3c-ccg.github.io/did-resolution/#output-diddocument) or a [DID Resolution Result](https://w3c-ccg.github.io/did-resolution/#output-didresolutionresult) in the HTTP body.

### API Definition

A Swagger API definition is available here:

https://github.com/decentralized-identity/universal-resolver/blob/master/swagger/api-driver.yml

### Example Driver

See this example driver:

https://github.com/peacekeeper/uni-resolver-driver-did-example

### Example PR

See this example PR for contributing a driver:

https://github.com/decentralized-identity/universal-resolver/pull/100

## Driver Requirements

- Driver must be fully open-source under a permissive license (Apache 2.0 preferred).
- Driver source code may be published at DIF or anywhere else.
- Driver image must be published on a publicly accessible container registry.
- Driver image should be tested as standalone Docker container.
- Driver image should be tested as part of the Universal Resolver with `docker-compose`.

## How to contribute a driver

Contributing a driver to the Universal Resolver expands the functionality of the service as new DID methods are added and used by the community.

In order to contribute a driver to the Universal Resolver, the driver's source code must be published. You may choose to publish it at the [Decentralized Identity Foundation](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers) or on another publicly available site. In addition, your Docker image must be published on a publicly accessible container registry with version tags to allow configuration of your driver. Ideally, the same Docker image is kept up-to-date to preserve versioning history (see below in the "How to Update a Driver" section).

In your PR, edit the following files in the Universal Resolver root directory:

- `.env`
  * list environment variables (if any) with default values
- `config.json` (add your driver)
  * regular expression for matching your DID method
  * Docker image name
  * example identifiers
- `docker-compose.yml` (add your driver)
  * docker image name and increment port number
  * environment variables
- `README.md` (insert a line to the driver table)
  * driver name (e.g. `did-example`), with link to driver source code
  * driver version (e.g. `0.1`)
  * DID spec version that the driver conforms to, with link to DID spec
  * DID method spec version (e.g. `0.1`), with link to DID method spec (or mark "missing")
  * Docker image name (e.g. `exampleorg/uni-resolver-driver-did-example`) with link to hosted Docker image

Your driver is expected to be well-documented, tested, and working before you submit a PR. The documentation for your driver should be clear enough to explain how to run your driver, how to troubleshoot it, and a process for updating the driver over time.

## How to update a driver

As DID methods are developed and matured, the Universal Resolver should maintain its DID drivers with new changes. Contributors should keep their drivers up-to-date as changes happen to the DID Core spec and the DID method spec. Contributors may only wish to direct users to the latest driver, or they may have a `stable` version, a `developer` version, etc. The driver version specified in the `README.md` file should be reflected in a Docker image with a tag that matches the driver version.

In order to update a driver, simply submit a new PR that increments the Docker image version and updates the relevant files (see above in the "How to Contribute a Driver" section).

## How to test a driver locally

Once your driver is implemented and published as a Docker container, you may want to test that it is running properly within the Universal Resolver.

To do so, follow these steps:

- Clone the Universal Resolver (this) repository:

  ```bash
  git clone https://github.com/decentralized-identity/universal-resolver
  cd universal-resolver/
  ```

- Make the required changes mentioned above ("How to contribute a driver") to the `.env`, `config.json` and `docker-compose.yml` files.

- Build uni-resolver-web locally:

  ```bash
  docker build -f ./resolver/java/uni-resolver-web/docker/Dockerfile . -t universalresolver/uni-resolver-web
  ```

- Run the uni-resolver-web locally:

  ```bash
  docker-compose -f docker-compose.yml pull
  docker-compose -f docker-compose.yml up
  ```

You can now resolve DID Documents via `curl` commands as documented in the [Quick Start](https://github.com/decentralized-identity/universal-resolver#quick-start) notes.

## Additional Notes

- Depending on the DID method, oftentimes DID drivers will need to read some decentralized ledger or distributed filesystem (the "target system") in order to resolve a DID. Each driver may decide how it will communicate with its respective target system. For those drivers performing operations on DLT's, the driver may do so via web API, communicating with a remote node, running a full node, or another experimental configuration.
- The detailed definition for the DID Resolution HTTP(S) binding can be found [here](https://w3c-ccg.github.io/did-resolution/#bindings-https).
