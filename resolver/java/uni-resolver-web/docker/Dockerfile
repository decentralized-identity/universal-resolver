# Dockerfile for universalresolver/uni-resolver-web

FROM maven:3-jdk-8
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root

# build dependencies

RUN cd /opt/ && git clone https://github.com/decentralized-identity/did-common-java.git
RUN cd /opt/did-common-java/ && mvn clean install -DskipTests

RUN mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get -DrepoUrl=https://repo.maven.apache.org/maven2 -Dartifact=org.eclipse.jetty:jetty-maven-plugin:9.4.18.v20190429
RUN mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get -DrepoUrl=https://repo.maven.apache.org/maven2 -Dartifact=org.apache.maven.plugins:maven-war-plugin:3.2.2

# build uni-resolver-web

ADD ./resolver/java/ /opt/uni-resolver-java
ADD ./config.json /opt/uni-resolver-java/uni-resolver-web/

RUN cd /opt/uni-resolver-java && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-resolver-java/uni-resolver-core && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-resolver-java/driver && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-resolver-java/driver-http && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-resolver-java/uni-resolver-local && mvn clean install -N -DskipTests && mvn clean && \
    cd /opt/uni-resolver-java/uni-resolver-web && mvn clean install package -N -DskipTests

# done

EXPOSE 8080

RUN chmod a+rx /opt/uni-resolver-java/uni-resolver-web/docker/run-uni-resolver-web.sh
CMD "/opt/uni-resolver-java/uni-resolver-web/docker/run-uni-resolver-web.sh"
