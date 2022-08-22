#!/bin/sh

echo "#### Kubernetes Deployment of the Universal Resolver ####"

set -e

echo "## Root folder ##"
ls -al /

mkdir -p /tmp/kube
touch /tmp/kube/kubeconfig
export KUBECONFIG=/tmp/kube/kubeconfig
chmod 600 $KUBECONFIG

if [ "$ENVIRONMENT" = Prod ]; then export CLUSTER="civic-prod"; else export CLUSTER="civic-dev"; fi
if [ "$ENVIRONMENT" = Prod ]; then export DEPLOYMENT_DOMAIN="did.civic.com"; else export DEPLOYMENT_DOMAIN="did-dev.civic.com"; fi


echo "ENVIRONMENT: $ENVIRONMENT"
echo "CLUSTER: $CLUSTER"
echo "DEPLOYMENT_DOMAIN: $DEPLOYMENT_DOMAIN"

aws eks --region us-east-1 update-kubeconfig --name $CLUSTER

echo "## Kubeconfig ##"
kubectl config view

cp /*.py /*.yaml -r /app-specs -r /namespace . 2>/dev/null || :

echo "Current workspace Folder"
pwd
ls -al .
python --version
python prepare-deployment.py -d $DEPLOYMENT_DOMAIN

echo "Driver config script"
python driver-config.py

# re-enable if the BTCR driver is enabled
#echo "Apply did-btcr secrets"
#python substitute-btcr-driver-values.py --url "$RPC_URL_TESTNET" --cert "$RPC_CERT_TESTNET"
#
echo "## Deployment Folder ##"
cd deploy
#cat deployment-driver-did-btcr.yaml
ls -al .

echo "### Deploying following Specs ### "
cat deploy.sh

echo "### Running deployment:"

./deploy.sh
