FROM docker:29-cli AS docker-cli

FROM python:3.14-alpine

LABEL org.opencontainers.image.title="Universal Resolver PR check"
LABEL org.opencontainers.image.description="Checks application.yml, docker-compose.yml, .env and README.md for malformed content and non-public images"
LABEL org.opencontainers.image.source="https://github.com/decentralized-identity/universal-resolver"
LABEL org.opencontainers.image.url="https://uniresolver.io"
LABEL maintainer="Bernhard Fuchs <bernhard.fuchs@danubetech.com>"

# Only the compose binary is needed to validate docker-compose.yml, no daemon
COPY --from=docker-cli /usr/local/libexec/docker/cli-plugins/docker-compose /usr/local/bin/docker-compose

WORKDIR /pr-check

COPY app/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app/pr_check.py .

COPY entrypoint.sh /
ENTRYPOINT ["/entrypoint.sh"]
