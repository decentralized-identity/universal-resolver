# Dockerfile for universalresolver/uni-resolver-web

FROM universalresolver/base
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build uni-resolver-web

ADD . /opt/uni-resolver-java

RUN cd /opt/uni-resolver-java && mvn clean
RUN cd /opt/uni-resolver-java && mvn install -N -DskipTests
RUN cd /opt/uni-resolver-java/uni-resolver-core && mvn install -N -DskipTests
RUN cd /opt/uni-resolver-java/driver && mvn install -N -DskipTests
RUN cd /opt/uni-resolver-java/driver-http && mvn install -N -DskipTests
RUN cd /opt/uni-resolver-java/uni-resolver-local && mvn install -N -DskipTests
RUN cd /opt/uni-resolver-java/uni-resolver-web && mvn install -N -DskipTests

# done

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

EXPOSE 8080

RUN chmod a+rx /opt/uni-resolver-java/uni-resolver-web/docker/run-uni-resolver-web.sh
CMD "/opt/uni-resolver-java/uni-resolver-web/docker/run-uni-resolver-web.sh"
