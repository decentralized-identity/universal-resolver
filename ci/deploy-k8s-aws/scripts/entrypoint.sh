#!/bin/sh

echo "Kubernetes Deployment of the Universal Resolver"

set -e

pwd

echo "$KUBE_CONFIG_DATA" | base64 --decode > /tmp/config
export KUBECONFIG=/tmp/config

kubectl version --client --short

kubectl get all --all-namespaces

cp /convert.py /k8s-template.yaml . 2>/dev/null || :

python --version

python convert.py

cd out

./deploy.sh

kubectl apply -f uni-resolver-ingress.yaml

kubectl get all --all-namespaces

