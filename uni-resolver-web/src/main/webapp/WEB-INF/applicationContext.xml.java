<?xml version="1.0" encoding="UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:util="http://www.springframework.org/schema/util" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-2.0.xsd">

	<!-- UNIVERSAL RESOLVER SERVLET -->

	<bean name="UniResolver" class="uniresolver.local.LocalUniResolver">
		<property name="drivers">
			<util:map>
				<entry key="did:btcr"><ref bean="DidBtcrDriver" /></entry>
				<entry key="did:sov"><ref bean="DidSovDriver" /></entry>
				<entry key="did:v1"><ref bean="DidV1Driver" /></entry>
			</util:map>
		</property>
	</bean>

	<bean name="ResolveServlet" class="uniresolver.web.servlet.ResolveServlet">
		<property name="uniResolver" ref="UniResolver" />
	</bean>

	<bean name="PropertiesServlet" class="uniresolver.web.servlet.PropertiesServlet">
		<property name="uniResolver" ref="UniResolver" />
	</bean>

	<!-- DRIVERS (VIA JAVA API) -->

	<bean id="DidBtcrDriver" class="uniresolver.driver.did.btcr.DidBtcrDriver">
		<property name="bitcoinConnection">
			<bean class="uniresolver.driver.did.btcr.bitcoinconnection.BlockcypherAPIBitcoinConnection" />
		</property>
	</bean>

	<bean id="DidSovDriver" class="uniresolver.driver.did.sov.DidSovDriver">
		<property name="libIndyPath" value="./sovrin/lib/" />
		<property name="poolConfigName" value="11347-05" />
		<property name="poolGenesisTxn" value="./sovrin/11347-05.txn" />
		<property name="walletName" value="default" />
	</bean>

	<bean id="DidV1Driver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="https://testnet.veres.one/dids/" />
	</bean>

	<!-- DRIVERS (VIA DOCKER REST API) -->

<!-- 	<bean id="DidBtcrDriver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="http://driver-did-btcr:8081/1.0/identifiers/$1" />
		<property name="propertiesUri" value="http://driver-did-btcr:8081/1.0/properties" />
		<property name="pattern" value="^(did:btcr:.+)$" />
	</bean>

	<bean id="DidSovDriver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="http://driver-did-sov:8082/1.0/identifiers/$1" />
		<property name="propertiesUri" value="http://driver-did-sov:8082/1.0/properties" />
		<property name="pattern" value="^(did:sov:.+)$" />
	</bean>

	<bean id="DidUportDriver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="http://driver-did-uport:8081/1.0/identifiers/$1" />
		<property name="pattern" value="^(did:uport:.+)$|^(did:muport:.+)$|^(did:eth:.+)$|^(did:ethr:.+)$" />
	</bean>

	<bean id="DidV1Driver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="https://genesis.testnet.veres.one/dids/$1" />
		<property name="pattern" value="^(did:v1:.+)$" />
	</bean>

	<bean id="DidIpidDriver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="https://ipfs.io/ipns/$1" />
		<property name="pattern" value="^did:ipid:(.+)$" />
	</bean>

	<bean id="DidStackDriver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="http://driver-did-stack:8084/1.0/identifiers/$1" />
		<property name="propertiesUri" value="http://driver-did-stack:8084/1.0/properties" />
		<property name="pattern" value="^(did:stack:.+)$" />
	</bean>

	<bean id="DidErc725Driver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="http://driver-did-erc725:8085/1.0/identifiers/$1" />
		<property name="propertiesUri" value="http://driver-did-erc725:8085/1.0/properties" />
		<property name="pattern" value="^(did:erc725:.+)$" />
	</bean>

	<bean id="DidDomDriver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="http://driver-did-dom:8086/1.0/identifiers/$1" />
		<property name="propertiesUri" value="http://driver-did-dom:8086/1.0/properties" />
		<property name="pattern" value="^(did:dom:.+)$" />
	</bean> -->

</beans>
