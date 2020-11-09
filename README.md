![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-resolver/master/docs/logo-dif.png)

# Universal Resolver

A Universal Resolver is an identifier resolver that works with any decentralized identifier system, including Decentralized Identifiers (DIDs).

See this [blog post](https://medium.com/decentralized-identity/a-universal-resolver-for-self-sovereign-identifiers-48e6b4a5cc3c) and this [webinar](https://ssimeetup.org/did-resolution-given-did-how-do-retrieve-document-markus-sabadello-webinar-13/) for an introduction.

See https://resolver.identity.foundation/ for a stable version or https://dev.uniresolver.io/ for a test instance of a Universal Resolver.

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
	curl -X GET http://localhost:8080/1.0/identifiers/did:ipid:QmYA7p467t4BGgBL4NmyHtsXMoPrYH9b3kSG6dbgFYskJm
	curl -X GET http://localhost:8080/1.0/identifiers/did:web:data-vault.eu:u:zi4xgcHtYo7fh_KAURaO1beG2v6WN9ImVnQ30CBDEJjLrw
	curl -X GET http://localhost:8080/1.0/identifiers/did:ethr:mainnet:0x3b0BC51Ab9De1e5B7B6E34E5b960285805C41736
	curl -X GET http://localhost:8080/1.0/identifiers/did:ethr:0x02b97c30de767f084ce3080168ee293053ba33b235d7116a3263d29f1450936b71
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
	curl -X GET http://localhost:8080/1.0/identifiers/did:kilt:5CqJa4Ct7oMeMESzehTiN9fwYdGLd7tqeirRMpGDh2XxYYyx
	curl -X GET http://localhost:8080/1.0/identifiers/did:evan:testcore:0x126E901F6F408f5E260d95c62E7c73D9B60fd734
	curl -X GET http://localhost:8080/1.0/identifiers/did:echo:1.1.25.0
	curl -X GET http://localhost:8080/1.0/identifiers/did:factom:testnet:6aa7d4afe4932885b5b6e93accb5f4f6c14bd1827733e05e3324ae392c0b2764
	curl -X GET http://localhost:8080/1.0/identifiers/did:dock:5FXqofpV7dsuki925U1dSzDvBuQbaci5yWTQGVWRQ7bdQP5p
	curl -X GET http://localhost:8080/1.0/identifiers/did:abt:z116ygT18P67xBp3scBtZLU6xVoDy268bgnY
	curl -X GET http://localhost:8080/1.0/identifiers/did:trustbloc:testnet.trustbloc.dev:EiBh6SP9nw2W-1Z9Y9KB0-GZ8hvtYxFgE5oeSVbTmbpXmw
	curl -X GET http://localhost:8080/1.0/identifiers/did:sirius:2VhYrbauc2cCx9ZpCp5wrDtK7HKf7jrsvgoKBD4KgK
	curl -X GET http://localhost:8080/1.0/identifiers/did:mpg:7PGGnRdvKKFftSXU3Jw75Vk5npfg
	curl -X GET http://localhost:8080/1.0/identifiers/did:trust:cert.EiBJ6qjVXgJ-A8xnaUiu4rtLDgeobQYgRWjMV49aCak4HQ
	curl -X GET http://localhost:8080/1.0/identifiers/did:io:0x476c81C27036D05cB5ebfe30ae58C23351a61C4A
    curl -X GET http://localhost:8080/1.0/identifiers/did:bba:t:45e6df15dc0a7d91dcccd24fda3b52c3983a214fb0eed0938321c11ec99403cf
	curl -X GET http://localhost:8080/1.0/identifiers/did:cy:2nnn7H7RJLLhFPoGyzxPCLzuhrzJ
	curl -X GET http://localhost:8080/1.0/identifiers/did:bid:6cc796b8d6e2fbebc9b3cf9e
	curl -X GET http://localhost:8080/1.0/identifiers/did:schema:public-ipfs:json-schema:Qma2beXKwZeiUXcaRaQKwbBV1TqyiJnsMTYExUTdQue43J
	curl -X GET http://localhost:8080/1.0/identifiers/did:ion:EiC5-1uBg-YC2DvQRbI6eihDvk7DOYaQ08OB0I3jCe9Ydg:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoiYW55U2lnbmluZ0tleUlkIiwiandrIjp7ImNydiI6InNlY3AyNTZrMSIsImt0eSI6IkVDIiwieCI6ImFHc01HMHU5Rlg2STU0cGVJS3FZb2tqblFQR2hMVVlUT1FOYzNuT3ZFMVEiLCJ5IjoiZmppbHFoZVdRWWtITkU3MHNoTVJ5TURyWnA4RUdDZkVfYUwzaC15Sm1RQSJ9LCJwdXJwb3NlIjpbImF1dGgiLCJnZW5lcmFsIl0sInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkifV0sInNlcnZpY2VfZW5kcG9pbnRzIjpbeyJlbmRwb2ludCI6Imh0dHA6Ly9hbnkuZW5kcG9pbnQiLCJpZCI6ImFueVNlcnZpY2VFbmRwb2ludElkIiwidHlwZSI6ImFueVR5cGUifV19fV0sInVwZGF0ZV9jb21taXRtZW50IjoiRWlERkM2RE9Ed0JNeG5kX19oMTFSeDRObjFlOHpubFlPUjJhLVBqeUNva2NGZyJ9LCJzdWZmaXhfZGF0YSI6eyJkZWx0YV9oYXNoIjoiRWlBbExNMC1qem1DWi1FcElVZ0laQ2piWk5yMDFfVVBMbnd5MHdfT3I0Rks0dyIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUJDNGhTMVVHeVNnTmYzbWFMdnNKRUpxX05aQUlKa0pndTNKMTJMeGNESE93In19
    curl -X GET http://localhost:8080/1.0/identifiers/did:ace:0xf81c16a78b257c10fddf87ed4324d433317169a005ddf36a3a1ba937ba9788e3


If this doesn't work, see [Troubleshooting](/docs/troubleshooting.md).

## Drivers

Are you developing a DID method and Universal Resolver driver? Click [Driver Development](/docs/driver-development.md) for instructions.

| Driver Name | Driver Version | DID Spec Version | DID Method Spec Version | Docker Image |
| ----------- | -------------- | ---------------- | ----------------------- | ------------ |
| [did-abt](https://github.com/ArcBlock/uni-resolver-driver-did-abt) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://arcblock.github.io/abt-did-spec/) | [arcblock/driver-did-abt](https://hub.docker.com/repository/docker/arcblock/driver-did-abt)  |
| [did-btcr](https://github.com/decentralized-identity/uni-resolver-driver-did-btcr/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://w3c-ccg.github.io/didm-btcr) | [universalresolver/driver-did-btcr](https://hub.docker.com/r/universalresolver/driver-did-btcr/)
| [did-sov](https://github.com/decentralized-identity/uni-resolver-driver-did-sov/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://sovrin-foundation.github.io/sovrin/spec/did-method-spec-template.html) | [universalresolver/driver-did-sov](https://hub.docker.com/r/universalresolver/driver-did-sov/)
| [did-erc725](https://github.com/decentralized-identity/uni-resolver-driver-did-erc725/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/WebOfTrustInfo/rebooting-the-web-of-trust-spring2018/blob/master/topics-and-advance-readings/DID-Method-erc725.md) | [universalresolver/driver-did-erc725](https://hub.docker.com/r/universalresolver/driver-did-erc725/)
| [did-stack](https://github.com/decentralized-identity/uni-resolver-driver-did-stack/) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | [universalresolver/driver-did-stack](https://hub.docker.com/r/universalresolver/driver-did-stack/)
| [did-dom](https://github.com/decentralized-identity/uni-resolver-driver-did-dom/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | [universalresolver/driver-did-dom](https://hub.docker.com/r/universalresolver/driver-did-dom/)
| [did-ethr](https://github.com/uport-project/uport-did-driver) | 1.4.0 | [1.0 WD](https://w3c.github.io/did-core/) | [3.0.0](https://github.com/decentralized-identity/ethr-did-resolver/blob/master/doc/did-method-spec.md) | [uport/uni-resolver-driver-did-uport](https://hub.docker.com/r/uport/uni-resolver-driver-did-uport/)
| [did-web](https://github.com/uport-project/uport-did-driver) | 1.4.0 | [1.0 WD](https://w3c.github.io/did-core/) | [3.0.0](https://w3c-ccg.github.io/did-method-web/) | [uport/uni-resolver-driver-did-uport](https://hub.docker.com/r/uport/uni-resolver-driver-did-uport/)
| [did-v1](https://github.com/veres-one/uni-resolver-did-v1-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://w3c-ccg.github.io/did-method-v1/) | [veresone/uni-resolver-did-v1-driver](https://hub.docker.com/r/veresone/uni-resolver-did-v1-driver)
| [did-key](https://github.com/veres-one/uni-resolver-did-v1-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [0.7](https://w3c-ccg.github.io/did-method-key/) | [veresone/uni-resolver-did-v1-driver](https://hub.docker.com/r/veresone/uni-resolver-did-v1-driver)
| did-ipid |  | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/jonnycrunch/ipid) |
| [did-jolo](https://github.com/jolocom/jolo-did-method/tree/master/jolocom-did-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/jolocom/jolocom-did-driver/blob/master/jolocom-did-method-specification.md) | [jolocomgmbh/jolocom-did-driver](https://hub.docker.com/r/jolocomgmbh/jolocom-did-driver) |
| [did-hacera](https://github.com/hacera/hacera-did-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | [hacera/hacera-did-driver](https://hub.docker.com/r/hacera/hacera-did-driver) |
| [did-elem](https://github.com/decentralized-identity/element) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | |
| [did-seraphid](https://github.com/swisscom-blockchain/seraph-id-did-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) |  [swisscomblockchainag/seraph-id-did-driver](https://hub.docker.com/r/swisscomblockchainag/seraph-id-did-driver) |
| [did-github](https://github.com/decentralized-identity/github-did) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | |
| [did-ccp](https://github.com/decentralized-identity/uni-resolver-driver-did-ccp/) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://did.baidu.com/did-spec/) | [hello2mao/driver-did-ccp](https://hub.docker.com/r/hello2mao/driver-did-ccp/)
| [did-work](https://github.com/decentralized-identity/uni-resolver-driver-did-work/) | 0.2  | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://workday.github.io/work-did-method-spec/) | [didwork/work-did-driver](https://hub.docker.com/r/didwork/work-did-driver)|
| [did-ont](https://github.com/ontio/ontid-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://github.com/ontio/ontology-DID/blob/master/docs/en/DID-ONT-method.md) |  [ontio/ontid-driver](https://hub.docker.com/r/ontio/ontid-driver) |
| [did-kilt](https://github.com/KILTprotocol/kilt-did-driver) | 1.2.1 | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://github.com/KILTprotocol/kilt-did-driver/blob/master/DID%20Method%20Specification.md) | [kiltprotocol/kilt-did-driver](https://hub.docker.com/r/kiltprotocol/kilt-did-driver)|
| [did-evan](https://github.com/evannetwork/did-driver) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [0.9](https://github.com/evannetwork/evan.network-DID-method-specification/blob/master/evan_did_method_spec.md) | [evannetwork/evan-did-driver](https://hub.docker.com/r/evannetwork/evan-did-driver) |
| [did-echo](https://github.com/echoprotocol/uni-resolver-driver-did-echo) | 0.0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://github.com/echoprotocol/uni-resolver-driver-did-echo/blob/master/echo_did_specifications.md) | [echoprotocol/uni-resolver-driver-did-echo](https://hub.docker.com/r/echoprotocol/uni-resolver-driver-did-echo) |
| [did-factom](https://github.com/Sphereon-Opensource/driver-did-factom) | 0.1-SNAPSHOT | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://github.com/bi-foundation/FIS/blob/feature/DID/FIS/DID.md) | [sphereon/driver-did-factom](https://hub.docker.com/r/sphereon/driver-did-factom) |
| [did-key](https://github.com/decentralized-identity/uni-resolver-driver-did-key) | 1.0.0 | [1.0 WD](https://w3c.github.io/did-core/) | [0.7](https://w3c-ccg.github.io/did-method-key/) | [universalresolver/driver-did-key](https://hub.docker.com/r/universalresolver/driver-did-key) |
| [did-dock](https://github.com/docknetwork/dock-did-driver) | 0.1.0 | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/docknetwork/dock-did-driver/blob/master/Dock%20DID%20method%20specification.md) | [docknetwork/dock-did-driver](https://hub.docker.com/r/docknetwork/dock-did-driver) |
| [did-trustbloc](https://github.com/trustbloc/trustbloc-did-method) | 0.1.4 | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/trustbloc/trustbloc-did-method/blob/v0.1.4/docs/spec/trustbloc-did-method.md) | [trustbloc/driver-did-trustbloc](https://github.com/trustbloc/trustbloc-did-method/packages/212043)
| [did-sirius](https://gitlab.com/proximax-enterprise/siriusid/tsjs-did-sirius-id-driver) | 0.0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [0.2](https://gitlab.com/proximax-enterprise/siriusid/sirius-id-specs/-/blob/master/docs/did-method-spec.md) | [proximax-enterprise/tsjs-did-sirius-id-driver](https://hub.docker.com/r/proximax/tsjs-did-sirius-id-driver)
| [did-trust](https://github.com/bjwswang/uni-resolver-driver-did-trust) | 0.1 | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](http://did.360.cn/doc/) | [bjwswang/driver-did-trust](https://hub.docker.com/repository/docker/bjwswang/driver-did-trust)
| [did-io](https://github.com/iotexproject/uni-resolver-driver-did-io) | 0.1.0 | [1.0 WD](https://w3c.github.io/did-core/) | (missing) | [iotex/uni-resolver-driver-did-io](iotex/uni-resolver-driver-did-io:latest)
| [did-bba](https://github.com/blobaa/bba-did-driver) | 0.2.0 | [1.0 WD](https://w3c.github.io/did-core/) | [1.0](https://github.com/blobaa/bba-did-method-specification/blob/master/docs/markdown/spec.md) | [blobaa/bba-did-driver](https://hub.docker.com/repository/docker/blobaa/bba-did-driver)
| [did-cy] | 0.1.0 | [1.0 WD](https://w3c.github.io/did-core/) |  | [](chainyard/driver-did-cy:latest)
| [did-bid](https://github.com/teleinfo-bif/bid-resolver) | 1.0.0 | [1.0 WD](https://w3c.github.io/did-core/) | [1.0.1](https://github.com/teleinfo-bif/bid/tree/master/doc/en) | [teleinfo/driver-did-bid](https://hub.docker.com/repository/docker/teleinfo/driver-did-bid)
| [did-schema](https://github.com/51nodes/schema-registry-did-resolver) | 0.1.1 | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/51nodes/schema-registry-did-method) | [51nodes/schema-registry-did-resolver](https://hub.docker.com/repository/docker/51nodes/schema-registry-did-resolver)  
| [did-ion](https://github.com/decentralized-identity/uni-resolver-driver-did-ion) | 0.8.1 | [1.0 WD](https://w3c.github.io/did-core/) | [0.1](https://github.com/decentralized-identity/ion) | [identityfoundation/driver-did-ion](https://hub.docker.com/r/identityfoundation/driver-did-ion)
| [did-ace](https://github.com/aceblockID/aceblock-did-resolver)| 1.0 | [1.0 WD](https://w3c.github.io/did-core/) |(missing) | [aceblock/ace-did-driver](https://hub.docker.com/r/aceblock/ace-did-driver)

## More Information

 * [Driver Development](/docs/driver-development.md)
 * [Continuous Integration and Delivery](/docs/continuous-integration-and-delivery.md)
 * [Development System](/docs/dev-system.md)
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
