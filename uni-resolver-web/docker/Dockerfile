# Dockerfile for universalresolver/uni-resolver-web

FROM maven:3-jdk-11 AS build
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build uni-resolver-web

ADD . /opt/universal-resolver
ADD config.json /opt/universal-resolver/uni-resolver-web/
RUN cd /opt/universal-resolver && mvn clean install -N
RUN cd /opt/universal-resolver/uni-resolver-core && mvn clean install -N
RUN cd /opt/universal-resolver/driver && mvn clean install -N
RUN cd /opt/universal-resolver/driver-http && mvn clean install -N
RUN cd /opt/universal-resolver/uni-resolver-local && mvn clean install -N
RUN cd /opt/universal-resolver/uni-resolver-web && mvn clean install package -N

# build image

FROM jetty:9.4-jre11
MAINTAINER Markus Sabadello <markus@danubetech.com>

COPY --from=build /opt/universal-resolver/uni-resolver-web/target/*.war /var/lib/jetty/webapps/ROOT.war
COPY --from=build /opt/universal-resolver/uni-resolver-web/config.json /var/lib/jetty/

# done

EXPOSE 8080
CMD java -Djetty.http.port=8080 -jar /usr/local/jetty/start.jar
