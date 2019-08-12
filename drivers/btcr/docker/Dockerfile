# Dockerfile for universalresolver/driver-did-btcr

FROM universalresolver/base-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build dependencies

RUN cd /opt/ && git clone https://github.com/dcdpr/libbech32-java.git && \
    cd /opt/libbech32-java/ && mvn clean install -DskipTests && mvn clean

RUN cd /opt/ && git clone https://github.com/dcdpr/libtxref-java.git && \
    cd /opt/libtxref-java/ && mvn clean install -DskipTests && mvn clean

RUN cd /opt/ && git clone https://github.com/WebOfTrustInfo/btc-tx-lookup-java.git && \
    cd /opt/btc-tx-lookup-java/ && mvn clean install -DskipTests && mvn clean

# build driver-did-btcr

ADD . /opt/driver-did-btcr
RUN cd /opt/driver-did-btcr && mvn clean install package -N -DskipTests

# variables

ENV uniresolver_driver_did_btcr_bitcoinConnection=blockcypherapi
ENV uniresolver_driver_did_btcr_rpcUrlMainnet=http://user:pass@localhost:8332/
ENV uniresolver_driver_did_btcr_rpcUrlTestnet=http://user:pass@localhost:18332/

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-btcr/docker/run-driver-did-btcr.sh
CMD "/opt/driver-did-btcr/docker/run-driver-did-btcr.sh"
