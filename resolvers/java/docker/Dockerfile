# Dockerfile for universalresolver/base

FROM debian:stretch
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root

RUN apt-get -y update

RUN apt-get install -y --no-install-recommends openjdk-8-jdk maven

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
ENV PATH $JAVA_HOME/bin:$PATH

# build dependencies

RUN apt-get install -y --no-install-recommends git

RUN cd /opt/ && git clone https://github.com/decentralized-identity/did-common-java.git
RUN cd /opt/did-common-java/ && mvn clean install -DskipTests
