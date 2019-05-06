# Dockerfile for universalresolver/driver-did-stack

FROM universalresolver/base-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-stack

ADD . /opt/driver-did-stack
RUN cd /opt/driver-did-stack && mvn clean install package -N -DskipTests

# done

EXPOSE 8084

RUN chmod a+rx /opt/driver-did-stack/docker/run-driver-did-stack.sh
CMD "/opt/driver-did-stack/docker/run-driver-did-stack.sh"
