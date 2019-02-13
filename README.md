![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/decentralized-identity.github.io/master/images/logo-small.png)

# Universal Resolver

A Universal Resolver is an identifier resolver that works with any decentralized identifier system, including Decentralized Identifiers (DIDs).

See this [blog post](https://medium.com/decentralized-identity/a-universal-resolver-for-self-sovereign-identifiers-48e6b4a5cc3c) for an introduction.

See https://uniresolver.io/ for a publicly hosted instance of a Universal Resolver.

## Quick Start

You can deploy the Universal Resolver on your local machine by cloning this Github repository, and using `docker-compose` to build and run the Universal Resolver as well as its drivers:

	git clone https://github.com/decentralized-identity/universal-resolver
	cd universal-resolver/
	docker-compose -f docker-compose.yml pull
	docker-compose -f docker-compose.yml up

You should then be able to resolve identifiers locally using simple `curl` requests as follows:

	curl -X GET  http://localhost:8080/1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw
	curl -X GET  http://localhost:8080/1.0/identifiers/did:btcr:xkrn-xzcr-qqlv-j6sl
	curl -X GET  http://localhost:8080/1.0/identifiers/did:v1:test:nym:3AEJTDMSxDDQpyUftjuoeZ2Bazp4Bswj1ce7FJGybCUu
	curl -X GET  http://localhost:8080/1.0/identifiers/did:ipid:QmYA7p467t4BGgBL4NmyHtsXMoPrYH9b3kSG6dbgFYskJm
	curl -X GET  http://localhost:8080/1.0/identifiers/did:uport:2omWsSGspY7zhxaG6uHyoGtcYxoGeeohQXz
	curl -X GET  http://localhost:8080/1.0/identifiers/did:jolo:e76fb4b4900e43891f613066b9afca366c6d22f7d87fc9f78a91515be24dfb21
	curl -X GET  http://localhost:8080/1.0/identifiers/did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0

## Implementations

The following implementations are available for use:

 * [Java](https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/java)
 * [Python3](https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/python)

## Drivers

| Driver Name | Driver Version | DID Spec Version | DID Method Spec Version | Docker Image |
| ----------- | -------------- | ---------------- | ----------------------- | ------------ |
| [did-btcr](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/btcr/) | 0.1-SNAPSHOT | [0.11](https://w3c-ccg.github.io/did-spec/) | [0.1](https://w3c-ccg.github.io/didm-btcr) | [universalresolver/driver-did-btcr](https://hub.docker.com/r/universalresolver/driver-did-btcr/)
| [did-sov](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/sov/) | 0.1-SNAPSHOT | [0.11](https://w3c-ccg.github.io/did-spec/) | [0.1](https://github.com/mikelodder7/sovrin/blob/master/spec/did-method-spec-template.html) | [universalresolver/driver-did-sov](https://hub.docker.com/r/universalresolver/driver-did-sov/)
| [did-erc725](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/erc725/) | 0.1-SNAPSHOT | [0.11](https://w3c-ccg.github.io/did-spec/) | [0.1](https://github.com/WebOfTrustInfo/rebooting-the-web-of-trust-spring2018/blob/master/topics-and-advance-readings/DID-Method-erc725.md) | [universalresolver/driver-did-erc725](https://hub.docker.com/r/universalresolver/driver-did-erc725/)
| [did-stack](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/stack/) | 0.1 | [0.11](https://w3c-ccg.github.io/did-spec/) | (missing) | [universalresolver/driver-did-stack](https://hub.docker.com/r/universalresolver/driver-did-stack/)
| [did-dom](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/dom/) | 0.1-SNAPSHOT | [0.11](https://w3c-ccg.github.io/did-spec/) | (missing) | [universalresolver/driver-did-dom](https://hub.docker.com/r/universalresolver/driver-did-dom/)
| [did-uport](https://github.com/uport-project/uport-did-driver) | 1.1.0 | [0.11](https://w3c-ccg.github.io/did-spec/) | [1.0](https://docs.google.com/document/d/1vS6UBUDwxYR8tLTNo4HUhGe2qb9Q95QLiJTt9NkwZ8M/) | [uport/uni-resolver-driver-did-uport](https://hub.docker.com/r/uport/uni-resolver-driver-did-uport/)
| did-v1 |  | [0.11](https://w3c-ccg.github.io/did-spec/) | [1.0](https://w3c-ccg.github.io/didm-veres-one/) |
| did-ipid |  | [0.11](https://w3c-ccg.github.io/did-spec/) | [0.1](https://github.com/jonnycrunch/ipid) |
| [did-jolo](https://github.com/jolocom/jolocom-did-driver) | 0.1 | [0.11](https://w3c-ccg.github.io/did-spec/) | (missing) | [jolocomgmbh/jolocom-did-driver](https://hub.docker.com/r/jolocomgmbh/jolocom-did-driver) |

## Troubleshooting

If docker-compose complains about wrong versions than you probably have a too old docker-compose version.

On Ubuntu 16.04 remove docker-compose and install a new version e.g.
```
sudo apt-get remove docker-compose
curl -L https://github.com/docker/compose/releases/download/1.22.0/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```
You might want to adjust the version number 1.22.0 to the latest one. Please see: [Installing docker-compose](https://docs.docker.com/compose/install/#install-compose)

## More Information

 * [Design Goals](/docs/design-goals.md)
 * [Architecture and Drivers](/docs/architecture-drivers.md)
 * [API Definition](/docs/api-definition.md)

## About

Decentralized Identity Foundation - http://identity.foundation/
 