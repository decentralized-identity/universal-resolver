# Dockerfile for universalresolver/uni-resolver-web

FROM maven:3-eclipse-temurin-17-focal AS build
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build uni-resolver-web

ADD . /opt/universal-resolver
RUN cd /opt/universal-resolver && mvn clean install -N
RUN cd /opt/universal-resolver/uni-resolver-core && mvn clean install -N
RUN cd /opt/universal-resolver/driver && mvn clean install -N
RUN cd /opt/universal-resolver/driver-http && mvn clean install -N
RUN cd /opt/universal-resolver/uni-resolver-local && mvn clean install -N
RUN cd /opt/universal-resolver/uni-resolver-web && mvn clean install package -N

# build image

FROM eclipse-temurin:17-jre-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

WORKDIR /opt/universal-resolver/uni-resolver-web/

COPY --from=build /opt/universal-resolver/uni-resolver-web/target/*-exec.jar ./

ENV uniresolver_web_spring_profiles_active=default

# done

EXPOSE 8080
CMD java -jar *.jar
