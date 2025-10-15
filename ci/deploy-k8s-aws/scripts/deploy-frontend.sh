#!/bin/bash

################################################################################
# Deploy Universal Resolver Frontend
#
# This script deploys the uni-resolver-frontend application which provides
# the web UI for the Universal Resolver. This is separate from the services
# defined in docker-compose.yml.
#
# Components deployed:
# - ConfigMap: uni-resolver-frontend (contains backend URL)
# - Deployment: uni-resolver-frontend (web UI container)
# - Service: uni-resolver-frontend (NodePort service)
#
# Prerequisites:
# - kubectl must be configured and authenticated
# - NAMESPACE environment variable must be set
#
# Usage:
#   NAMESPACE=uni-resolver ./deploy-frontend.sh
################################################################################

set -e

# Validate prerequisites
if [ -z "$NAMESPACE" ]; then
    echo "Error: NAMESPACE environment variable is not set"
    exit 1
fi

# Configuration
BACKEND_URL="https://dev.uniresolver.io/"

echo "===================================================================="
echo "Deploying Universal Resolver Frontend"
echo "===================================================================="
echo ""

################################################################################
# Step 1: Deploy ConfigMap
################################################################################

echo "Step 1: Creating/updating ConfigMap..."

cat > configmap-uni-resolver-frontend.yaml << EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: uni-resolver-frontend
  namespace: ${NAMESPACE}
  labels:
    app: uni-resolver-frontend
    managed-by: github-action
data:
  backend_url: ${BACKEND_URL}
EOF

if kubectl apply -f configmap-uni-resolver-frontend.yaml; then
    echo "✓ ConfigMap created/updated"
else
    echo "✗ Failed to create ConfigMap"
    exit 1
fi

echo ""

################################################################################
# Step 2: Deploy Frontend Application
################################################################################

echo "Step 2: Creating/updating Deployment and Service..."

cat > deployment-uni-resolver-frontend.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: uni-resolver-frontend
  namespace: ${NAMESPACE}
  labels:
    app: uni-resolver-frontend
    type: frontend
    managed-by: github-action
spec:
  replicas: 1
  selector:
    matchLabels:
      app: uni-resolver-frontend
  template:
    metadata:
      labels:
        app: uni-resolver-frontend
    spec:
      containers:
        - name: uni-resolver-frontend
          image: universalresolver/universal-resolver-frontend
          imagePullPolicy: Always
          ports:
            - containerPort: 7081
          env:
            - name: BACKEND_URL
              valueFrom:
                configMapKeyRef:
                  name: uni-resolver-frontend
                  key: backend_url
---
apiVersion: v1
kind: Service
metadata:
  name: uni-resolver-frontend
  namespace: ${NAMESPACE}
  labels:
    app: uni-resolver-frontend
    managed-by: github-action
spec:
  type: NodePort
  selector:
    app: uni-resolver-frontend
  ports:
    - protocol: TCP
      port: 7081
      targetPort: 7081
EOF

if kubectl apply -f deployment-uni-resolver-frontend.yaml; then
    echo "✓ Deployment and Service created/updated"
else
    echo "✗ Failed to create Deployment and Service"
    exit 1
fi

echo ""

################################################################################
# Step 3: Wait for Deployment to be Ready
################################################################################

echo "Step 3: Waiting for deployment to become ready..."

if kubectl rollout status deployment/uni-resolver-frontend -n "$NAMESPACE" --timeout=300s; then
    echo "✓ Frontend deployment is ready"
else
    echo "⚠ Frontend deployment rollout timed out"
    echo "  Continuing with deployment process..."
fi

echo ""

################################################################################
# Summary
################################################################################

echo "===================================================================="
echo "Frontend Deployment Summary"
echo "===================================================================="
echo ""

# Show deployment status
echo "Deployment:"
kubectl get deployment uni-resolver-frontend -n "$NAMESPACE" -o wide

echo ""
echo "Service:"
kubectl get service uni-resolver-frontend -n "$NAMESPACE" -o wide

echo ""
echo "Pods:"
kubectl get pods -n "$NAMESPACE" -l app=uni-resolver-frontend -o wide

echo ""
echo "✓ Frontend deployment complete"
