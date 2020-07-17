#!/bin/sh

echo "#### Kubernetes Deployment of the Universal Resolver ####"

set -e

echo "## Root folder ##"
ls -al /

echo "$KUBE_CONFIG_DATA" | base64 --decode > /tmp/config
export KUBECONFIG=/tmp/config

cp /prepare-deployment.py /k8s-template.yaml -r /app-specs -r /namespace . 2>/dev/null || :

echo "Current workspace Folder"
pwd
ls -al .
python --version
python prepare-deployment.py

echo "## Deployment Folder ##"
cd deploy
ls -al .

echo "### Deploying following Specs ### "
cat deploy.sh

./deploy.sh
