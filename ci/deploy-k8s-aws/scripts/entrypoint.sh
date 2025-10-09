#!/bin/bash

################################################################################
# Kubernetes Deployment Entrypoint
#
# This is the main entrypoint script for deploying the Universal Resolver
# to a Kubernetes cluster using a smart deployment strategy.
#
# The script orchestrates the following steps:
# 1. Setup Kubernetes configuration
# 2. Parse docker-compose.yml to extract service definitions
# 3. Process environment variables and create ConfigMaps
# 4. Deploy or update services (only changes are applied)
# 5. Handle special cases (e.g., driver-did-btcr secrets)
# 6. Clean up orphaned resources
# 7. Verify all deployments are healthy
#
# Required Environment Variables:
# - KUBE_CONFIG_DATA: Base64-encoded Kubernetes config
# - NAMESPACE: Target Kubernetes namespace (default: uni-resolver)
# - AWS_ACCESS_KEY_ID: AWS access key for authentication
# - AWS_SECRET_ACCESS_KEY: AWS secret key for authentication
#
# Optional Environment Variables:
# - RPC_URL_TESTNET: RPC URL for driver-did-btcr
# - RPC_CERT_TESTNET: RPC certificate for driver-did-btcr
#
# Usage:
#   This script is typically run inside a Docker container as part of
#   a GitHub Actions workflow.
################################################################################

set -e  # Exit on error
set -o pipefail  # Catch errors in pipes

################################################################################
# Configuration and Validation
################################################################################

echo "===================================================================="
echo "Universal Resolver Kubernetes Deployment"
echo "Version: 2.0.0"
echo "===================================================================="
echo ""

# Hardcoded EKS cluster configuration
export EKS_CLUSTER_NAME="dif-universal-resolver-prod"
export AWS_REGION="us-east-2"

# Set default namespace if not provided
export NAMESPACE="${NAMESPACE:-uni-resolver}"

echo "Configuration:"
echo "  EKS Cluster: $EKS_CLUSTER_NAME"
echo "  AWS Region: $AWS_REGION"
echo "  Namespace: $NAMESPACE"
echo "  Working Directory: $(pwd)"
echo ""

# Validate required environment variables
if [ -z "$KUBE_CONFIG_DATA" ]; then
    echo "Error: KUBE_CONFIG_DATA environment variable is not set"
    echo "This variable must contain base64-encoded Kubernetes configuration"
    exit 1
fi

if [ -z "$AWS_ACCESS_KEY_ID" ]; then
    echo "Warning: AWS_ACCESS_KEY_ID is not set"
fi

