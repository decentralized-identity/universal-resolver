FROM node:lts-alpine3.13

LABEL "com.github.actions.name"="Did Test Suite"
LABEL "com.github.actions.description"="Run the did-test-suite against the api of a Universal Resolver deployment"
LABEL "com.github.actions.icon"="mic"
LABEL "com.github.actions.color"="blue"
LABEL "version"="1.0.0"
LABEL "repository"="https://github.com/decentralized-identity/universal-resolver"
LABEL "homepage"="https://uniresolver.io"
LABEL "maintainer"="Bernhard Fuchs <bernhard.fuchs@danubetech.com>"

RUN apk update && apk upgrade && \
    apk add --no-cache git

WORKDIR /run-did-test-suite/

COPY app/package.json .
RUN npm install

COPY app/index.js .
COPY app/local-files-utils.js .
COPY app/testserver-utils.js .
COPY app/utils.js .

COPY entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]