![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/decentralized-identity.github.io/master/images/logo-small.png)

# Universal Resolver

A Universal Resolver is an identifier resolver that works with any decentralized identifier system, including Decentralized Identifiers (DIDs).

See this [blog post](https://medium.com/decentralized-identity/a-universal-resolver-for-self-sovereign-identifiers-48e6b4a5cc3c) for an introduction.

See https://uniresolver.io/ for a publicly hosted instance of a Universal Resolver.

## Implementations

The following implementations are available for use:

 * [Java](https://github.com/decentralized-identity/universal-resolver-java)
 * [Python3](https://github.com/decentralized-identity/universal-resolver-python)

## Drivers

| Driver Name | Driver Version | DID Spec Version | DID Method Spec Version | Docker Image |
| ----------- | -------------- | ---------------- | ----------------------- | ------------ |
| [did-btcr](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/btcr/) | 0.1-SNAPSHOT | [0.7](https://w3c-ccg.github.io/did-spec/) | [0.1](https://w3c-ccg.github.io/didm-btcr) | [universalresolver/driver-did-btcr](https://hub.docker.com/r/universalresolver/driver-did-btcr/)
| [did-sov](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/sov/) | 0.1-SNAPSHOT | [0.7](https://w3c-ccg.github.io/did-spec/) | [0.1](https://github.com/mikelodder7/sovrin/blob/master/spec/did-method-spec-template.html) | [universalresolver/driver-did-sov](https://hub.docker.com/r/universalresolver/driver-did-sov/)
| [did-erc725](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/erc725/) | 0.1-SNAPSHOT | [0.7](https://w3c-ccg.github.io/did-spec/) | [0.1](https://github.com/WebOfTrustInfo/rebooting-the-web-of-trust-spring2018/blob/master/topics-and-advance-readings/DID-Method-erc725.md) | [universalresolver/driver-did-erc725](https://hub.docker.com/r/universalresolver/driver-did-erc725/)
| [did-stack](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/stack/) | 0.1 | [0.7](https://w3c-ccg.github.io/did-spec/) | (missing) | [universalresolver/driver-did-stack](https://hub.docker.com/r/universalresolver/driver-did-stack/)
| [did-dom](https://github.com/decentralized-identity/universal-resolver/tree/master/drivers/dom/) | 0.1-SNAPSHOT | [0.7](https://w3c-ccg.github.io/did-spec/) | (missing) | [universalresolver/driver-did-dom](https://hub.docker.com/r/universalresolver/driver-did-dom/)
| [did-uport](https://github.com/uport-project/uport-did-driver) | 1.1.0 | [0.7](https://w3c-ccg.github.io/did-spec/) | [1.0](https://docs.google.com/document/d/1vS6UBUDwxYR8tLTNo4HUhGe2qb9Q95QLiJTt9NkwZ8M/) | [uport/uni-resolver-driver-did-uport](https://hub.docker.com/r/uport/uni-resolver-driver-did-uport/)
| did-v1 |  | [0.7](https://w3c-ccg.github.io/did-spec/) | [1.0](https://w3c-ccg.github.io/didm-veres-one/) |
| did-ipid |  | [0.7](https://w3c-ccg.github.io/did-spec/) | [0.1](https://github.com/jonnycrunch/ipid) |

## More Information

 * [Design Goals](/docs/design-goals.md)
 * [Architecture and Drivers](/docs/architecture-drivers.md)
 * [API Definition](/docs/api-definition.md)

## About

Decentralized Identity Foundation - http://identity.foundation/
 