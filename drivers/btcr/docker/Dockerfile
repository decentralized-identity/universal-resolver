# Dockerfile for universalresolver/driver-did-btcr

FROM universalresolver/driver
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build dependencies

RUN cd /opt/ && git clone https://github.com/WebOfTrustInfo/txref-conversion-java.git
RUN cd /opt/txref-conversion-java/ && mvn clean install -DskipTests

# build driver-did-btcr

ADD . /opt/driver-did-btcr
RUN cd /opt/driver-did-btcr && mvn install package -N -DskipTests

# clean up

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# variables

ENV uniresolver_driver_did_btcr_bitcoinConnection=blockcypherapi
ENV uniresolver_driver_did_btcr_rpcUrlMainnet=http://user:pass@localhost:8332/
ENV uniresolver_driver_did_btcr_rpcUrlTestnet=http://user:pass@localhost:18332/

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-btcr/docker/run-driver-did-btcr.sh
CMD "/opt/driver-did-btcr/docker/run-driver-did-btcr.sh"
