# Dockerfile for universalresolver/driver-did-erc725

FROM universalresolver/driver
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-erc725

ADD . /opt/driver-did-erc725
RUN cd /opt/driver-did-erc725 && mvn install package -N -DskipTests

# clean up

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# variables

ENV uniresolver_driver_did_erc725_ethereumConnection=jsonrpc
ENV uniresolver_driver_did_erc725_rpcUrlMainnet=https://mainnet.infura.io/9W2fvWQMP6cJCMH3ESqP
ENV uniresolver_driver_did_erc725_rpcUrlRopsten=https://ropsten.infura.io/9W2fvWQMP6cJCMH3ESqP
ENV uniresolver_driver_did_erc725_rpcUrlRinkeby=https://rinkeby.infura.io/9W2fvWQMP6cJCMH3ESqP
ENV uniresolver_driver_did_erc725_rpcUrlKovan=https://kovan.infura.io/9W2fvWQMP6cJCMH3ESqP
ENV uniresolver_driver_did_erc725_etherscanApiMainnet=http://api.etherscan.io/api
ENV uniresolver_driver_did_erc725_etherscanApiRopsten=http://api-ropsten.etherscan.io/api
ENV uniresolver_driver_did_erc725_etherscanApiRinkeby=http://api-rinkeby.etherscan.io/api
ENV uniresolver_driver_did_erc725_etherscanApiKovan=http://api-kovan.etherscan.io/api

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-erc725/docker/run-driver-did-erc725.sh
CMD "/opt/driver-did-erc725/docker/run-driver-did-erc725.sh"
