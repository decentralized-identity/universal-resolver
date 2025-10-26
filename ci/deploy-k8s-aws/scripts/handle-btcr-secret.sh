#!/bin/bash

################################################################################
# Handle driver-did-btcr Secret
#
# This script handles the special case for the driver-did-btcr service, which
# requires specific RPC configuration via environment variables.
#
# The script:
# - Checks if driver-did-btcr is deployed
# - Creates/updates environment variables for RPC URL and certificate
# - Patches the deployment to include these environment variables
#
# Prerequisites:
# - kubectl must be configured and authenticated
# - NAMESPACE environment variable must be set
# - RPC_URL_TESTNET and RPC_CERT_TESTNET environment variables should be set
# - processed_services.txt must exist (created by deploy-services.sh)
#
# Usage:
#   NAMESPACE=uni-resolver \
#   RPC_URL_TESTNET="https://..." \
#   RPC_CERT_TESTNET="cert-data" \
#   ./handle-btcr-secret.sh
################################################################################

set -e

# Validate prerequisites
if [ -z "$NAMESPACE" ]; then
    echo "Error: NAMESPACE environment variable is not set"
    exit 1
fi

if [ ! -f "processed_services.txt" ]; then
    echo "Warning: processed_services.txt not found, skipping btcr secret handling"
    exit 0
fi

# Check if driver-did-btcr is in the processed services
if ! grep -q "driver-did-btcr" processed_services.txt 2>/dev/null; then
    echo "driver-did-btcr not found in processed services, skipping secret handling"
    exit 0
fi

echo "===================================================================="
echo "Handling special configuration for driver-did-btcr"
echo "===================================================================="

# Validate that required environment variables are set
if [ -z "$RPC_URL_TESTNET" ]; then
    echo "Warning: RPC_URL_TESTNET environment variable is not set"
    echo "driver-did-btcr may not function correctly without RPC configuration"
fi

if [ -z "$RPC_CERT_TESTNET" ]; then
    echo "Warning: RPC_CERT_TESTNET environment variable is not set"
    echo "driver-did-btcr may not function correctly without RPC certificate"
fi

# Check if deployment exists
if ! kubectl get deployment driver-did-btcr -n "$NAMESPACE" &>/dev/null; then
    echo "Error: driver-did-btcr deployment not found in namespace $NAMESPACE"
    exit 1
fi

echo "✓ driver-did-btcr deployment found"

# Create a secret for the BTCR driver configuration
# This secret will contain the RPC URL and certificate
echo "Creating/updating secret for driver-did-btcr..."

# Create secret YAML
cat << EOF > driver-did-btcr-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: driver-did-btcr-secret
  namespace: ${NAMESPACE}
  labels:
    app: driver-did-btcr
    managed-by: github-action
type: Opaque
stringData:
  rpc-url: "${RPC_URL_TESTNET}"
  rpc-cert: "${RPC_CERT_TESTNET}"
EOF

# Apply the secret
kubectl apply -f driver-did-btcr-secret.yaml
echo "✓ Secret 'driver-did-btcr-secret' created/updated"

# Patch the deployment to add environment variables from the secret
echo "Patching driver-did-btcr deployment to use secret..."

# Create a patch to add environment variables from the secret
# This will add RPC_URL_TESTNET and RPC_CERT_TESTNET to the container
cat << 'EOF' > btcr-env-patch.yaml
spec:
  template:
    spec:
      containers:
      - name: driver-did-btcr
        env:
        - name: RPC_URL_TESTNET
          valueFrom:
            secretKeyRef:
              name: driver-did-btcr-secret
              key: rpc-url
        - name: RPC_CERT_TESTNET
          valueFrom:
            secretKeyRef:
              name: driver-did-btcr-secret
              key: rpc-cert
EOF

# Apply the patch using strategic merge
if kubectl patch deployment driver-did-btcr -n "$NAMESPACE" --patch-file btcr-env-patch.yaml; then
    echo "✓ Deployment patched successfully"

    # Wait for the rollout to complete
    echo "Waiting for deployment to restart with new configuration..."
    if kubectl rollout status deployment/driver-did-btcr -n "$NAMESPACE" --timeout=300s; then
        echo "✓ driver-did-btcr is ready with updated configuration"
    else
        echo "⚠ Warning: Rollout status check timed out, but deployment may still succeed"
    fi
else
    echo "⚠ Warning: Failed to patch deployment, secret was created but not mounted"
    echo "You may need to manually update the deployment to use the secret"
fi

echo "===================================================================="
echo "driver-did-btcr secret handling completed"
echo "===================================================================="