if [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
    echo "Warning: AWS_SECRET_ACCESS_KEY is not set"
fi

################################################################################
# Step 0: Install kubectl matching EKS cluster version
################################################################################

echo "===================================================================="
echo "Step 0: Installing kubectl matching EKS cluster version"
echo "===================================================================="

# Configure AWS region for both AWS CLI and aws-iam-authenticator
export AWS_DEFAULT_REGION="$AWS_REGION"
aws configure set region "$AWS_REGION"

# Query EKS cluster version
echo "Querying EKS cluster version..."
CLUSTER_VERSION=$(aws eks describe-cluster --name "$EKS_CLUSTER_NAME" --region "$AWS_REGION" --query 'cluster.version' --output text)

if [ -z "$CLUSTER_VERSION" ]; then
    echo "Error: Failed to query EKS cluster version"
    exit 1
fi

echo "EKS Cluster version: v$CLUSTER_VERSION"

# Download and install matching kubectl version
echo "Downloading kubectl v$CLUSTER_VERSION..."
KUBECTL_URL="https://dl.k8s.io/release/v${CLUSTER_VERSION}.0/bin/linux/amd64/kubectl"

if curl -LO "$KUBECTL_URL" 2>/dev/null; then
    chmod +x ./kubectl
    mv ./kubectl /usr/local/bin/kubectl
    echo "✓ Successfully installed kubectl v$CLUSTER_VERSION"
else
    echo "Warning: Failed to download kubectl v$CLUSTER_VERSION, trying latest patch version..."
    # Try without the patch version
    KUBECTL_URL="https://dl.k8s.io/release/v${CLUSTER_VERSION}.1/bin/linux/amd64/kubectl"
    if curl -LO "$KUBECTL_URL" 2>/dev/null; then
        chmod +x ./kubectl
        mv ./kubectl /usr/local/bin/kubectl
        echo "✓ Successfully installed kubectl v${CLUSTER_VERSION}.1"
    else
        echo "Error: Failed to install compatible kubectl version"
        exit 1
    fi
fi

# Verify kubectl installation
kubectl version --client

echo ""

################################################################################
# Step 1: Setup Kubernetes Configuration
################################################################################

echo "===================================================================="
echo "Step 1: Setting up Kubernetes configuration"
echo "===================================================================="

# Decode and save Kubernetes config
echo "$KUBE_CONFIG_DATA" | base64 --decode > /tmp/config
export KUBECONFIG=/tmp/config

# Debug: Verify AWS credentials and region are set
echo "Verifying AWS configuration..."
echo "  AWS_DEFAULT_REGION: ${AWS_DEFAULT_REGION:-NOT SET}"
echo "  AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID:0:10}... (length: ${#AWS_ACCESS_KEY_ID})"
echo "  Kubeconfig location: $KUBECONFIG"

# Verify kubectl connectivity
echo ""
echo "Testing kubectl connectivity..."
if kubectl cluster-info 2>&1; then
    echo "✓ Successfully connected to Kubernetes cluster"
    kubectl version --short 2>/dev/null || kubectl version
else
    echo ""
    echo "✗ Failed to connect to Kubernetes cluster"
    echo ""
    echo "Debug information:"
    echo "  kubectl version:"
    kubectl version --client
    echo ""
    echo "  Checking if aws-iam-authenticator is accessible:"
    which aws-iam-authenticator
    aws-iam-authenticator version
    echo ""
    echo "  AWS CLI version:"
    aws --version
    exit 1
fi

# Verify namespace exists, create if it doesn't
echo ""
echo "Checking namespace '$NAMESPACE'..."
if kubectl get namespace "$NAMESPACE" &>/dev/null; then
    echo "✓ Namespace '$NAMESPACE' exists"
else
    echo "⚠ Namespace '$NAMESPACE' does not exist, creating..."
    kubectl create namespace "$NAMESPACE"
    echo "✓ Namespace created"
fi

echo ""

################################################################################
# Step 2: Parse docker-compose.yml
################################################################################

echo "===================================================================="
echo "Step 2: Parsing docker-compose.yml"
echo "===================================================================="

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
    echo "Error: docker-compose.yml not found in current directory"
    echo "Contents of current directory:"
    ls -la
    exit 1
fi

# Execute parse script
if /scripts/parse-compose.sh; then
    echo "✓ Successfully parsed docker-compose.yml"
else
    echo "✗ Failed to parse docker-compose.yml"
    exit 1
fi

echo ""

################################################################################
# Step 3: Process Environment Variables
################################################################################

echo "===================================================================="
echo "Step 3: Processing environment variables"
echo "===================================================================="

# Execute environment processing script
if /scripts/process-env.sh; then
    echo "✓ Successfully processed environment variables"
else
    echo "⚠ Warning: Failed to process environment variables"
    echo "Continuing with deployment..."
fi

echo ""

################################################################################
# Step 4: Deploy or Update Services
################################################################################

echo "===================================================================="
echo "Step 4: Deploying or updating services"
echo "===================================================================="

# Execute deployment script
if /scripts/deploy-services.sh; then
    echo "✓ Successfully processed all services"
else
    echo "✗ Failed to deploy services"
    exit 1
fi

echo ""

################################################################################
# Step 5: Handle Special Cases
################################################################################

echo "===================================================================="
echo "Step 5: Handling special cases"
echo "===================================================================="

# Handle driver-did-btcr secret configuration
if /scripts/handle-btcr-secret.sh; then
    echo "✓ Special case handling completed"
else
    echo "⚠ Warning: Special case handling encountered issues"
    echo "Continuing with deployment..."
fi

echo ""

################################################################################
# Step 6: Clean Up Orphaned Resources
################################################################################

echo "===================================================================="
echo "Step 6: Cleaning up orphaned resources"
echo "===================================================================="

# Execute cleanup script
if /scripts/cleanup-orphaned.sh; then
    echo "✓ Cleanup completed"
else
    echo "⚠ Warning: Cleanup encountered issues"
    echo "Continuing with verification..."
fi

echo ""

################################################################################
# Step 7: Verify Deployment Status
################################################################################

echo "===================================================================="
echo "Step 7: Verifying deployment status"
echo "===================================================================="

# Execute verification script
if /scripts/verify-deployment.sh; then
    echo ""
    echo "===================================================================="
    echo "✓ DEPLOYMENT SUCCESSFUL"
    echo "===================================================================="
    echo ""
    echo "All services have been deployed and verified successfully!"
    exit 0
else
    echo ""
    echo "===================================================================="
    echo "⚠ DEPLOYMENT COMPLETED WITH WARNINGS"
    echo "===================================================================="
    echo ""
    echo "Services were deployed but some may not be fully ready."
    echo "Check the logs above for details."
    exit 1
fi
