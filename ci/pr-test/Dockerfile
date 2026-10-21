# For running the PR test locally; the GitHub Action runs the sources directly on the runner (see action.yml).
# Needs the Docker socket: docker run --rm -v /var/run/docker.sock:/var/run/docker.sock pr-test --pr 578
FROM docker:29-cli AS docker-cli

FROM node:24-alpine

LABEL org.opencontainers.image.title="Universal Resolver PR test"
LABEL org.opencontainers.image.description="Starts the drivers changed by a pull request and resolves their test identifiers"
LABEL org.opencontainers.image.source="https://github.com/decentralized-identity/universal-resolver"
LABEL org.opencontainers.image.url="https://uniresolver.io"
LABEL maintainer="Bernhard Fuchs <bernhard.fuchs@danubetech.com>"

# Docker CLI with the compose plugin; the containers run on the host's Docker daemon
COPY --from=docker-cli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=docker-cli /usr/local/libexec/docker/cli-plugins/docker-compose /usr/local/libexec/docker/cli-plugins/docker-compose

WORKDIR /pr-test

COPY package.json package-lock.json ./
RUN npm ci --omit=dev --ignore-scripts && npm cache clean --force

# Node runs the TypeScript sources directly (type stripping), no build step
COPY src ./src

ENTRYPOINT ["node", "/pr-test/src/main.ts"]
