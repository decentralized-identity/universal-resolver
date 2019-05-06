# Dockerfile for universalresolver/driver-dns

FROM universalresolver/base-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-dns

ADD . /opt/driver-dns
RUN cd /opt/driver-dns && mvn clean install package -N -DskipTests

# variables

ENV uniresolver_driver_dns_dnsServers=

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-dns/docker/run-driver-dns.sh
CMD "/opt/driver-dns/docker/run-driver-dns.sh"
