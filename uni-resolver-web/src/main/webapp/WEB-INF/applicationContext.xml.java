<?xml version="1.0" encoding="UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:util="http://www.springframework.org/schema/util" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-2.0.xsd">

	<!-- UNIVERSAL RESOLVER SERVLET -->

	<bean name="UniResolver" class="uniresolver.local.LocalUniResolver">
		<property name="drivers">
			<util:map>
				<entry key="did-btcr"><ref bean="DidBtcrDriver" /></entry>
				<entry key="did-sov"><ref bean="DidSovDriver" /></entry>
				<entry key="did-v1"><ref bean="DidV1Driver" /></entry>
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
		<property name="libIndyPath" value="" />
		<property name="poolConfigs" value="_;./sovrin/mainnet.txn;staging;./sovrin/stagingnet.txn;builder;./sovrin/buildernet.txn;danube;./sovrin/danube.txn" />
		<property name="poolVersions" value="_;2;staging;2;builder;2;danube;2" />
		<property name="walletName" value="default" />
	</bean>

	<bean id="DidV1Driver" class="uniresolver.driver.http.HttpDriver">
		<property name="resolveUri" value="https://testnet.veres.one/dids/" />
	</bean>

</beans>
