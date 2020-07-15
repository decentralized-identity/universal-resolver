#!/bin/sh

echo "Kubernetes Deployment of the Universal Resolver"

set -e

echo "Root folder"
ls -al /

echo "Current Folder"
pwd
ls -al
echo "$KUBE_CONFIG_DATA" | base64 --decode > /tmp/config
export KUBECONFIG=/tmp/config

cp /prepare-deployment.py /k8s-template.yaml .

ls -al .
python --version
python prepare-deployment.py

cd deploy
ls -al
./deploy.sh
