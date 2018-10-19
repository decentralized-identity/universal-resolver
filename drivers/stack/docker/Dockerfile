# Dockerfile for universalresolver/driver-did-stack

FROM universalresolver/driver
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-stack

ADD . /opt/driver-did-stack
RUN cd /opt/driver-did-stack && mvn install package -N -DskipTests

# done

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

EXPOSE 8084

RUN chmod a+rx /opt/driver-did-stack/docker/run-driver-did-stack.sh
CMD "/opt/driver-did-stack/docker/run-driver-did-stack.sh"
