# Universal Resolver — Java Components

This is a Java implementation of a Universal Resolver. See [universal-resolver](https://github.com/decentralized-identity/universal-resolver/) for a general introduction to Universal Resolvers and drivers.

## Build (native Java)

Maven build:

	mvn clean install

## Local Resolver

You can use a [Local Resolver](https://github.com/decentralized-identity/universal-resolver/tree/main/uni-resolver-client) in your Java project that invokes drivers locally (either directly via their JAVA API or via a Docker REST API).

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-resolver-local</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

[Example Use](https://github.com/decentralized-identity/universal-resolver/blob/main/examples/src/main/java/uniresolver/examples/TestLocalUniResolver.java):

## Web Resolver

You can deploy a [Web Resolver](https://github.com/decentralized-identity/universal-resolver/tree/main/uni-resolver-web) that can be called by clients and invokes drivers locally (either directly via their JAVA API or via a Docker REST API).

See the [Example Configuration](https://github.com/decentralized-identity/universal-resolver/blob/main/uni-resolver-web/src/main/webapp/WEB-INF/applicationContext.xml).

How to run:

	mvn jetty:run

## Client Resolver

You can use a [Client Resolver](https://github.com/decentralized-identity/universal-resolver/tree/main/uni-resolver-client) in your Java project that calls a remote Web Resolver.

Dependency:

	<dependency>
		<groupId>decentralized-identity</groupId>
		<artifactId>uni-resolver-client</artifactId>
		<version>0.1-SNAPSHOT</version>
	</dependency>

[Example Use](https://github.com/decentralized-identity/universal-resolver/blob/main/examples/src/main/java/uniresolver/examples/TestClientUniResolver.java):

## About

Decentralized Identity Foundation - http://identity.foundation/
