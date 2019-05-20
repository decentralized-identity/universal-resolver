# Dockerfile for universalresolver/base-ubuntu

FROM ubuntu:18.04
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root

RUN apt-get -y update && \
    apt-get install -y --no-install-recommends openjdk-8-jdk-headless maven git && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
ENV PATH $JAVA_HOME/bin:$PATH

# build dependencies

RUN cd /opt/ && git clone https://github.com/decentralized-identity/did-common-java.git
RUN cd /opt/did-common-java/ && mvn clean install -DskipTests

RUN mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get -DrepoUrl=https://repo.maven.apache.org/maven2 -Dartifact=org.eclipse.jetty:jetty-maven-plugin:9.4.18.v20190429
RUN mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get -DrepoUrl=https://repo.maven.apache.org/maven2 -Dartifact=org.apache.maven.plugins:maven-war-plugin:3.2.2

# build driver

ADD . /opt/uni-resolver-java

RUN cd /opt/uni-resolver-java && mvn clean install -N -DskipTests && \
    cd /opt/uni-resolver-java/uni-resolver-core && mvn install -N -DskipTests && \
    cd /opt/uni-resolver-java/driver && mvn install -N -DskipTests
