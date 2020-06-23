#!/bin/sh

echo "Kubernetes Deployment of the Universal Resolver"

set -e

pwd
ls -al
echo "$KUBE_CONFIG_DATA" | base64 --decode > /tmp/config
export KUBECONFIG=/tmp/config

#kubectl version --client --short

#kubectl get all --all-namespaces

cp /convert.py /k8s-template.yaml . 2>/dev/null || :
cp -r /deploy .

python --version

python convert.py

cd deploy
ls -al
./deploy.sh

#kubectl apply -f uni-resolver-ingress.yaml

#kubectl get all --all-namespaces

