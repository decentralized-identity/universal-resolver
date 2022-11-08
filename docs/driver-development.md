# Universal Resolver — Driver Development

## Introduction

The Universal Resolver wraps an API around a number of co-located Docker containers running DID-method-specific drivers. Additional DID methods can be supported as they are developed by the community. The contribution for a new DID method driver consists of a Docker image which exposes an HTTP interface for resolving DIDs. New contributions are submitted as Pull Requests to the Universal Resolver (this) repository.

An example driver is available [here](https://github.com/peacekeeper/uni-resolver-driver-did-example).

An example PR for contributing a driver is available [here](https://github.com/decentralized-identity/universal-resolver/pull/100).

## Driver Interface

Your driver will be invoked via an HTTP GET call to:

`http://<your-image-url>/1.0/identifiers/<your-did>`

Your driver will receive an `Accept` header with the value `application/ld+json`, and it should return either a valid [DID Document](https://w3c-ccg.github.io/did-resolution/#output-diddocument) or a [DID Resolution Result](https://w3c-ccg.github.io/did-resolution/#output-didresolutionresult) in the HTTP body. Your driver should also return an appropriate value in the `Content-Type` header, such as `application/did+ld+json`.

A Swagger API definition is available [here](https://github.com/decentralized-identity/universal-resolver/blob/main/swagger/api-driver.yml).

For more information about this interface, see the [DID Resolution](https://w3c-ccg.github.io/did-resolution/) specification.

## Driver Rules

- The DID method implemented by the driver must have a specification that is registered in the
  [DID Method Registry](https://w3c.github.io/did-spec-registries/#did-methods) of the W3C DID WG.
- Multiple drivers for the same DID method are allowed and can be listed in the README, but only one can be included in the
  configuration. In this case, the DIF I&D WG will choose a default.
- Contact information must be provided when contributing a driver (either email address, or Github/Gitlab/Bitbucket handle).
- Driver source code must be publicly available and fully open-source under a permissive license (Apache 2.0 preferred).
- Driver source code may optionally be hosted at DIF (as a contribution to the I&D WG).
- Driver image must be published on a publicly accessible container registry.
- Driver image hosted on Dockerhub should have [Expanded Support for Open Source Software Projects](https://www.docker.com/blog/expanded-support-for-open-source-software-projects/) to avoid hitting Dockerhub pull limits.
- Driver image should be tested both as standalone Docker container, and as part of the Universal Resolver with `docker-compose`.

## How to

### How to contribute a driver

Create a PR that edits the following files in the Universal Resolver root directory and uni-resolver-web's [`application.yml`](https://github.com/decentralized-identity/universal-resolver/blob/main/uni-resolver-web/src/main/resources/application.yml):

- [`docker-compose.yml`](https://github.com/decentralized-identity/universal-resolver/blob/main/docker-compose.yml) (add your driver, if it has a Docker image)
  * image - your Docker image name
  * ports - incremented port number exposed by your Docker image
  * environment - optional environment variables supported by your Docker image
- [`application.yml`](https://github.com/decentralized-identity/universal-resolver/blob/main/uni-resolver-web/src/main/resources/application.yml) (add  your driver)
  * pattern - regular expression for matching your DID method
  * url - endpoint of your Docker image or external resolver endpoint
  * testIdentifiers - list of example DIDs that your driver can resolve
- [`.env`](https://github.com/decentralized-identity/universal-resolver/blob/main/.env)
  * list environment variables (if any) with default values
- [`README.md`](https://github.com/decentralized-identity/universal-resolver/blob/main/README.md) (insert a line to the driver table)
  * driver name (e.g. `did-example`), with link to driver source code
  * driver version (e.g. `0.1`), should match Docker image version
  * DID method spec version (e.g. `0.1`), with link to DID method spec (or mark "missing")
  * Docker image name (e.g. `exampleorg/uni-resolver-driver-did-example`) with link to hosted Docker image
  * contact information

Your driver is expected to be well-documented, tested, and working before you submit a PR. The documentation for your driver should be clear enough to explain how to run your driver, how to troubleshoot it, and a process for updating the driver over time.

### How to update a driver

Contributors should keep their drivers up-to-date as changes happen to the DID Core spec and the DID method spec. Driver implementers may wish to use the `:latest` Docker image version, but should preferably use incremental Docker image versions.

In order to update a driver, simply submit a new PR that increments the Docker image version and updates the relevant files (see above in the "How to contribute a driver" section).

### How we handle problems with drivers

If your Docker image exists and starts, but your example DIDs cannot be resolved or your driver produces invalid responses:

- You will be contacted and given 30 days to fix the problem; after that, your driver will be removed.
- You are welcome to still fix the problem later and re-contribute your driver.

If your Docker image does not exist:

- Your driver will be removed immediately.
- You are welcome to still fix the problem later and re-contribute your driver.

If your driver does not fulfill the [Driver Rules](#driver-rules):

- You will be contacted and given 30 days to fix the problem; after that, your driver will be removed.
- You are welcome to still fix the problem later and re-contribute your driver.

### How to test a driver locally

Once your driver is implemented and published as a Docker container, you may want to test that it is running properly within the Universal Resolver.

To do so, follow these steps:

- Clone the Universal Resolver (this) repository:

  ```bash
  git clone https://github.com/decentralized-identity/universal-resolver
  cd universal-resolver/
  ```

- Make the required changes mentioned above ("How to contribute a driver") to the `.env`, [`application.yml`](https://github.com/decentralized-identity/universal-resolver/blob/main/uni-resolver-web/src/main/resources/application.yml) and `docker-compose.yml` files.

- Pull remote docker images

  ```bash
    docker-compose -f docker-compose.yml pull
  ```

- Build uni-resolver-web locally:

  ```bash
  docker build -f ./uni-resolver-web/docker/Dockerfile . -t universalresolver/uni-resolver-web
  ```

- Run the uni-resolver-web locally:

  ```bash
  docker-compose -f docker-compose.yml up
  ```

After each local change, you must rebuild uni-resolver-web locally. If you pull docker images, it will overwrite the local uni-resolver-web, so you must rebuild again after pulling.

You can now resolve DID Documents via `curl` commands as documented in the [Quick Start](https://github.com/decentralized-identity/universal-resolver#quick-start) notes.

## Additional Notes

- Depending on the DID method, oftentimes DID drivers will need to read some decentralized ledger or distributed filesystem (the "target system") in order to resolve a DID. Each driver may decide how it will communicate with its respective target system. For those drivers performing operations on DLT's, the driver may do so via web API, communicating with a remote node, running a full node, or another experimental configuration.
- The detailed definition for the DID Resolution HTTP(S) binding can be found [here](https://w3c-ccg.github.io/did-resolution/#bindings-https).
