#!/bin/bash

################################################################################
# Clean Up Orphaned Deployments
#
# This script identifies and removes Kubernetes resources that are no longer
# defined in the docker-compose.yml file (the source of truth).
#
# Orphaned resources are those that:
# - Have the label 'managed-by=github-action'
# - Do NOT have corresponding services in docker-compose.yml
# - Are NOT in the exclusion list (special deployments managed separately)
#
# Excluded deployments:
# - uni-resolver-frontend: Web UI deployed separately, not in docker-compose.yml
#
# The script removes:
# - Orphaned Deployments
# - Orphaned Services
# - Service-specific ConfigMaps (not the global app-config)
#
# Prerequisites:
# - kubectl must be configured and authenticated
# - NAMESPACE environment variable must be set
# - services.json must exist (created by parse-compose.sh)
#
# Usage:
#   NAMESPACE=uni-resolver ./cleanup-orphaned.sh
################################################################################

set -e

# Validate prerequisites
if [ -z "$NAMESPACE" ]; then
    echo "Error: NAMESPACE environment variable is not set"
    exit 1
fi

if [ ! -f "services.json" ]; then
    echo "Error: services.json not found. Run parse-compose.sh first."
    exit 1
fi

echo "===================================================================="
echo "Checking for orphaned deployments in namespace: $NAMESPACE"
echo "===================================================================="

# Deployments that are not in docker-compose.yml but should be kept
# These are special deployments managed separately
EXCLUDED_DEPLOYMENTS="uni-resolver-frontend"

# Get all deployments managed by this GitHub Action
kubectl get deployments -n "$NAMESPACE" -l managed-by=github-action -o json 2>/dev/null | \
    jq -r '.items[].metadata.name' > managed_deployments.txt || touch managed_deployments.txt

# Get all services defined in docker-compose.yml
cat services.json | jq -r '.name' > compose_services.txt

# Check if there are any managed deployments
if [ ! -s managed_deployments.txt ]; then
    echo "No managed deployments found in namespace"
    exit 0
fi

echo "Managed deployments in cluster:"
cat managed_deployments.txt

echo ""
echo "Services in docker-compose.yml:"
cat compose_services.txt

echo ""
echo "Comparing deployments with docker-compose.yml..."

# Track if any orphans were found
orphans_found=0

# Find and remove orphaned deployments
while read -r deployment; do
    # Check if this deployment is in the exclusion list
    if echo "$EXCLUDED_DEPLOYMENTS" | grep -qw "$deployment"; then
        echo ""
        echo "ℹ Skipping deployment: $deployment (excluded from cleanup)"
        continue
    fi

    # Check if this deployment exists in docker-compose.yml
    if ! grep -q "^${deployment}$" compose_services.txt; then
        echo ""
        echo "⚠ Found orphaned deployment: $deployment"
        echo "  This deployment is not defined in docker-compose.yml"
        echo "  Removing deployment and associated resources..."

        orphans_found=$((orphans_found + 1))

        # Delete deployment
        if kubectl delete deployment "$deployment" -n "$NAMESPACE" --ignore-not-found=true; then
            echo "  ✓ Deployment '$deployment' deleted"
        else
            echo "  ✗ Failed to delete deployment '$deployment'"
        fi

        # Delete associated service
        if kubectl delete service "$deployment" -n "$NAMESPACE" --ignore-not-found=true; then
            echo "  ✓ Service '$deployment' deleted"
        else
            echo "  ℹ No service found for '$deployment' (this is OK)"
        fi

        # Delete service-specific configmap (not the global app-config)
        # Only delete configmaps that match the pattern ${deployment}-config
        if kubectl get configmap "${deployment}-config" -n "$NAMESPACE" &>/dev/null; then
            if kubectl delete configmap "${deployment}-config" -n "$NAMESPACE" --ignore-not-found=true; then
                echo "  ✓ ConfigMap '${deployment}-config' deleted"
            fi
        fi

        echo "  ✓ Cleanup completed for '$deployment'"
    fi
done < managed_deployments.txt

echo ""
echo "===================================================================="
if [ $orphans_found -eq 0 ]; then
    echo "✓ No orphaned deployments found"
    echo "  All managed deployments match docker-compose.yml"
else
    echo "✓ Cleanup completed: $orphans_found orphaned deployment(s) removed"
fi
echo "===================================================================="
