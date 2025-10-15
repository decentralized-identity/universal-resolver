#!/bin/bash

################################################################################
# Deploy Ingress
#
# This script ensures the uni-resolver-ingress exists in the cluster.
# If it doesn't exist, it creates it from scratch.
#
# The Ingress configures AWS ALB to route traffic to:
# - uni-resolver-web service (API endpoints at /1.0/*)
# - uni-resolver-frontend service (UI at /*)
#
# Prerequisites:
# - kubectl must be configured and authenticated
# - NAMESPACE environment variable must be set
# - AWS Load Balancer Controller must be installed in the cluster
#
# Usage:
#   NAMESPACE=uni-resolver ./deploy-ingress.sh
################################################################################

set -e

# Validate prerequisites
if [ -z "$NAMESPACE" ]; then
    echo "Error: NAMESPACE environment variable is not set"
    exit 1
fi

# Constants
INGRESS_NAME="uni-resolver-ingress"
DEV_DOMAIN_NAME="dev.uniresolver.io"
PROD_DOMAIN_NAME="resolver.identity.foundation"
CERTIFICATE_ARNS="arn:aws:acm:us-east-2:332553390353:certificate/925fce37-d446-4af3-828e-f803b3746af0,arn:aws:acm:us-east-2:332553390353:certificate/59fa30ca-de05-4024-8f80-fea9ab9ab8bf"

echo "===================================================================="
echo "Checking Ingress: $INGRESS_NAME"
echo "===================================================================="

# First, ensure required services exist and are of type NodePort
echo "Checking required services..."
echo ""

for service in uni-resolver-web uni-resolver-frontend; do
    if kubectl get service "$service" -n "$NAMESPACE" &>/dev/null; then
        service_type=$(kubectl get service "$service" -n "$NAMESPACE" -o jsonpath='{.spec.type}')
        echo "  Service '$service' exists (type: $service_type)"

        if [ "$service_type" != "NodePort" ] && [ "$service_type" != "LoadBalancer" ]; then
            echo "    ⚠ Service type is '$service_type', patching to 'NodePort'..."
            kubectl patch service "$service" -n "$NAMESPACE" -p '{"spec":{"type":"NodePort"}}'
            echo "    ✓ Service patched to NodePort"
        fi
    else
        echo "  ⚠ Warning: Service '$service' does not exist"
        echo "    The Ingress will be created but may not work until the service is deployed"
    fi
done

echo ""

# Check if ingress exists
if kubectl get ingress "$INGRESS_NAME" -n "$NAMESPACE" &>/dev/null; then
    echo "✓ Ingress '$INGRESS_NAME' already exists"
    echo ""
    echo "Current Ingress configuration:"
    kubectl get ingress "$INGRESS_NAME" -n "$NAMESPACE" -o wide
    echo ""
    echo "No action needed"
    exit 0
fi

echo "⚠ Ingress '$INGRESS_NAME' does not exist"
echo "Creating Ingress..."
echo ""

# Generate Ingress manifest
cat > ingress.yaml << EOF
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ${INGRESS_NAME}
  namespace: ${NAMESPACE}
  annotations:
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/certificate-arn: "${CERTIFICATE_ARNS}"
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS":443}]'
    alb.ingress.kubernetes.io/ssl-redirect: '443'
  labels:
    app: uni-resolver-web
    managed-by: github-action
spec:
  ingressClassName: alb
  rules:
    - host: ${DEV_DOMAIN_NAME}
      http:
        paths:
          - path: /1.0/*
            pathType: ImplementationSpecific
            backend:
              service:
                name: uni-resolver-web
                port:
                  number: 8080
          - path: /*
            pathType: ImplementationSpecific
            backend:
              service:
                name: uni-resolver-frontend
                port:
                  number: 7081
    - host: ${PROD_DOMAIN_NAME}
      http:
        paths:
          - path: /1.0/*
            pathType: ImplementationSpecific
            backend:
              service:
                name: uni-resolver-web
                port:
                  number: 8080
          - path: /*
            pathType: ImplementationSpecific
            backend:
              service:
                name: uni-resolver-frontend
                port:
                  number: 7081
EOF

echo "Generated Ingress manifest:"
cat ingress.yaml
echo ""

# Apply Ingress
if kubectl apply -f ingress.yaml; then
    echo "✓ Ingress created successfully"
else
    echo "✗ Failed to create Ingress"
    exit 1
fi

echo ""
echo "Waiting for ALB to provision (this may take 2-3 minutes)..."
echo "You can check the status with: kubectl get ingress $INGRESS_NAME -n $NAMESPACE -w"
echo ""

# Show the created ingress
kubectl get ingress "$INGRESS_NAME" -n "$NAMESPACE" -o wide

echo ""
echo "✓ Ingress deployment complete"
echo ""
echo "Note: The AWS Load Balancer Controller will provision an ALB."
echo "DNS records for ${DEV_DOMAIN_NAME} and ${PROD_DOMAIN_NAME}"
echo "must point to the ALB address shown above."
