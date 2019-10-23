# Dockerfile for hello2mao/driver-did-ccp

FROM universalresolver/base-ubuntu
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-ccp

ADD . /opt/driver-did-ccp
RUN cd /opt/driver-did-ccp && mvn clean install package -N -DskipTests

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-ccp/docker/run-driver-did-ccp.sh
CMD "/opt/driver-did-ccp/docker/run-driver-did-ccp.sh"
