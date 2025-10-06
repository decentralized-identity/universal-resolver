#!/bin/bash

################################################################################
# Verify Deployment Status
#
# This script verifies that all deployments defined in docker-compose.yml
# are healthy and running in the Kubernetes cluster.
#
# Health checks include:
# - Deployment exists
# - All desired replicas are ready
# - Pods are running without errors
#
# Prerequisites:
# - kubectl must be configured and authenticated
# - NAMESPACE environment variable must be set
# - services.json must exist (created by parse-compose.sh)
#
# Usage:
#   NAMESPACE=uni-resolver ./verify-deployment.sh
#
# Exit codes:
#   0 - All deployments are healthy
#   1 - One or more deployments are not healthy
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
echo "Verifying deployment status in namespace: $NAMESPACE"
echo "===================================================================="
echo ""

# Counter for failed deployments
failed=0
total=0

# Check each service defined in docker-compose.yml
cat services.json | jq -r '.name' | while read -r service; do
    total=$((total + 1))
    echo "Checking service: $service"

    # Check if deployment exists
    if ! kubectl get deployment "$service" -n "$NAMESPACE" &>/dev/null; then
        echo "  ✗ Deployment not found"
        failed=$((failed + 1))
        continue
    fi

    # Get deployment status
    ready=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
    desired=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "0")
    available=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.status.availableReplicas}' 2>/dev/null || echo "0")

    # Handle empty responses (convert to 0)
    ready=${ready:-0}
    desired=${desired:-1}
    available=${available:-0}

    # Check if deployment is healthy
    if [ "$ready" == "$desired" ] && [ "$available" == "$desired" ]; then
        echo "  ✓ Healthy: $ready/$desired replicas ready"

        # Get image information
        image=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].image}')
        echo "    Image: $image"

        # Get pod status
        pod_name=$(kubectl get pods -n "$NAMESPACE" -l app="$service" -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
        if [ ! -z "$pod_name" ]; then
            pod_status=$(kubectl get pod "$pod_name" -n "$NAMESPACE" -o jsonpath='{.status.phase}')
            echo "    Pod status: $pod_status"
        fi
    else
        echo "  ✗ Unhealthy: $ready/$desired replicas ready, $available available"
        failed=$((failed + 1))

        # Show pod status for debugging
        echo "    Pod status:"
        kubectl get pods -n "$NAMESPACE" -l app="$service" -o wide 2>/dev/null || echo "    No pods found"

        # Show recent events
        echo "    Recent events:"
        kubectl get events -n "$NAMESPACE" --field-selector involvedObject.name="$service" --sort-by='.lastTimestamp' | tail -5 || echo "    No events found"
    fi

    echo ""
done

# Read the failed count from the last iteration
# Note: Due to the pipe, the failed variable inside the loop is in a subshell
# We need to recalculate it
failed=0
total=0
cat services.json | jq -r '.name' | while read -r service; do
    total=$((total + 1))
    if kubectl get deployment "$service" -n "$NAMESPACE" &>/dev/null; then
        ready=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
        desired=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "1")
        available=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.status.availableReplicas}' 2>/dev/null || echo "0")

        ready=${ready:-0}
        desired=${desired:-1}
        available=${available:-0}

        if [ "$ready" != "$desired" ] || [ "$available" != "$desired" ]; then
            failed=$((failed + 1))
        fi
    else
        failed=$((failed + 1))
    fi
done

# Display summary
echo "===================================================================="
echo "Deployment Verification Summary"
echo "===================================================================="
echo ""

# Get deployment statistics
echo "Deployments in namespace:"
kubectl get deployments -n "$NAMESPACE" -o wide

echo ""
echo "Services in namespace:"
kubectl get services -n "$NAMESPACE" -o wide

echo ""
echo "ConfigMaps in namespace:"
kubectl get configmaps -n "$NAMESPACE"

echo ""
echo "Secrets in namespace:"
kubectl get secrets -n "$NAMESPACE"

echo ""
echo "===================================================================="

# Determine exit code based on failures
# Since we can't easily get the failed count from the loop, let's check directly
all_healthy=true
for service in $(cat services.json | jq -r '.name'); do
    if kubectl get deployment "$service" -n "$NAMESPACE" &>/dev/null; then
        ready=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
        desired=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "1")
        available=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.status.availableReplicas}' 2>/dev/null || echo "0")

        ready=${ready:-0}
        desired=${desired:-1}
        available=${available:-0}

        if [ "$ready" != "$desired" ] || [ "$available" != "$desired" ]; then
            all_healthy=false
            break
        fi
    else
        all_healthy=false
        break
    fi
done

if [ "$all_healthy" = true ]; then
    echo "✓ All deployments are healthy!"
    exit 0
else
    echo "✗ Warning: Some deployments are not fully ready"
    echo "  Check the logs above for details"
    exit 1
fi
