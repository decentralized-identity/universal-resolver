# Universal Resolver - API Definition

## Interface Specification

The UR will offer the following operations and input/output parameters:

### resolve()

#### Input

* **id:** The identifier to be resolved, i.e. semantic name, DID, etc.
* **resulttype** (optional): The UR user may be interested in different types of results. Not all result types may be supported by all identifier system plugins. Possible result types:
    * The raw descriptor object associated with an identifier (e.g. DID Document, BNS zone file).
    * The DID that a semantic name maps to.
    * The current public key associated with an identifier.
A certain service endpoint from the descriptor object ("routing").
* **query (optional):** This parameter could be used to further narrow down the result, e.g. which service endpoint should be selected from a DID Document (the Hub endpoint, OpenID endpoint, XDI endpoint, etc.).
* **hint (optional):** One or more hint parameters could indicate to the UR certain preferences for the resolution process, e.g. with regard to the "Identifier Ambiguity" and "Caching Behavior" questions mentioned above.

#### Output

* **result:** Depending on the resulttype, this is the resolved DID Document, BNS zone file, DID, public key, service endpoint, etc.
* **supplementary (optional):** Various metadata about the resolution process, e.g. is this a cached result, has the result been obtained from a local full node or a local thin client or a remote API, has a signature on a DID Document been found and verified, etc.

The `supplementary` field is for forward compatibility with future systems.  This is a field a service client can use to return something service-specific to clients (for example, Blockstack and ENS might want to give back the relevant transaction ID that created the DID).  If it is discovered that each client returns the same types of data in their `supplementary` fields, then we will standardize the response in a future version of this specification.

Schema (NOTE: missing precise DID Document fields)

```
    {
       'type': 'object',
       'properties': {
          'version': {
             'type': 'string',
             'pattern': '^1\.0$',
          },
          'ddo': {
             'type': 'object',
             'properties': {
                '...'
             }
             'required': [
                '@context',
                'id',
                'signature',
             ],
          },
          'supplementary': {
             'type': 'object',
             'additionalProperties': true,
          },
       },
       'additionalProperties': false,
    }           
```

Examples

```
    {
       'version': '1.0',
       'ddo': {
           '@context': '...',
           'id': 'did:sov:21tDAKCERh95uGgKbJNHYp',
           'equiv-id': [
              'did:sov:33ad7beb1abc4a26b89246',
              'did:uport:2opT3phRXKtkaqjv6LAyR9pqkVwADVECZwx',
              'did:bstk:479395-27'
           ],
           'verkey': 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCABMC',
           'control': [
              'self',
              'did:sov:21tDAKCERh95uGgKbJNHYp',
           ],
           'service': {
              'dif': 'https://dif.microsoft.com',
              'blockstack': 'https://explorer.blockstack.org',
              'ens': 'https://etherscan.io/enslookup',
              'openid': 'https://vicci.org/id',
           },
           'type': 'https://schema.org/Person',
           'creator': 'did:sov:21tDAKCERh95uGgKbJNHYp',
           'created': '2016-10-10T17:00:00Z',
           'updated': '2017-03-14T18:00:30Z',
           'signature': {
              'type': 'LinkedDataSignature2015',
              'created': '2016-02-08T16:02:20Z',
              'creator': 'did:sov:21tDAKCERh95uGgKbJNHYp/keys/1',
              'signatureValue': 'QNB13Y7Q9oLlDLL6AHyL31OE5fLji9DwJSA8qnv81oRaKonij8m+Jv4XdiEYvJ97iRlzKU/92/0LafSL5JftEgl960DLcbqMFxOtbAmFOIMa7eDcrgTL5ytXeYCYKLjHQG3s8a3UKDKRuEK54qK1G5hGKGoLgAVa6xgcDLjW7M19PEJV/c3HpGA7Eez6VFMoTt4yESjZvOXC97xN3KpshOx2HT/btgUbo0XjA1Oi0QHdgrLcUsQGt6w23RjeSToalrsA1G69OFeN2OiQrz9Jb4561hvKLSyWObwRmS6n5Vgr5xkvUm6MONRq0Vg33kXevoVM64KTBkISul61tzjn4w==',
           },
        },
        'supplementary': {
           'whois': {
              "block_preordered_at": 373622, 
              "block_renewed_at": 373622, 
              "expire_block": 489247, 
              "has_zonefile": true, 
              "last_transaction_height": 422657, 
              "last_transaction_id": "5e4c0a42874ac59a9e21d351ec8b06971231ed2c02e0bac61b7dcc8ee8cb1ad2", 
              "owner_address": "16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg", 
              "owner_script": "76a914395f3643cea07ec4eec73b4d9a973dcce56b9bf188ac", 
              "zonefile_hash": "4d718e536b57c2dcc556cf2cdbadf1b6647ead80"
           },
           'zonefile': "$ORIGIN judecn.id\n$TTL 3600\npubkey TXT \"pubkey:data:04cabba0b5b9a871dbaa11c044066e281c5feb57243c7d2a452f06a0d708613a46ced59f9f806e601b3353931d1e4a98d7040127f31016311050bedc0d4f1f62ff\"\n_file URI 10 1 \"file:///home/jude/.blockstack/storage-disk/mutable/judecn.id\"\n_https._tcp URI 10 1 \"https://blockstack.s3.amazonaws.com/judecn.id\"\n_http._tcp URI 10 1 \"http://node.blockstack.org:6264/RPC2#judecn.id\"\n_dht._udp URI 10 1 \"dht+udp://fc4d9c1481a6349fe99f0e3dd7261d67b23dadc5\"\n"
       },
    }       
```

