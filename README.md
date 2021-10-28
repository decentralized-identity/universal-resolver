![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-resolver/master/docs/logo-dif.png)

# Universal Resolver

The Universal Resolver resolves Decentralized Identifiers (DIDs) across many different DID methods, based on the [W3C DID Core 1.0](https://www.w3.org/TR/did-core/) and [DID Resolution](https://w3c-ccg.github.io/did-resolution/) specifications. It is a work item of the [DIF Identifiers&Discovery Working Group](https://github.com/decentralized-identity/identifiers-discovery/).

See this [blog post](https://medium.com/decentralized-identity/a-universal-resolver-for-self-sovereign-identifiers-48e6b4a5cc3c) and this [webinar](https://ssimeetup.org/did-resolution-given-did-how-do-retrieve-document-markus-sabadello-webinar-13/) for an introduction.

See https://dev.uniresolver.io/ for a DIF-hosted instance of the Universal Resolver that can be used for testing purposes. See [Docker Hub](https://hub.docker.com/u/universalresolver) for images.

## Quick Start

You can deploy the Universal Resolver on your local machine by cloning this Github repository, and using `docker-compose` to build and run the Universal Resolver as well as its drivers.

	git clone https://github.com/decentralized-identity/universal-resolver
	cd universal-resolver/
	docker-compose -f docker-compose.yml pull
	docker-compose -f docker-compose.yml up

You should then be able to resolve identifiers locally using simple `curl` requests as follows:

	curl -X GET http://localhost:8080/1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw
	curl -X GET http://localhost:8080/1.0/identifiers/did:btcr:xz35-jznz-q6mr-7q6
	curl -X GET http://localhost:8080/1.0/identifiers/did:v1:test:nym:z6Mkmpe2DyE4NsDiAb58d75hpi1BjqbH6wYMschUkjWDEEuR
	curl -X GET http://localhost:8080/1.0/identifiers/did:key:z6Mkfriq1MqLBoPWecGoDLjguo1sB9brj6wT3qZ5BxkKpuP6
	curl -X GET http://localhost:8080/1.0/identifiers/did:web:did.actor:alice
	curl -X GET http://localhost:8080/1.0/identifiers/did:web:did.actor:bob
	curl -X GET http://localhost:8080/1.0/identifiers/did:web:did.actor:mike
	curl -X GET http://localhost:8080/1.0/identifiers/did:ethr:mainnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736
	curl -X GET http://localhost:8080/1.0/identifiers/did:ethr:0x4:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736
	curl -X GET http://localhost:8080/1.0/identifiers/did:ethr:0x02b97c30de767f084ce3080168ee293053ba33b235d7116a3263d29f1450936b71
	curl -X GET http://localhost:8080/1.0/identifiers/did:ethr:0x1e:0x02b97c30de767f084ce3080168ee293053ba33b235d7116a3263d29f1450936b71
	curl -X GET http://localhost:8080/1.0/identifiers/did:eosio:eos:eoscanadacom
	curl -X GET http://localhost:8080/1.0/identifiers/did:eosio:4667b205c6838ef70ff7988f6e8257e8be0e1284a2f59699054a018f743b1d11:caleosblocks
	curl -X GET http://localhost:8080/1.0/identifiers/did:nacl:Md8JiMIwsapml_FtQ2ngnGftNP5UmVCAUuhnLyAsPxI
	curl -X GET http://localhost:8080/1.0/identifiers/did:jolo:e76fb4b4900e43891f613066b9afca366c6d22f7d87fc9f78a91515be24dfb21
	curl -X GET http://localhost:8080/1.0/identifiers/did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0
	curl -X GET http://localhost:8080/1.0/identifiers/did:hcr:0f674e7e-4b49-4898-85f6-96176c1e30de
	curl -X GET http://localhost:8080/1.0/identifiers/did:neoid:priv:b4eeeb80d20bfb38b23001d0659ce0c1d96be0aa
	curl -X GET http://localhost:8080/1.0/identifiers/did:elem:EiAS3mqC4OLMKOwcz3ItIL7XfWduPT7q3Fa4vHgiCfSG2A
	curl -X GET http://localhost:8080/1.0/identifiers/did:github:gjgd
	curl -X GET http://localhost:8080/1.0/identifiers/did:ccp:ceNobbK6Me9F5zwyE3MKY88QZLw
	curl -X GET http://localhost:8080/1.0/identifiers/did:work:2UUHQCd4psvkPLZGnWY33L
	curl -X GET http://localhost:8080/1.0/identifiers/did:ont:AN5g6gz9EoQ3sCNu7514GEghZurrktCMiH
	curl -X GET http://localhost:8080/1.0/identifiers/did:kilt:5CqJa4Ct7oMeMESzehTiN9fwYdGLd7tqeirRMpGDh2XxYYyx
	curl -X GET http://localhost:8080/1.0/identifiers/did:evan:testcore:0x126E901F6F408f5E260d95c62E7c73D9B60fd734
	curl -X GET http://localhost:8080/1.0/identifiers/did:factom:testnet:6aa7d4afe4932885b5b6e93accb5f4f6c14bd1827733e05e3324ae392c0b2764
	curl -X GET http://localhost:8080/1.0/identifiers/did:mpg:7PGGnRdvKKFftSXU3Jw75Vk5npfg
	curl -X GET http://localhost:8080/1.0/identifiers/did:io:0x476c81C27036D05cB5ebfe30ae58C23351a61C4A
	curl -X GET http://localhost:8080/1.0/identifiers/did:bba:t:45e6df15dc0a7d91dcccd24fda3b52c3983a214fb0eed0938321c11ec99403cf
	curl -X GET http://localhost:8080/1.0/identifiers/did:bid:6cc796b8d6e2fbebc9b3cf9e
	curl -X GET http://localhost:8080/1.0/identifiers/did:schema:public-ipfs:json-schema:Qma2beXKwZeiUXcaRaQKwbBV1TqyiJnsMTYExUTdQue43J
	curl -X GET http://localhost:8080/1.0/identifiers/did:ion:EiClkZMDxPKqC9c-umQfTkR8vvZ9JPhl_xLDI9Nfk38w5w
	curl -X GET http://localhost:8080/1.0/identifiers/did:ace:0xf81c16a78b257c10fddf87ed4324d433317169a005ddf36a3a1ba937ba9788e3
	curl -X GET http://localhost:8080/1.0/identifiers/did:gatc:2xtSori9UQZdTqzxrkp7zqKM4Kj5B4C7
	curl -X GET http://localhost:8080/1.0/identifiers/did:icon:01:64aa0a2a479cb47afbf2d18d6f9f216bcdcbecdda27ccba3
	curl -X GET http://localhost:8080/1.0/identifiers/did:vaa:3wJVWDQWtDFx27FqvSqyo5xsTsxC
	curl -X GET http://localhost:8080/1.0/identifiers/did:unisot:1EjHm7VtgsqNzCkvA8XRgGXZ1UKo1txSM4
	curl -X GET http://localhost:8080/1.0/identifiers/did:sol:devnet:2eK2DKs6vdzTEoj842Gfcs6DdtffPpw1iF6JbzQL4TuK
	curl -X GET http://localhost:8080/1.0/identifiers/did:lit:AEZ87t1bi5bRxmVh3ksMUi
	curl -X GET http://localhost:8080/1.0/identifiers/did:ebsi:DfPaUdYwuzcqFoiMDSrUX8aQyZnr2SesH3vDVASYv8PE
	curl -X GET http://localhost:8080/1.0/identifiers/did:emtrust:0x242a5ac36676462bd58a
	curl -X GET http://localhost:8080/1.0/identifiers/did:meta:0000000000000000000000000000000000000000000000000000000000005e65
	curl -X GET http://localhost:8080/1.0/identifiers/did:tz:tz1YwA1FwpgLtc1G8DKbbZ6e6PTb1dQMRn5x
	curl -X GET http://localhost:8080/1.0/identifiers/did:pkh:tz:tz2BFTyPeYRzxd5aiBchbXN3WCZhx7BqbMBq
	curl -X GET http://localhost:8080/1.0/identifiers/did:orb:hl:uEiDI6sZJnp44sC_BG2XekWA4TN2m0Wtwv8AI71UER3vCHw:uoQ-BeEJpcGZzOi8vYmFma3JlaWdpNWxkZXRodTZoY3ljN3FpM214cGpjeWJ5anRvMm51bGxvYzc0YWNocGt1Y2VvNjZjZDQ:EiClaNSwhKSP6tQe36bYc5w41oRpCi1xv89MkUnNBm8agw

You can also use an "Accept" header to request the DID document in a specific representation, e.g.:

	curl -H "Accept: application/did+ld+json" https://dev.uniresolver.io/1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw
	curl -H "Accept: application/did+json" https://dev.uniresolver.io/1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw
	curl -H "Accept: application/did+cbor" https://dev.uniresolver.io/1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw

If this doesn't work, see [Troubleshooting](/docs/troubleshooting.md).

Note that there is also a [Universal Resolver frontend](https://github.com/decentralized-identity/universal-resolver-frontend/) that can optionally be installed separately.

## Drivers

Are you developing a DID method and Universal Resolver driver? Click [Driver Development](/docs/driver-development.md) for instructions.

| Driver Name | Driver Version | DID Method Spec Version | Docker Image or URL | Description |
| ----------- | -------------- | ----------------------- | ------------------- | ----------- |
| [did-btcr](https://github.com/decentralized-identity/uni-resolver-driver-did-btcr/) | 0.1-SNAPSHOT | [0.1](https://w3c-ccg.github.io/didm-btcr) | [universalresolver/driver-did-btcr](https://hub.docker.com/r/universalresolver/driver-did-btcr/) | Bitcoin Reference
| [did-sov](https://github.com/decentralized-identity/uni-resolver-driver-did-sov/) | 0.1-SNAPSHOT | [0.1](https://sovrin-foundation.github.io/sovrin/spec/did-method-spec-template.html) | [universalresolver/driver-did-sov](https://hub.docker.com/r/universalresolver/driver-did-sov/) | Sovrin public ledger
| [did-stack](https://github.com/decentralized-identity/uni-resolver-driver-did-stack/) | 0.1 | [1.0](https://github.com/blockstack/stacks-blockchain/blob/stacks-1.0/docs/blockstack-did-spec.md) | [universalresolver/driver-did-stack](https://hub.docker.com/r/universalresolver/driver-did-stack/)
| [did-dom](https://github.com/decentralized-identity/uni-resolver-driver-did-dom/) | 0.1-SNAPSHOT | (missing) | [universalresolver/driver-did-dom](https://hub.docker.com/r/universalresolver/driver-did-dom/)
| [did-ethr](https://github.com/uport-project/uport-did-driver) | 2.2.0 | [4.3.4](https://github.com/decentralized-identity/ethr-did-resolver/blob/master/doc/did-method-spec.md) | [uport/uni-resolver-driver-did-uport](https://hub.docker.com/r/uport/uni-resolver-driver-did-uport/) | Ethereum addresses or secp256k1 publicKeys
| [did-eosio](https://github.com/Gimly-Blockchain/eosio-did-universal-resolver-driver) | 0.1.3 | [0.1](https://github.com/Gimly-Blockchain/eosio-did-spec) | [gimlyblockchain/eosio-universal-resolver-driver](https://hub.docker.com/r/gimlyblockchain/eosio-universal-resolver-driver) | EOSIO blockchain platform
| [did-web](https://github.com/uport-project/uport-did-driver) | 2.2.0 | [3.0.0](https://w3c-ccg.github.io/did-method-web/) | [uport/uni-resolver-driver-did-uport](https://hub.docker.com/r/uport/uni-resolver-driver-did-uport/) | Domain name
| [did-v1](https://github.com/veres-one/uni-resolver-did-v1-driver) | 0.1 | [1.0](https://w3c-ccg.github.io/did-method-v1/) | [veresone/uni-resolver-did-v1-driver](https://hub.docker.com/r/veresone/uni-resolver-did-v1-driver) | Veres One Blockchain
| [did-jolo](https://github.com/jolocom/jolo-did-method/tree/master/jolocom-did-driver) | 0.1 | [0.1](https://github.com/jolocom/jolocom-did-driver/blob/master/jolocom-did-method-specification.md) | [jolocomgmbh/jolocom-did-driver](https://hub.docker.com/r/jolocomgmbh/jolocom-did-driver) | Jolocom identity management
| [did-hacera](https://github.com/hacera/hacera-did-driver) | 0.1 | (missing) | [hacera/hacera-did-driver](https://hub.docker.com/r/hacera/hacera-did-driver) | HACERA autonomous data exchange network
| [did-elem](https://github.com/decentralized-identity/element) | 0.1 | (missing) | | Experimental Sidetree protocol (Ethereum and IPFS)
| [did-seraphid](https://github.com/swisscom-blockchain/seraph-id-did-driver) | 0.1 | (missing) |  [swisscomblockchainag/seraph-id-did-driver](https://hub.docker.com/r/swisscomblockchainag/seraph-id-did-driver) | Seraph ID (SSI  solution on the NEO blockchain platform)
| [did-github](https://github.com/decentralized-identity/github-did) | 0.1 | (missing) | | Github
| [did-ccp](https://github.com/decentralized-identity/uni-resolver-driver-did-ccp/) | 0.1-SNAPSHOT | [0.1](https://did.baidu.com/did-spec/) | [hello2mao/driver-did-ccp](https://hub.docker.com/r/hello2mao/driver-did-ccp/) | Baidu Cloud
| [did-work](https://github.com/decentralized-identity/uni-resolver-driver-did-work/) | 0.2  | [1.0](https://workday.github.io/work-did-method-spec/) | [didwork/work-did-driver](https://hub.docker.com/r/didwork/work-did-driver)| Workday Credentials
| [did-ont](https://github.com/ontio/ontid-driver) | 0.1 | [1.0](https://github.com/ontio/ontology-DID/blob/master/docs/en/DID-ONT-method.md) |  [ontio/ontid-driver](https://hub.docker.com/r/ontio/ontid-driver) | Ontology ONT ID
| [did-kilt](https://github.com/KILTprotocol/kilt-did-driver) | 1.2.1 | [1.0](https://github.com/KILTprotocol/kilt-did-driver/blob/master/DID%20Method%20Specification.md) | [kiltprotocol/kilt-did-driver](https://hub.docker.com/r/kiltprotocol/kilt-did-driver)| KILT Protocol (SSI credentials)
| [did-evan](https://github.com/evannetwork/did-driver) | 0.1 | [0.9](https://github.com/evannetwork/evan.network-DID-method-specification/blob/master/evan_did_method_spec.md) | [evannetwork/evan-did-driver](https://hub.docker.com/r/evannetwork/evan-did-driver) | evan.network
| [did-factom](https://github.com/Sphereon-Opensource/uni-resolver-driver-did-factom) | 0.2.0-SNAPSHOT | [1.0](https://github.com/bi-foundation/FIS/blob/feature/DID/FIS/DID.md) | [sphereon/uni-resolver-driver-did-factom](https://hub.docker.com/r/sphereon/uni-resolver-driver-did-factom) | Factom Protocol
| [did-key](https://github.com/decentralized-identity/uni-resolver-driver-did-key) | 1.0.0 | [0.7](https://w3c-ccg.github.io/did-method-key/) | [universalresolver/driver-did-key](https://hub.docker.com/r/universalresolver/driver-did-key) | Public keys (in general)
| [did-io](https://github.com/iotexproject/uni-resolver-driver-did-io) | 0.1.0 | (missing) | [iotex/uni-resolver-driver-did-io](iotex/uni-resolver-driver-did-io:latest) | IoTeX Network
| [did-bba](https://github.com/blobaa/bba-did-driver) | 0.2.2 | [1.0](https://github.com/blobaa/bba-did-method-specification/blob/master/docs/markdown/spec.md) | [blobaa/bba-did-driver](https://hub.docker.com/repository/docker/blobaa/bba-did-driver) | Blobaa blockchain-based authentication on the Ardor blockchain
| [did-schema](https://github.com/51nodes/schema-registry-did-resolver) | 0.1.1 | [0.1](https://github.com/51nodes/schema-registry-did-method) | [51nodes/schema-registry-did-resolver](https://hub.docker.com/repository/docker/51nodes/schema-registry-did-resolver) | Identify and address schema definitions in a schema registry
| [did-ion](https://github.com/decentralized-identity/uni-resolver-driver-did-ion) | 0.8.1 | [0.1](https://github.com/decentralized-identity/ion) | [identityfoundation/driver-did-ion](https://hub.docker.com/r/identityfoundation/driver-did-ion) | ION network (Sidetree implementation on top of Bitcoin)
| [did-ace](https://github.com/aceblockID/aceblock-did-resolver)| 1.0 |(missing) | [aceblock/ace-did-driver](https://hub.docker.com/r/aceblock/ace-did-driver) | AceBlock blockchain framework
| [did-gatc](https://github.com/gataca-io/universal-resolver-driver) | 1.0.0 | [1.0 WD](https://github.com/gatacaid/gataca-did-method) | [gatacaid/universal-resolver-driver](https://hub.docker.com/r/gatacaid/universal-resolver-driver) | GATACA (blockchain-based digital identity platform)
| [did-icon-zzeung](https://github.com/amuyu/uni-resolver-driver-did-icon) | 0.1.2 | [1.0 WD](https://github.com/icon-project/icon-DID/blob/master/docs/ICON-DID-method.md) | [amuyu/driver-did-icon](https://hub.docker.com/r/amuyu/driver-did-icon) | ICON decentralized network
| [did-vaa](https://github.com/caict-develop-zhangbo/uni-resolver-driver-did-vaa)|1.0.0|[1.0 WD](https://github.com/caict-develop-zhangbo/vaa-method)|[caict/driver-did-vaa](https://hub.docker.com/repository/docker/caictdevelop/driver-did-vaa) | BIF blockchain
| [did-unisot](https://gitlab.com/unisot-did/unisot-did-driver)|1.0.0|[1.0.0](https://gitlab.com/unisot-did/unisot-did-method-specification)|[unisot/unisot-did-driver](https://hub.docker.com/r/unisot/unisot-did-driver) | UNISOT distributed identity system (atop Bitcoin SV blockchain)
| [did-sol](https://github.com/identity-com/sol-did)|1.0.0|[1.0.0](https://github.com/identity-com/sol-did/)|[identitydotcom/driver-did-sol](https://hub.docker.com/r/identitydotcom/driver-did-sol) | Solana blockchain
| [did-lit](https://github.com/ibct-dev/lit-resolver) | 0.1.1 | [0.1.1](https://github.com/ibct-dev/lit-DID/blob/main/docs/did:lit-method-spec_eng_v0.1.1.md) | [ibct/driver-did-lit](https://hub.docker.com/r/ibct/driver-did-lit) | LEDGIS blockchain
| [did-ebsi](https://api.preprod.ebsi.eu/docs/#/DID%20Registry) | 2.0.0 | 2.0.0 | [URL](https://api.preprod.ebsi.eu/did-registry/v2/identifiers/) | EBSI Platform (European Blockchain Services Infrastructure)
| [did-emtrust](https://github.com/Halialabs/did-spec) | 0.1| 0.1 | [halialabsdev/emtrust_did_driver](https://hub.docker.com/r/halialabsdev/emtrust_did_driver) | EmTrust WAI distributed identity system
| [did-meta](https://github.com/METADIUM/meta-DID/blob/master/doc/DID-method-metadium.md) | 1.0 | 1.0 | [URL](https://resolver.metadium.com/1.0/identifiers/) | Metadium Decentralized Identifiers
| [did-tz](https://github.com/spruceid/ssi/tree/main/did-tezos/) | 0.1.0 | [0.1](https://did-tezos.spruceid.com/) | [ghcr.io/spruceid/didkit-http](https://github.com/orgs/spruceid/packages/container/package/didkit-http) | Tezos DID method
| [did-pkh](https://github.com/spruceid/ssi/tree/main/did-pkh/) | 0.0.1 | [0.1](https://github.com/spruceid/ssi/blob/main/did-pkh/did-pkh-method-draft.md) | [ghcr.io/spruceid/didkit-http](https://github.com/orgs/spruceid/packages/container/package/didkit-http) | Public Key Hash DID method
| [did-orb](https://github.com/trustbloc/orb/releases/tag/v0.1.3) | 0.1.3 | [0.2](https://trustbloc.github.io/did-method-orb/) | [trustbloc/orb-did-driver](https://github.com/orgs/trustbloc-cicd/packages/container/package/orb-did-driver) | 


## More Information

 * [Driver Development](/docs/driver-development.md)
 * [Continuous Integration and Delivery](/docs/continuous-integration-and-delivery.md)
 * [Development System](/docs/dev-system.md)
 * [Branching Strategy](/docs/branching-strategy.md)
 * [Design Goals](/docs/design-goals.md)
 * [Troubleshooting](/docs/troubleshooting.md)
 * [Java Components](/docs/java-components.md)

## About

<img align="left" src="https://raw.githubusercontent.com/decentralized-identity/universal-resolver/main/docs/logo-dif.png" width="115">

Decentralized Identity Foundation - https://identity.foundation/

<br clear="left" />

<img align="left" src="https://raw.githubusercontent.com/decentralized-identity/universal-resolver/main/docs/logo-ngi0pet.png" width="115">

Supported by [NLnet](https://nlnet.nl/) and [NGI0 PET](https://nlnet.nl/PET/#NGI), which is made possible with financial support from the European Commission's [Next Generation Internet](https://ngi.eu/) programme.
