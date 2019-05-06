# Dockerfile for universalresolver/driver-did-erc725

FROM universalresolver/base-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-erc725

ADD . /opt/driver-did-erc725
RUN cd /opt/driver-did-erc725 && mvn clean install package -N -DskipTests

# variables

ENV uniresolver_driver_did_erc725_ethereumConnection=hybrid
ENV uniresolver_driver_did_erc725_rpcUrlMainnet=https://mainnet.infura.io/v3/fd9e225bc1234f49b48b295c611078eb
ENV uniresolver_driver_did_erc725_rpcUrlRopsten=https://ropsten.infura.io/v3/fd9e225bc1234f49b48b295c611078eb
ENV uniresolver_driver_did_erc725_rpcUrlRinkeby=https://rinkeby.infura.io/v3/fd9e225bc1234f49b48b295c611078eb
ENV uniresolver_driver_did_erc725_rpcUrlKovan=https://kovan.infura.io/v3/fd9e225bc1234f49b48b295c611078eb
ENV uniresolver_driver_did_erc725_etherscanApiMainnet=http://api.etherscan.io/api
ENV uniresolver_driver_did_erc725_etherscanApiRopsten=http://api-ropsten.etherscan.io/api
ENV uniresolver_driver_did_erc725_etherscanApiRinkeby=http://api-rinkeby.etherscan.io/api
ENV uniresolver_driver_did_erc725_etherscanApiKovan=http://api-kovan.etherscan.io/api

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-erc725/docker/run-driver-did-erc725.sh
CMD "/opt/driver-did-erc725/docker/run-driver-did-erc725.sh"
