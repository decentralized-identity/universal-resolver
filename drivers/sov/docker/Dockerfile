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

RUN apt-get install -y --no-install-recommends curl build-essential pkg-config cmake libssl-dev libsqlite3-dev libsodium-dev libzmq3-dev

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y

RUN cd /opt/ && git clone https://github.com/hyperledger/indy-sdk.git && cd indy-sdk && git checkout v1.6.2
RUN cd /opt/indy-sdk/libindy/ && ~/.cargo/bin/cargo clean && ~/.cargo/bin/cargo build
RUN cd /opt/indy-sdk/wrappers/java/ && mvn clean install -DskipTests

# build driver-did-sov

ADD . /opt/driver-did-sov
RUN cd /opt/driver-did-sov && mvn install package -N -DskipTests

RUN mkdir /opt/driver-did-sov/lib/
RUN cp /opt/indy-sdk/libindy/target/debug/libindy.so /opt/driver-did-sov/sovrin/lib/

# clean up

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# variables

ENV uniresolver_driver_did_sov_libIndyPath ./sovrin/lib/
ENV uniresolver_driver_did_sov_poolConfigName 11347-05
ENV uniresolver_driver_did_sov_poolGenesisTxn ./sovrin/11347-05.txn
ENV uniresolver_driver_did_sov_walletName default

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-sov/docker/run-driver-did-sov.sh
CMD "/opt/driver-did-sov/docker/run-driver-did-sov.sh"
