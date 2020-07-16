#!/bin/sh

echo "#### Kubernetes Deployment of the Universal Resolver ####"

set -e

echo "Root folder"
ls -al /

ls -al
echo "$KUBE_CONFIG_DATA" | base64 --decode > /tmp/config
export KUBECONFIG=/tmp/config

cp /prepare-deployment.py /k8s-template.yaml -r /app-specs -r /namespace . 2>/dev/null || :

echo "Current workspace Folder"
pwd
ls -al .
python --version
python prepare-deployment.py

cd deploy
ls -al
./deploy.sh
