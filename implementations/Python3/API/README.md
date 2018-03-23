This is a simple API Library implementaation built for Python3 with backwards compatibility for python2.x. It is intended to make a simple http request to a web service. The web service will handle the resolving of the DID and will return a DDO (DID Document) through the HTTP response.

implementation: assign a config file with a list of URLs pointing to a resolver web service. This is done, by creating a UniversalUriResolver object and assign it a string with the name of the file.

```python
uri = ClientUriResolver("Config.txt")
```

Now you can resolve DIDs by passing them as a string argument into the resolver method

```python
ddo = uri.resolver("did:sov:WRfXPg8dantKVubE3HX8pw")
```

This returns back the DDO in the form of a JSON object. If it fails to resolve using all of the provided web services, it will return a None object.

example to print a DDO:

```python
webResolver = ClientWebResolver("config.txt")
response = webResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw")
print(response)
```

response will look like this:
```javascript
{'control': [], 
    'owner': {
        'curve': 'ed25519', 
        'publicKeyBase64': 'H3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV', 
        'type': ['CryptographicKey', 'EdDsaSAPublicKey'], 
        'id': 'did:sov:WRfXPg8dantKVubE3HX8pw'
    }, 
    'id': 'did:sov:WRfXPg8dantKVubE3HX8pw', 
    'service': {
            'xdi': 'http://127.0.0.1:8080/xdi'
    }, 
    '@context': 'https://example.org/did/v1'
}
```
