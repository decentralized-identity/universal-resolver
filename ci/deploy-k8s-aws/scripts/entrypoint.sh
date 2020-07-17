#!/bin/sh

echo "#### Kubernetes Deployment of the Universal Resolver ####"

set -e

echo "## Root folder ##"
ls -al /

ls -al
echo "$KUBE_CONFIG_DATA" | base64 --decode > /tmp/config
export KUBECONFIG=/tmp/config

echo "### Prepare deployment ###"
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
