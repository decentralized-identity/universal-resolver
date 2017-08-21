![RWoT Logo](https://github.com/decentralized-identity/uni-resolver-java/blob/master/logo.svg?raw=true)

### Information

This is a work-in-progress Java implementation of a Universal Resolver (aka Community Resolver) to be used for a decentralized naming system. It includes core resolution logic, a web interface, a client library, and drivers for the **did:sov** and **did:btcr** methods.

See the [specifications](https://github.com/decentralized-identity/universal-resolver/blob/master/docs/api-documentation.md) for details.

Use at your own risk! Pull requests welcome.

### Quick Start

Try the following:

	curl -X GET  http://localhost:8080/1.0/identifiers/did:sov:AdLi7vX2z1bLyVZaoy18K1
	curl -X GET  http://localhost:8080/1.0/identifiers/did:btcr:txtest1-xkrn-xzcr-qqlv-j6sl

See the examples:

[Examples](https://github.com/decentralized-identity/uni-resolver-java/blob/master/examples/)

### Build

Build:

	mvn clean install

In order to use the **did:sov** driver, you also need to build [libindy-sdk](https://github.com/hyperledger/indy-sdk/).

### Local Resolver

You can use a local resolver in your Java project that invokes drivers locally.

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-resolver-local</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

Example Code:

	LocalUniResolver uniResolver = LocalUniResolver.getDefault();
	uniResolver.getDriver(DidSovDriver.class).setLibIndyPath("./sovrin/lib/");
	uniResolver.getDriver(DidSovDriver.class).setPoolConfigName("sandbox");
	uniResolver.getDriver(DidSovDriver.class).setPoolGenesisTxn("sandbox.txn");
	uniResolver.getDriver(DidBtcrDriver.class).setExtendedBitcoinConnection(BlockcypherAPIExtendedBitcoinConnection.get());

	DDO ddo1 = uniResolver.resolve("did:sov:AdLi7vX2z1bLyVZaoy18K1");
	System.out.println(ddo1.serialize());

	DDO ddo2 = uniResolver.resolve("did:btcr:txtest1-xkrn-xzcr-qqlv-j6sl");
	System.out.println(ddo2.serialize());

### Client Resolver

You can use a client resolver in your Java project that calls a remote Web Resolver.

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-resolver-client</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

Example Code:

	LocalUniResolver uniResolver = LocalUniResolver.getDefault();
	uniResolver.getDriver(DidSovDriver.class).setLibIndyPath("./sovrin/lib/");
	uniResolver.getDriver(DidSovDriver.class).setPoolConfigName("sandbox");
	uniResolver.getDriver(DidSovDriver.class).setPoolGenesisTxn("sandbox.txn");
	uniResolver.getDriver(DidBtcrDriver.class).setExtendedBitcoinConnection(BlockcypherAPIExtendedBitcoinConnection.get());

	DDO ddo1 = uniResolver.resolve("did:sov:AdLi7vX2z1bLyVZaoy18K1");
	System.out.println(ddo1.serialize());

	DDO ddo2 = uniResolver.resolve("did:btcr:txtest1-xkrn-xzcr-qqlv-j6sl");
	System.out.println(ddo2.serialize());


### About

Decentralized Identity Foundation - http://http://identity.foundation/
