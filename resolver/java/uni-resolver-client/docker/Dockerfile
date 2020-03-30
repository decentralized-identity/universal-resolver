# Dockerfile for universalresolver/uni-resolver-client

FROM maven:3-jdk-8
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root

# build uni-resolver-client

ADD ./resolver/java/ /opt/uni-resolver-java

RUN cd /opt/uni-resolver-java && mvn --settings /opt/uni-resolver-java/settings.xml clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-resolver-java/uni-resolver-core && mvn --settings /opt/uni-resolver-java/settings.xml clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-resolver-java/uni-resolver-client && mvn --settings /opt/uni-resolver-java/settings.xml clean install -N -DskipTests && mvn clean
