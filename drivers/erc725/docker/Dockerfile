# Dockerfile for universalresolver/driver-did-erc725

FROM universalresolver/base-alpine
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-erc725

ADD . /opt/driver-did-erc725

RUN cd /opt/driver-did-erc725 && mvn clean install package -N -DskipTests

# variables

ENV uniresolver_driver_did_erc725_ethereumConnections=mainnet;hybrid;ropsten;hybrid;rinkeby;hybrid;kovan;hybrid
ENV uniresolver_driver_did_erc725_rpcUrls=mainnet;https://mainnet.infura.io/v3/fd9e225bc1234f49b48b295c611078eb;ropsten;https://ropsten.infura.io/v3/fd9e225bc1234f49b48b295c611078eb;rinkeby;https://rinkeby.infura.io/v3/fd9e225bc1234f49b48b295c611078eb;kovan;https://kovan.infura.io/v3/fd9e225bc1234f49b48b295c611078eb
ENV uniresolver_driver_did_erc725_etherscanApis=mainnet;http://api.etherscan.io/api;ropsten;http://api-ropsten.etherscan.io/api;rinkeby;http://api-rinkeby.etherscan.io/api;kovan;http://api-kovan.etherscan.io/api

# done

EXPOSE 8080

RUN chmod a+rx /opt/driver-did-erc725/docker/run-driver-did-erc725.sh
CMD "/opt/driver-did-erc725/docker/run-driver-did-erc725.sh"
