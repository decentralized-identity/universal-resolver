# Dockerfile for universalresolver/uni-resolver-web

FROM maven:3-jdk-11
MAINTAINER Markus Sabadello <markus@danubetech.com>

USER root

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
