![DIF Logo](https://github.com/decentralized-identity/universal-resolver/blob/master/implementations/java/logo-dif.png?raw=true)

### Information

This is a work-in-progress Java implementation of a Universal Resolver (aka Community Resolver) to be used for a decentralized naming system. It includes core resolution logic, a web API, a client library, and drivers for the **did:sov**, **did:btcr**, and **did:stack** methods.

See the [specifications](https://github.com/decentralized-identity/universal-resolver/blob/master/docs/api-documentation.md) for more information.

Incomplete implementation! Not ready for production use! Use at your own risk! Pull requests welcome.

### Quick Start

You can deploy the Universal Resolver on your local machine by cloning this Github repository, and using `docker-compose` to build and run the Universal Resolver as well as its drivers:

	git clone https://github.com/decentralized-identity/universal-resolver.git
	cd universal-resolver/implementations/java/
	docker-compose -f docker-compose.yml build
	docker-compose -f docker-compose.yml up

You should then be able to resolve identifiers locally using simple `curl` requests as follows:

	curl -X GET  http://localhost:8080/1.0/identifiers/did:sov:WRfXPg8dantKVubE3HX8pw
	curl -X GET  http://localhost:8080/1.0/identifiers/did:btcr:xkrn-xzcr-qqlv-j6sl
	curl -X GET  http://localhost:8080/1.0/identifiers/did:v1:testnet:5431fafa-a38f-4e37-96b6-cdeb8e5d1d40
	curl -X GET  http://localhost:8080/1.0/identifiers/did:ipid:QmbFuwbp7yFDTMX6t8HGcEiy3iHhfvng89A19naCYGKEBj
	curl -X GET  http://localhost:8080/1.0/identifiers/did:uport:2ok9oMAM54TeFMfLb3ZX4i9Qu6x5pcPA7nV
	curl -X GET  http://localhost:8080/1.0/identifiers/did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0

Also see the [Examples](https://github.com/decentralized-identity/universal-resolver/tree/master/implementations/java/examples/src/main/java/uniresolver/examples/).

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

	DDO ddo3 = uniResolver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0");
	System.out.println(ddo3.serialize());

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

	DDO ddo3 = uniResolver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0");
	System.out.println(ddo3.serialize());

### Drivers

**TODO** more details

Drivers can be invoked either locally as a Maven dependency, or they can be invoked via a REST GET call to a Docker container.

Drivers for the **did:sov**, **did:btcr**, and **did:stack** methods are included in this repository. A driver for the **did:uport** method is available at https://github.com/uport-project/uport-did-driver.

### About

Decentralized Identity Foundation - http://identity.foundation/
