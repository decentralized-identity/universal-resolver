# Dockerfile for universalresolver/uni-resolver-web

FROM maven:3-jdk-11
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build uni-resolver-web

ADD ./resolver/java/ /opt/uni-resolver-java
ADD ./config.json /opt/uni-resolver-java/uni-resolver-web/
RUN cd /opt/uni-resolver-java && mvn clean install -N -DskipTests
RUN cd /opt/uni-resolver-java/uni-resolver-core && mvn clean install -N -DskipTests
RUN cd /opt/uni-resolver-java/driver && mvn clean install -N -DskipTests
RUN cd /opt/uni-resolver-java/driver-http && mvn clean install -N -DskipTests
RUN cd /opt/uni-resolver-java/uni-resolver-local && mvn clean install -N -DskipTests
RUN cd /opt/uni-resolver-java/uni-resolver-web && mvn clean install package -N -DskipTests

# build image

FROM openjdk:11
MAINTAINER Markus Sabadello <markus@danubetech.com>

COPY --from=0 /opt/uni-resolver-java/uni-resolver-web/target/*.war /var/lib/jetty/webapps/ROOT.war
ENV jetty.port=8080

# done

EXPOSE 8080
