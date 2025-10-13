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

# Check if .env file exists
if [ -f "$ENV_FILE" ]; then
    # Create a ConfigMap from .env file
    # Using --dry-run=client to generate YAML without applying it first
    kubectl create configmap app-config \
        --from-env-file="$ENV_FILE" \
        --namespace="$NAMESPACE" \
        --dry-run=client -o yaml > configmap.yaml

    # Apply the ConfigMap to the cluster
    kubectl apply -f configmap.yaml

    echo "✓ ConfigMap 'app-config' created/updated from $ENV_FILE"
    echo "  Total environment variables: $(grep -c "=" "$ENV_FILE" 2>/dev/null || echo 0)"
else
    echo "Warning: No $ENV_FILE file found, skipping ConfigMap creation"
    echo "Deployments will use default environment variables from their Docker images"
fi
