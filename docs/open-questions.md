# Universal Resolver - Open Questions

## Identifier Ambiguity

How can the UR determine which identifier system should be used to resolve a given identifier?  This is also known as the "multiple namespaces problem". E.g. the identifier markus.eth could be a semantic name registered in ENS, but it could also be registered in BNS. If one objective for the UR is extensibility, then ambiguity may be inevitable. In addition, some identifier systems may offer multiple deployed instances such as a "mainnet" and a "testnet".

The UR should implement some logic (we can use the term "identifier system selector") to make sure the outcome of every resolution request is deterministic, based on

1. Community consensus and recommendations by the IND WG.
2. Custom configuration by the UR user.
3. Input parameter by the UR user.

## Reverse Lookup

Should reverse lookup be supported, e.g. find semantic names given a DID?

## Identifier Discovery

Should search for identifiers be supported, e.g. return a list of all identifiers that satisfy certain criteria? This could involve the use of higher-level functionality such as the DIF Hub protocols or Blockstack storage layer.

## Local Names

Should local names be supported? This would imply some kind of local state or context to be held by the UR user, such as an address book.

## Caching Behavior

Should resolution results be cached? This could be determined by a combination of
Community consensus and recommendations by the IND WG.
Custom configuration by the UR user.
Input parameter by the UR user.
Contents of the identifier's descriptor object (DDO, BNS zone file).
