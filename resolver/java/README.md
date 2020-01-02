# Universal Resolver — Java Components

This is a Java implementation of a Universal Resolver. See [universal-resolver](https://github.com/decentralized-identity/universal-resolver/) for a general introduction to Universal Resolvers and drivers.

## Build (native Java)

First, build https://github.com/decentralized-identity/did-common-java

Then run:

	mvn clean install -pl '!examples'

## Local Resolver

You can use a [Local Resolver](https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/java/uni-resolver-client) in your Java project that invokes drivers locally (either directly via their JAVA API or via a Docker REST API).

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-resolver-local</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

[Example Use](https://github.com/decentralized-identity/universal-resolver/blob/master/resolver/java/examples/src/main/java/uniresolver/examples/TestLocalUniResolver.java):

	LocalUniResolver uniResolver = LocalUniResolver.getDefault();
	uniResolver.getDriver(DidSovDriver.class).setLibIndyPath("./sovrin/lib/");
	uniResolver.getDriver(DidSovDriver.class).setPoolConfigName("live");
	uniResolver.getDriver(DidSovDriver.class).setPoolGenesisTxn("live.txn");
	uniResolver.getDriver(DidBtcrDriver.class).setBitcoinConnection(BlockcypherAPIBitcoinConnection.get());
	
	DIDDocument didDocument1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw").getDidDocument();
	System.out.println(didDocument1.toJson());
	
	DIDDocument didDocument2 = uniResolver.resolve("did:btcr:xkrn-xzcr-qqlv-j6sl").getDidDocument();
	System.out.println(didDocument2.toJson());
	
	DIDDocument didDocument3 = uniResolver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0").getDidDocument();
	System.out.println(didDocument3.toJson());

## Web Resolver

You can deploy a [Web Resolver](https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/java/uni-resolver-web) that can be called by clients and invokes drivers locally (either directly via their JAVA API or via a Docker REST API).

See the [Example Configuration](https://github.com/decentralized-identity/universal-resolver/blob/master/resolver/java/uni-resolver-web/src/main/webapp/WEB-INF/applicationContext.xml).

How to run:

	mvn jetty:run

## Client Resolver

You can use a [Client Resolver](https://github.com/decentralized-identity/universal-resolver/tree/master/resolver/java/uni-resolver-client) in your Java project that calls a remote Web Resolver.

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-resolver-client</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

[Example Use](https://github.com/decentralized-identity/universal-resolver/blob/master/resolver/java/examples/src/main/java/uniresolver/examples/TestClientUniResolver.java):

	ClientUniResolver uniResolver = new ClientUniResolver();
	uniResolver.setResolveUri("https://uniresolver.danubetech.com/1.0/identifiers/");
	
	DIDDocument didDocument1 = uniResolver.resolve("did:sov:WRfXPg8dantKVubE3HX8pw").getDidDocument();
	System.out.println(didDocument1.toJson());
	
	DIDDocument didDocument2 = uniResolver.resolve("did:btcr:xz35-jzv2-qqs2-9wjt").getDidDocument();
	System.out.println(didDocument2.toJson());
	
	DIDDocument didDocument3 = uniResolver.resolve("did:stack:v0:16EMaNw3pkn3v6f2BgnSSs53zAKH4Q8YJg-0").getDidDocument();
	System.out.println(didDocument3.toJson());

## About

Decentralized Identity Foundation - http://identity.foundation/
