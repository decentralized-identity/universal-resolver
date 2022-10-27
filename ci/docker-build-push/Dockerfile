#!/bin/bash
FROM docker:stable

LABEL "name"="Docker build/push Action"
LABEL "maintainer"="Phil <philipp.potisk@danubetech.com>"
LABEL "version"="1.0.0"

LABEL "com.github.actions.name"="Docker build/push Action"
LABEL "com.github.actions.description"="GitHub Action for building and pushing a Docker container"
LABEL "com.github.actions.icon"="package"
LABEL "com.github.actions.color"="blue"

ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