## Web API

The UR includes an implementation of a Web API. This exposes the UR's **resolve()** operation via an HTTP API that can answer resolution requests using standard HTTP GET requests. The required input parameter(s) for the **resolve()** operation - i.e. the identifier to be resolved - can be passed as part of the URL, e.g.:

<pre>
http://localhost:8080/1.0/identifiers/<b>did:sov:21tDAKCERh95uGgKbJNHYp</b>
http://localhost:8080/1.0/identifiers/<b>did:btcr:xkrn-xzcr-qqlv-j6sl</b>
</pre>

For additional input parameters such as **resulttype**, we need to specify:

* How to set their values in a request to the web proxy (e.g. using query string parameters or HTTP headers).
* What are their default values (e.g. the default result type, or the default service endpoint).

Depending on the result type, the web proxy may behave in different ways:

* Send the result (e.g. DID Document) in the response body with an appropriate MIME type.
* If the result is a single service endpoint URI, send an HTTP redirect (maybe HTTP 307? [debatable]).

## Protocol

![Protocol](/docs/figures/protocol.png)

The Universal Resolver relies on its drivers to resolve
fully-qualified names or DIDs into DID Documents.  It does this simply by forwarding requests it
receives to the respective driver, and caching the response.  **The HTTP headers in
the request will be used to determine whether or not to check the cache, and for
how long to cache the DID Document response.**

The service orchestrator will forward the request to **at most one driver**.  It will choose which one based on the suffix of the name (see below).

## Resolver Interface

![System Architecture](/docs/figures/overview.png)

A single endpoint for the Universal Resolver is defined:  `GET /:versionString/identifiers/:fullyQualifiedNameOrDID`

* `:versionString` is the resolver API version.  For now, this is `1.0`.
* `:fullyQualifiedNameOrDID` is the fully-qualified name or DID to query.  This includes any/all indications of things like which system the name lives in, which blockchain the name is registered on, which namespace it lives in, and so on.

Examples:

* `GET /1.0/identifiers/judecn.id.bsk` resolves `judecn.id` in Blockstack’s virtualchain to a DID Document.
* `GET /1.0/identifiers/nickjohnson.eth.ens` resolves `nickjohnson.eth` using ENS to a DID Document.
* `GET /1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw` resolves the DID Document for `did:sov:WRfXPg8dantKVubE3HX8pw` using Sovrin (`sov`).

### DIDs

A DID is a string that starts with `did:`.  If `:fullyQualifiedNameOrDID` starts
with `did:`, it will be treated as a DID _even if it is also a well-formed
name_.  The reason for this is to discourage the use of names that have an
ambiguous interpretation.

### Fully-Qualified Names

Each name _must_ end in a '.' followed by a URL-safe suffix.  This suffix identifies the service that can resolve
the name.  This is similar in function to the method ID in a DID.  Anyone can
claim an identifier, provided that it is unique.

**Names _may not_ start with `did:`.**  This prefix is reserved for DIDs.

A name _must_ resolve to at most one DID Document.  The service client _may_ do so by
first resolving the name to a DID, and then resolving the DID to a DID Document.

Identifiers in use today:

* `.bsk`: for Blockstack-hosted names
* `.ens`: for ENS-hosted names
* ... (insert yours here)

## Driver Interface

The driver interface has two endpoints, one for fully-qualified names
and one for DIDs.  The reason for two endpoints is that the service client _must
not_ be responsible for disambiguating an identifier string.  This
responsibility belongs only to the resolver.

The driver makes these endpoints available via HTTP. The two endpoints are:

* A `names` endpoint: `GET /:versionString/names/:fullyQualifiedName`
* A `dids` endpoint: `GET /:versionString/dids/:DID`

A driver _must_ implement at least one endpoint.  However, if it does not
implement an endpoint, it _must_ handle requests to it by returning `HTTP 501`.

Both endpoints return at most one DID Document.  Implementations are encouraged, but not
required, to resolve the `:fullyQualifiedName` to a DID and then resolve the DID
to the DID Document.
