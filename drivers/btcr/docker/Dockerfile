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

RUN cd /opt/ && git clone https://github.com/WebOfTrustInfo/txref-conversion-java.git
RUN cd /opt/txref-conversion-java/ && mvn clean install -DskipTests

# build driver-did-btcr

ADD . /opt/driver-did-btcr
RUN cd /opt/driver-did-btcr && mvn install package -N -DskipTests

# clean up

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# variables

ENV uniresolver_driver_did_btcr_bitcoinConnection blockcypherapi
ENV uniresolver_driver_did_btcr_rpcUrlMainnet http://user:pass@localhost:8332/
ENV uniresolver_driver_did_btcr_rpcUrlTestnet http://user:pass@localhost:18332/

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-btcr/docker/run-driver-did-btcr.sh
CMD "/opt/driver-did-btcr/docker/run-driver-did-btcr.sh"
