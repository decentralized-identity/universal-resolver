![DIF Logo](https://github.com/decentralized-identity/universal-resolver/blob/master/implementations/java/logo-dif.png?raw=true)

# Universal Resolver

The Universal Resolver is an identifier resolver that works with any decentralized identifier system.

See this [blog post](https://medium.com/decentralized-identity/a-universal-resolver-for-self-sovereign-identifiers-48e6b4a5cc3c) for an introduction.

## Implementations

The following implementations are available for use:

 * [Java](/implementations/java)
 * [Python3](/implementations/Python3)

## Drivers

| Driver Name | Driver Version | DID Spec Version | DID Method Spec Version | Docker Image |
| ----------- | -------------- | ---------------- | ----------------------- | ------------ |
| did-btcr | [0.1-SNAPSHOT](https://github.com/peacekeeper/universal-resolver/tree/master/implementations/java/driver-did-btcr) | [0.7](https://w3c-ccg.github.io/did-spec/) | [0.1](https://github.com/WebOfTrustInfo/rebooting-the-web-of-trust-fall2017/blob/master/topics-and-advance-readings/btcr-dids-ddos.md) | [danubetech/uni-resolver-driver-did-btcr](https://hub.docker.com/r/danubetech/uni-resolver-driver-did-btcr/)
| did-sov | [0.1-SNAPSHOT](https://github.com/peacekeeper/universal-resolver/tree/master/implementations/java/driver-did-sov) | [0.7](https://w3c-ccg.github.io/did-spec/) | [0.1](https://github.com/mikelodder7/sovrin/blob/master/spec/did-method-spec-template.html) | [danubetech/uni-resolver-driver-did-sov](https://hub.docker.com/r/danubetech/uni-resolver-driver-did-sov/)
| did-stack | [0.1](https://github.com/peacekeeper/universal-resolver/tree/master/implementations/java/driver-did-stack) | [0.7](https://w3c-ccg.github.io/did-spec/) |  |
| did-uport | [1.1.0](https://github.com/uport-project/uport-did-driver) | [0.7](https://w3c-ccg.github.io/did-spec/) | [1.0](https://docs.google.com/document/d/1vS6UBUDwxYR8tLTNo4HUhGe2qb9Q95QLiJTt9NkwZ8M/) | [uport/uni-resolver-driver-did-uport](https://hub.docker.com/r/uport/uni-resolver-driver-did-uport/)
| did-v1 |  | [0.7](https://w3c-ccg.github.io/did-spec/) | [1.0](https://w3c-ccg.github.io/didm-veres-one/) |
| did-ipid |  | [0.7](https://w3c-ccg.github.io/did-spec/) | [0.1](https://github.com/jonnycrunch/ipid) |

## More Information

 * [Design Goals](/docs/design-goals.md)
 * [Architecture and Drivers](/docs/architecture-drivers.md)
 * [API Definition](/docs/api-definition.md)
