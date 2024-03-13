#!/bin/sh

echo "#### Kubernetes Deployment of the Universal Resolver ####"

set -e

echo "## Root folder ##"
ls -al /

echo "$KUBE_CONFIG_DATA" | base64 --decode > /tmp/config
export KUBECONFIG=/tmp/config

cp /*.py /*.yaml -r /app-specs -r /namespace . 2>/dev/null || :

echo "Current workspace Folder"
pwd
ls -al .
python --version
python prepare-deployment.py

echo "Driver config script"
python driver-config.py

echo "Apply did-btcr secrets"
python substitute-btcr-driver-values.py --url "$RPC_URL_TESTNET" --cert "$RPC_CERT_TESTNET"

echo "## Deployment Folder ##"
cd deploy
cat deployment-driver-did-btcr.yaml
ls -al .

echo "### Deploying following Specs ### "
cat deploy.sh


./deploy.sh
