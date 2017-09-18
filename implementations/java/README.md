![DIF Logo](https://github.com/decentralized-identity/universal-resolver/blob/master/implementations/java/logo-dif.png?raw=true)

### Information

This is a work-in-progress Java implementation of a Universal Resolver (aka Community Resolver) to be used for a decentralized naming system. It includes core resolution logic, a web API, a client library, and drivers for the **did:sov** and **did:btcr** methods.

See the [specifications](https://github.com/decentralized-identity/universal-resolver/blob/master/docs/api-documentation.md) for more information.

Incomplete implementation! Not ready for production use! Use at your own risk! Pull requests welcome.

### Quick Start

Try the following:

	curl -X GET  https://uniresolver.danubetech.com/1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw
	curl -X GET  https://uniresolver.danubetech.com/1.0/identifiers/did:btcr:xkrn-xzcr-qqlv-j6sl

See the [Examples](https://github.com/decentralized-identity/universal-resolver/tree/master/implementations/java/examples/src/main/java/uniresolver/examples/).

### Build

In order to build the **did:sov** driver, you first need to build [libindy-sdk](https://github.com/hyperledger/indy-sdk/) and its [Java wrapper](https://github.com/hyperledger/indy-sdk/tree/master/wrappers/java).

In order to build the **did:btcr** driver, you first need to build [txref-conversion-java](https://github.com/WebOfTrustInfo/txref-conversion-java/).

Build all:

	mvn clean install

### Local Resolver

You can use a [Local Resolver](https://github.com/decentralized-identity/universal-resolver/tree/master/implementations/java/uni-resolver-local) in your Java project that invokes drivers locally (either directly via their JAVA API or via a Docker REST API).

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-resolver-local</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

[Example Use](https://github.com/decentralized-identity/universal-resolver/blob/master/implementations/java/examples/src/main/java/uniresolver/examples/TestLocalUniResolver.java):

	LocalUniResolver uniResolver = LocalUniResolver.getDefault();
	uniResolver.getDriver(DidSovDriver.class).setLibIndyPath("./sovrin/lib/");
	uniResolver.getDriver(DidSovDriver.class).setPoolConfigName("live");
	uniResolver.getDriver(DidSovDriver.class).setPoolGenesisTxn("live.txn");
	uniResolver.getDriver(DidBtcrDriver.class).setExtendedBitcoinConnection(BlockcypherAPIExtendedBitcoinConnection.get());
	
	DDO ddo1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw");
	System.out.println(ddo1.serialize());
	
	DDO ddo2 = uniResolver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl");
	System.out.println(ddo2.serialize());

### Web Resolver

You can deploy a [Web Resolver](https://github.com/decentralized-identity/universal-resolver/tree/master/implementations/java/uni-resolver-web) that can be called by clients and invokes drivers locally (either directly via their JAVA API or via a Docker REST API).

See the [Example Configuration](https://github.com/decentralized-identity/universal-resolver/tree/master/implementations/java/uni-resolver-web/src/main/webapp/WEB-INF/applicationContext.xml).

How to run:

	mvn jetty:run

### Client Resolver

You can use a [Client Resolver](https://github.com/decentralized-identity/universal-resolver/tree/master/implementations/java/uni-resolver-client) in your Java project that calls a remote Web Resolver.

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-resolver-client</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

[Example Use](https://github.com/decentralized-identity/universal-resolver/blob/master/implementations/java/examples/src/main/java/uniresolver/examples/TestClientUniResolver.java):

	ClientUniResolver uniResolver = new ClientUniResolver();
	uniResolver.setResolverUri("https://uniresolver.danubetech.com/1.0/identifiers/");
	
	DDO ddo1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw");
	System.out.println(ddo1.serialize());
	
	DDO ddo2 = uniResolver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl");
	System.out.println(ddo2.serialize());

### Drivers

**TODO** more details

Drivers can be invoked either locally as a Maven dependency, or they can be invoked via a REST GET call to a Docker container.

### Docker

Both the Web Resolver and the individual drivers for **did:sov** and **did:btcr** can be built and deployed as Docker containers.

How to build:

	docker build -f ./docker/Dockerfile-driver-did-btcr . -t uni-resolver-driver-did-btcr
	docker build -f ./docker/Dockerfile-driver-did-sov . -t uni-resolver-driver-did-sov

How to run:

	docker run -p 8081:8080 uni-resolver-driver-did-btcr
	docker run -p 8082:8080 uni-resolver-driver-did-sov

### About

Decentralized Identity Foundation - http://identity.foundation/
