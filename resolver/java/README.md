# Universal Resolver — Java Components

This is a Java implementation of a Universal Resolver. See [universal-resolver](https://github.com/decentralized-identity/universal-resolver/) for a general introduction to Universal Resolvers and drivers.

## Build (native Java)

First you need to add the following to to your "~/.m2/settings.xml". Here OWNER should be be developer's GitHub user name and TOKEN must be GitHub personal access token . For more information [follow](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line).
~~~
  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>github</id>
          <name>GitHub WebOfTrustInfo ld-signatures-java</name>
          <url>https://maven.pkg.github.com/WebOfTrustInfo/ld-signatures-java</url>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub danubetech verifiable-credentials-java</name>
          <url>https://maven.pkg.github.com/danubetech/verifiable-credentials-java</url>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub danubetech key-formats-java</name>
          <url>https://maven.pkg.github.com/danubetech/key-formats-java</url>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub tomasbjerre git-changelog-maven-plugin</name>
          <url>https://maven.pkg.github.com/tomasbjerre/git-changelog-maven-plugin</url>
        </repository> 
	<repository>
          <id>github</id>
          <name>GitHub decentralized-identity did-common-java</name>
          <url>https://maven.pkg.github.com/decentralized-identity/did-common-java</url>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub decentralized-identity universal-resolver</name>
          <url>https://maven.pkg.github.com/decentralized-identity/universal-resolver</url>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub decentralized-identity uni-resolver-driver-did-sov</name>
          <url>https://maven.pkg.github.com/decentralized-identity/uni-resolver-driver-did-sov</url>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub decentralized-identity uni-resolver-driver-did-btcr</name>
          <url>https://maven.pkg.github.com/decentralized-identity/uni-resolver-driver-did-btcr</url>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub decentralized-identity uni-resolver-driver-did-work</name>
          <url>https://maven.pkg.github.com/decentralized-identity/uni-resolver-driver-did-work</url>
        </repository>
        <repository>
          <id>github</id>
          <name>GitHub decentralized-identity uni-resolver-driver-did-stack</name>
          <url>https://maven.pkg.github.com/decentralized-identity/uni-resolver-driver-did-stack</url>
        </repository>
	<repository>
          <id>github</id>
          <name>GitHub decentralized-identity did-common-java</name>
          <url>https://maven.pkg.github.com/decentralized-identity/did-common-java</url>
        </repository>
	<repository>
          <id>github</id>
          <name>GitHub WebOfTrustInfo btc-tx-lookup-java</name>
          <url>https://maven.pkg.github.com/WebOfTrustInfo/btc-tx-lookup-java</url>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>azuzi</username>
      <password>97fc66008d7cbe49bfbd507487f16c7394bcef85</password>
    </server>
  </servers>
~~~
Then run:

	mvn clean install

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
