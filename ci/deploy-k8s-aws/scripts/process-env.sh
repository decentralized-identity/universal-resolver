#!/bin/bash

################################################################################
# Process Environment Variables
#
# This script creates a Kubernetes ConfigMap from the .env file.
# The ConfigMap is named 'app-config' and can be referenced by deployments
# to inject environment variables into containers.
#
# Prerequisites:
# - kubectl must be configured and authenticated
# - NAMESPACE environment variable must be set
#
# Usage:
#   NAMESPACE=uni-resolver ./process-env.sh [path-to-env-file]
#
# Output:
#   ConfigMap 'app-config' created/updated in the specified namespace
################################################################################

set -e

# Default .env file location
ENV_FILE="${1:-.env}"

# Validate namespace is set
if [ -z "$NAMESPACE" ]; then
    echo "Error: NAMESPACE environment variable is not set"
    exit 1
fi

echo "Processing environment variables from $ENV_FILE..."

TEMP_ENV_FILE=$(mktemp)
cp /dev/null "$TEMP_ENV_FILE"

append_or_replace_env_var() {
    local key="$1"
    local value="$2"
    local always="${3:-false}"
    if [ -z "$value" ] && [ "$always" != "true" ]; then
        return
    fi

    sed -i "/^${key}=.*/d" "$TEMP_ENV_FILE"
    printf '%s=%s\n' "$key" "$value" >> "$TEMP_ENV_FILE"
}

# Check if .env file exists
if [ -f "$ENV_FILE" ]; then
    cp "$ENV_FILE" "$TEMP_ENV_FILE"
fi

append_or_replace_env_var "UNIRESOLVER_DISABLED_ENTRIES" "$UNIRESOLVER_DISABLED_ENTRIES" true

if [ -s "$TEMP_ENV_FILE" ]; then
    kubectl create configmap app-config \
        --from-env-file="$TEMP_ENV_FILE" \
        --namespace="$NAMESPACE" \
        --dry-run=client -o yaml > configmap.yaml

    # Apply the ConfigMap to the cluster
    kubectl apply -f configmap.yaml

    echo "✓ ConfigMap 'app-config' created/updated"
    echo "  Total environment variables: $(grep -c "=" "$TEMP_ENV_FILE" 2>/dev/null || echo 0)"
else
    echo "Warning: No $ENV_FILE file found, skipping ConfigMap creation"
    echo "Deployments will use default environment variables from their Docker images"
fi

kubectl create secret generic app-secret \
    --from-literal=UNIRESOLVER_ENTRY_PROBE_TOKEN="$UNIRESOLVER_ENTRY_PROBE_TOKEN" \
    --namespace="$NAMESPACE" \
    --dry-run=client -o yaml > secret.yaml

kubectl apply -f secret.yaml
echo "✓ Secret 'app-secret' created/updated"

rm -f "$TEMP_ENV_FILE"
