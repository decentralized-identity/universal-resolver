# Dockerfile for universalresolver/driver-did-dom

FROM universalresolver/driver
MAINTAINER Ricardo Cosme <ricardo.cosme@dominode.com>

# build driver-did-dom

ADD . /opt/driver-did-dom
RUN cd /opt/driver-did-dom && mvn install package -N -DskipTests

# done

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

EXPOSE 8086

RUN chmod a+rx /opt/driver-did-dom/docker/run-driver-did-dom.sh
CMD "/opt/driver-did-dom/docker/run-driver-did-dom.sh"
