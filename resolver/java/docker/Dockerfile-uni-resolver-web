# Dockerfile for universalresolver/uni-resolver-web

FROM universalresolver/base-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build uni-resolver-web

ADD . /opt/uni-resolver-java

RUN cd /opt/uni-resolver-java && mvn clean && \
    cd /opt/uni-resolver-java && mvn install -N -DskipTests && \
    cd /opt/uni-resolver-java/uni-resolver-core && mvn install -N -DskipTests && \
    cd /opt/uni-resolver-java/driver && mvn install -N -DskipTests && \
    cd /opt/uni-resolver-java/driver-http && mvn install -N -DskipTests && \
    cd /opt/uni-resolver-java/uni-resolver-local && mvn install -N -DskipTests && \
    cd /opt/uni-resolver-java/uni-resolver-web && mvn install -N -DskipTests

# done

EXPOSE 8080

RUN chmod a+rx /opt/uni-resolver-java/docker/run-uni-resolver-web.sh
CMD "/opt/uni-resolver-java/docker/run-uni-resolver-web.sh"
