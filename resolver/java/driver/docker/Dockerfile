# Dockerfile for universalresolver/driver

FROM universalresolver/base
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver

ADD . /opt/uni-resolver-java

RUN cd /opt/uni-resolver-java && mvn clean install -N -DskipTests
RUN cd /opt/uni-resolver-java/uni-resolver-core && mvn install -N -DskipTests
RUN cd /opt/uni-resolver-java/driver && mvn install -N -DskipTests
