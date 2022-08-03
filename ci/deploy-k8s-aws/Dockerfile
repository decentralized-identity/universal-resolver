FROM python:3.8.0-slim

LABEL "name"="Deployment of the Universal Resolver to a Kubernetes Cluster"
LABEL "maintainer"="Bernhard Fuchs <bernhard.fuchs@danubetech.com>"
LABEL "version"="1.0.0"

LABEL "com.github.actions.name"="GitHub Action for deploying the Universal Resolver"
LABEL "com.github.actions.description"="Deployes the Universal Resolver to a Kubernetes cluster."
LABEL "com.github.actions.icon"="package"
LABEL "com.github.actions.color"="blue"

RUN apt-get update -y && \
    apt-get install -y curl gnupg openssh-client git && \
    curl -Lso /bin/aws-iam-authenticator https://amazon-eks.s3-us-west-2.amazonaws.com/1.11.5/2018-12-06/bin/linux/amd64/aws-iam-authenticator && \
    chmod +x /bin/aws-iam-authenticator && \
    curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.23.6/bin/linux/amd64/kubectl && \
    chmod +x ./kubectl && \
    mv ./kubectl /usr/local/bin/kubectl && \
    apt-get -y clean && apt-get -y autoclean && apt-get -y autoremove && \
    pip install setuptools awscli

COPY app-specs /app-specs
COPY namespace /namespace
COPY scripts /
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
