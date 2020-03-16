![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-resolver/master/docs/logo-dif.png)

# Universal Resolver

A Universal Resolver is an identifier resolver that works with any decentralized identifier system, including Decentralized Identifiers (DIDs).

See this [blog post](https://medium.com/decentralized-identity/a-universal-resolver-for-self-sovereign-identifiers-48e6b4a5cc3c) and this [webinar](https://ssimeetup.org/did-resolution-given-did-how-do-retrieve-document-markus-sabadello-webinar-13/) for an introduction.

See https://uniresolver.io/ for a publicly hosted instance of a Universal Resolver.

## Quick Start

You can deploy the Universal Resolver on your local machine by cloning this Github repository, and using `docker-compose` to build and run the Universal Resolver as well as its drivers.

Before running docker-compose, please place a valid Personal Access Token for GitHub in file [resolver/java/settings.xml](https://github.com/decentralized-identity/universal-resolver/blob/master/resolver/java/settings.xml), as explained here [Creating a personal access token for the command line](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line). Only the scope **read:packages** is required.


	git clone https://github.com/decentralized-identity/universal-resolver
	cd universal-resolver/
	docker-compose -f docker-compose.yml pull
	docker-compose -f docker-compose.yml up

You should then be able to resolve identifiers locally using simple `curl` requests as follows:

	curl -X GET http://localhost:8080/1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw
	curl -X GET http://localhost:8080/1.0/identifiers/did:btcr:xz35-jznz-q6mr-7q6
	curl -X GET http://localhost:8080/1.0/identifiers/did:v1:test:nym:3AEJTDMSxDDQpyUftjuoeZ2Bazp4Bswj1ce7FJGybCUu
	curl -X GET http://localhost:8080/1.0/identifiers/did:ipid:QmYA7p467t4BGgBL4NmyHtsXMoPrYH9b3kSG6dbgFYskJm
	curl -X GET http://localhost:8080/1.0/identifiers/did:web:uport.me
	curl -X GET http://localhost:8080/1.0/identifiers/did:ethr:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736
	curl -X GET http://localhost:8080/1.0/identifiers/did:nacl:Md8JiMIwsapml_FtQ2ngnGftNP5UmVCAUuhnLyAsPxI
	curl -X GET http://localhost:8080/1.0/identifiers/did:jolo:e76fb4b4900e43891f613066b9afca366c6d22f7d87fc9f78a91515be24dfb21
	curl -X GET http://localhost:8080/1.0/identifiers/did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0
	curl -X GET http://localhost:8080/1.0/identifiers/did:erc725:ropsten:2F2B37C890824242Cb9B0FE5614fA2221B79901E
	curl -X GET http://localhost:8080/1.0/identifiers/did:hcr:0f674e7e-4b49-4898-85f6-96176c1e30de
	curl -X GET http://localhost:8080/1.0/identifiers/did:neoid:priv:b4eeeb80d20bfb38b23001d0659ce0c1d96be0aa
	curl -X GET http://localhost:8080/1.0/identifiers/did:elem:EiAS3mqC4OLMKOwcz3ItIL7XfWduPT7q3Fa4vHgiCfSG2A
	curl -X GET http://localhost:8080/1.0/identifiers/did:github:gjgd
	curl -X GET http://localhost:8080/1.0/identifiers/did:ccp:ceNobbK6Me9F5zwyE3MKY88QZLw
	curl -X GET http://localhost:8080/1.0/identifiers/did:work:2UUHQCd4psvkPLZGnWY33L
	curl -X GET http://localhost:8080/1.0/identifiers/did:ont:AN5g6gz9EoQ3sCNu7514GEghZurrktCMiH
	curl -X GET http://localhost:8080/1.0/identifiers/did:kilt:5GFs8gCumJcZDDWof5ETFqDFEsNwCsVJUj2bX7y4xBLxN5qT
	curl -X GET http://localhost:8080/1.0/identifiers/did:evan:testcore:0x126E901F6F408f5E260d95c62E7c73D9B60fd734

If this doesn't work, see [Troubleshooting](/docs/troubleshooting.md).

## Drivers

Are you developing a DID method and Universal Resolver driver? Click [Driver Development](/docs/driver-development.md) for instructions.

| Driver Name | Driver Version | DID Spec Version | DID Method Spec Version | Docker Image |
| ----------- | -------------- | ---------------- | ----------------------- | ------------ |
| [did-btcr](https://github.com/decentralized-identity/uni-resolver-driver-did-btcr/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://w3c-ccg.github.io/didm-btcr) | [universalresolver/driver-did-btcr](https://hub.docker.com/r/universalresolver/driver-did-btcr/)
| [did-sov](https://github.com/decentralized-identity/uni-resolver-driver-did-sov/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://sovrin-foundation.github.io/sovrin/spec/did-method-spec-template.html) | [universalresolver/driver-did-sov](https://hub.docker.com/r/universalresolver/driver-did-sov/)
| [did-erc725](https://github.com/decentralized-identity/uni-resolver-driver-did-erc725/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/WebOfTrustInfo/rebooting-the-web-of-trust-spring2018/blob/master/topics-and-advance-readings/DID-Method-erc725.md) | [universalresolver/driver-did-erc725](https://hub.docker.com/r/universalresolver/driver-did-erc725/)
| [did-stack](https://github.com/decentralized-identity/uni-resolver-driver-did-stack/) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | [universalresolver/driver-did-stack](https://hub.docker.com/r/universalresolver/driver-did-stack/)
| [did-dom](https://github.com/decentralized-identity/uni-resolver-driver-did-dom/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | [universalresolver/driver-did-dom](https://hub.docker.com/r/universalresolver/driver-did-dom/)
| [did-uport](https://github.com/uport-project/uport-did-driver) | 1.3.1 | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://docs.google.com/document/d/1vS6UBUDwxYR8tLTNo4HUhGe2qb9Q95QLiJTt9NkwZ8M/) | [uport/uni-resolver-driver-did-uport](https://hub.docker.com/r/uport/uni-resolver-driver-did-uport/)
| did-v1 |  | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://w3c-ccg.github.io/did-method-v1/) |
| did-ipid |  | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/jonnycrunch/ipid) |
| [did-jolo](https://github.com/jolocom/jolo-did-method/tree/master/jolocom-did-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/jolocom/jolocom-did-driver/blob/master/jolocom-did-method-specification.md) | [jolocomgmbh/jolocom-did-driver](https://hub.docker.com/r/jolocomgmbh/jolocom-did-driver) |
| [did-hacera](https://github.com/hacera/hacera-did-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | [hacera/hacera-did-driver](https://hub.docker.com/r/hacera/hacera-did-driver) |
| [did-elem](https://github.com/decentralized-identity/element) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | |
| [did-seraphid](https://github.com/swisscom-blockchain/seraph-id-did-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) |  [swisscomblockchainag/seraph-id-did-driver](https://hub.docker.com/r/swisscomblockchainag/seraph-id-did-driver) |
| [did-github](https://github.com/decentralized-identity/github-did) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | |
| [did-ccp](https://github.com/decentralized-identity/uni-resolver-driver-did-ccp/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://did.baidu.com/did-spec/) | [hello2mao/driver-did-ccp](https://hub.docker.com/r/hello2mao/driver-did-ccp/)
| [did-work](https://github.com/decentralized-identity/uni-resolver-driver-did-work/) | 0.1  | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://workday.github.io/work-did-method-spec/) | [didwork/work-did-driver](https://hub.docker.com/r/didwork/work-did-driver)|
| [did-ont](https://github.com/ontio/ontid-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://github.com/ontio/ontology-DID/blob/master/docs/en/DID-ONT-method.md) |  [ontio/ontid-driver](https://hub.docker.com/r/ontio/ontid-driver) |
| [did-kilt](https://github.com/KILTprotocol/kilt-did-driver) | 1.0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://github.com/KILTprotocol/kilt-did-driver/blob/master/DID%20Method%20Specification.md) | [kiltprotocol/kilt-did-driver](https://hub.docker.com/r/kiltprotocol/kilt-did-driver)|
| [did-evan](https://github.com/evannetwork/did-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [0.9](https://github.com/evannetwork/evan.network-DID-method-specification/blob/master/evan_did_method_spec.md) | [evannetwork/evan-did-driver](https://hub.docker.com/r/evannetwork/evan-did-driver) |

## More Information

 * [Driver Development](/docs/driver-development.md)
 * [Continuous Integration and Delivery](/docs/continuous-integration-and-delivery.md)
 * [Development System](/docs/dev-system.md)
 * [Creating Releases](/docs/creating-releases.md)
 * [Branching Strategy](/docs/branching-strategy.md)
 * [Design Goals](/docs/design-goals.md)
 * [Troubleshooting](/docs/troubleshooting.md)
 * [Java Components](/resolver/java)

## About

<img align="left" src="https://raw.githubusercontent.com/decentralized-identity/universal-resolver/master/docs/logo-dif.png" width="115">

Decentralized Identity Foundation - http://identity.foundation/

<br clear="left" />

<img align="left" src="https://raw.githubusercontent.com/decentralized-identity/universal-resolver/master/docs/logo-ngi0pet.png" width="115">

Supported by [NLnet](https://nlnet.nl/) and [NGI0 PET](https://nlnet.nl/PET/#NGI), which is made possible with financial support from the European Commission's [Next Generation Internet](https://ngi.eu/) programme.
