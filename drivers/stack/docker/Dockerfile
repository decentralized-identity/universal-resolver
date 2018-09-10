# Dockerfile

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

RUN cd /opt/ && git clone https://github.com/decentralized-identity/universal-resolver-java.git
RUN cd /opt/universal-resolver-java && mvn clean install -N -DskipTests
RUN cd /opt/universal-resolver-java/uni-resolver-core && mvn install -N -DskipTests
RUN cd /opt/universal-resolver-java/driver && mvn install -N -DskipTests

# build driver-did-stack

ADD . /opt/driver-did-stack
RUN cd /opt/driver-did-stack && mvn install package -N -DskipTests

# done

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

EXPOSE 8084

RUN chmod a+rx /opt/driver-did-stack/docker/run-driver-did-stack.sh
CMD "/opt/driver-did-stack/docker/run-driver-did-stack.sh"
