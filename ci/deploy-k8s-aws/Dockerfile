FROM debian:bookworm-slim

LABEL "name"="Deployment of the Universal Resolver to a Kubernetes Cluster"
LABEL "maintainer"="Bernhard Fuchs <bernhard.fuchs@danubetech.com>"
LABEL "version"="2.0.0"

LABEL "com.github.actions.name"="GitHub Action for deploying the Universal Resolver"
LABEL "com.github.actions.description"="Deploys the Universal Resolver to a Kubernetes cluster with smart deployment strategy."
LABEL "com.github.actions.icon"="package"
LABEL "com.github.actions.color"="blue"

# Install base tools and AWS CLI
RUN apt-get update -y && \
    apt-get install -y curl gnupg openssh-client git unzip jq ca-certificates && \
    # Install AWS CLI v2
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf awscliv2.zip aws && \
    # Install AWS IAM Authenticator
    curl -Lso /bin/aws-iam-authenticator https://amazon-eks.s3-us-west-2.amazonaws.com/1.11.5/2018-12-06/bin/linux/amd64/aws-iam-authenticator && \
    chmod +x /bin/aws-iam-authenticator && \
    # Install kubectl (latest stable)
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    chmod +x ./kubectl && \
    mv ./kubectl /usr/local/bin/kubectl && \
    # Install yq for YAML parsing
    curl -Lso /usr/local/bin/yq https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 && \
    chmod +x /usr/local/bin/yq && \
    # Cleanup
    apt-get -y clean && apt-get -y autoclean && apt-get -y autoremove && \
    rm -rf /var/lib/apt/lists/*

# Copy deployment scripts
COPY scripts /scripts
RUN chmod +x /scripts/*.sh

ENTRYPOINT ["/scripts/entrypoint.sh"]
