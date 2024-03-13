#!/usr/bin/env bash

kubectl create -f namespace-setup.yaml
CLUSTER_NAME=$(kubectl config view -o=jsonpath='{.clusters[0].name}')
USER_NAME=$(kubectl config view -o=jsonpath='{.users[0].name}')

kubectl config set-context uni-resolver --namespace=uni-resolver \
  --cluster="$CLUSTER_NAME" \
  --user="$USER_NAME"
