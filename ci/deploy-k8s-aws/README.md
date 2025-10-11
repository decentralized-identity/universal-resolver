# Kubernetes Deployment of the Universal Resolver

## Overview

This GitHub Action deploys the Universal Resolver to a Kubernetes cluster using a smart deployment strategy that minimizes downtime and only updates services when their container images have changed.

**Version:** 2.0.0

## Features

- **Smart Deployment**: Only updates deployments when container images differ from the running version
- **Zero Downtime**: Unchanged services continue running without interruption
- **Automatic Cleanup**: Removes orphaned deployments not defined in docker-compose.yml
- **ConfigMap Management**: Automatically creates ConfigMaps from .env file
- **Health Verification**: Verifies all deployments are healthy before completing
- **Special Case Handling**: Manages secrets for driver-did-btcr

## Architecture

The action is composed of modular bash scripts that handle different aspects of deployment:

1. **parse-compose.sh** - Parses docker-compose.yml and extracts service definitions
2. **process-env.sh** - Creates ConfigMaps from .env file
3. **deploy-services.sh** - Implements smart deployment logic
4. **handle-btcr-secret.sh** - Handles driver-did-btcr secret configuration
5. **cleanup-orphaned.sh** - Removes orphaned resources
6. **verify-deployment.sh** - Verifies deployment health
7. **entrypoint.sh** - Orchestrates all scripts

## Usage in GitHub Actions

```yaml
- name: Deploy to AWS Kubernetes Cluster
  uses: ./ci/deploy-k8s-aws
  with:
    kube-config-data: ${{ secrets.KUBE_CONFIG_DATA }}
    aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
    aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
    namespace: 'uni-resolver'
    rpc-url-testnet: ${{ secrets.RPC_URL_TESTNET }}
    rpc-cert-testnet: ${{ secrets.RPC_CERT_TESTNET }}
```

## Inputs

| Input | Description | Required | Default |
|-------|-------------|----------|---------|
| `kube-config-data` | Base64-encoded Kubernetes configuration | Yes | - |
| `aws-access-key-id` | AWS Access Key ID | Yes | - |
| `aws-secret-access-key` | AWS Secret Access Key | Yes | - |
| `namespace` | Kubernetes namespace | No | `uni-resolver` |
| `rpc-url-testnet` | RPC URL for driver-did-btcr | No | - |
| `rpc-cert-testnet` | RPC certificate for driver-did-btcr | No | - |

## How It Works

### 1. Parse docker-compose.yml
The action reads your docker-compose.yml file (the source of truth) and extracts all service definitions including:
- Container images and tags
- Port mappings
- Environment variables
- Volume mounts

### 2. Smart Deployment Logic
For each service in docker-compose.yml:
- **If deployment exists**: Compare current image with desired image
  - If different: Update deployment and wait for rollout
  - If same: Skip (no downtime)
- **If deployment doesn't exist**: Create new deployment and service

### 3. Automatic Cleanup
After deploying all services:
- Identify deployments with `managed-by=github-action` label
- Compare with services in docker-compose.yml
- Remove any orphaned resources (deployments, services, configmaps)

### 4. Health Verification
Verify that all deployments are healthy:
- Check ready replicas match desired replicas
- Display pod status and recent events
- Exit with error if any deployment is unhealthy

## Benefits Over Previous Approach

### Previous (Destructive)
- Deleted entire namespace on every deployment
- All services restarted, causing downtime
- No version checking
- Inefficient for versioned containers

### Current (Smart)
- Only updates changed services
- Zero downtime for unchanged services
- Faster deployments
- Maintains persistent state
- Better observability

## Local Development

### Build Docker Image
```bash
cd ci/deploy-k8s-aws
docker build -t ur-deployer .
```

### Run Locally
```bash
docker run -t \
  -e KUBE_CONFIG_DATA="$(cat ~/.kube/config | base64)" \
  -e AWS_ACCESS_KEY_ID="your-key" \
  -e AWS_SECRET_ACCESS_KEY="your-secret" \
  -e NAMESPACE="uni-resolver" \
  -v $(pwd)/../..:/workspace \
  -w /workspace \
  ur-deployer
```

## Directory Structure

```
ci/deploy-k8s-aws/
├── action.yml              # GitHub Action definition
├── Dockerfile             # Container image with tools
├── README.md              # This file
└── scripts/
    ├── entrypoint.sh      # Main orchestration script
    ├── parse-compose.sh   # Parse docker-compose.yml
    ├── process-env.sh     # Create ConfigMaps
    ├── deploy-services.sh # Smart deployment logic
    ├── handle-btcr-secret.sh  # Special case handling
    ├── cleanup-orphaned.sh    # Remove orphaned resources
    └── verify-deployment.sh   # Health verification
```

## Requirements

### Container Image Includes
- AWS CLI v2
- AWS IAM Authenticator
- kubectl (latest stable)
- yq (YAML processor)
- jq (JSON processor)

### Source of Truth
- **docker-compose.yml** - All services must be defined here
- **.env** - Environment variables (optional)

## Troubleshooting

### Deployment Fails
Check the logs for each step. The action provides detailed output for:
- Parsing errors
- Connection issues
- Deployment failures
- Health check failures

### Service Not Updating
Ensure the image tag in docker-compose.yml is different from the deployed version.

### Orphaned Resources Not Cleaned
Only resources with the `managed-by=github-action` label are cleaned up. Manually created resources are not affected.

## Migration from Previous Version

The previous version used Python scripts and a destructive deployment strategy. This version:
1. Uses bash scripts for better maintainability
2. Implements smart deployment logic
3. Eliminates unnecessary downtime
4. Removes dependency on manually maintained deployment specs

All services must now be defined in docker-compose.yml. The app-specs and namespace folders are no longer used.

## Version History

### 2.0.0 (Current)
- Complete refactor to bash-based approach
- Smart deployment strategy
- Automatic cleanup of orphaned resources
- ConfigMap management from .env
- Improved health verification
- Zero downtime for unchanged services

### 1.0.0 (Legacy)
- Python-based scripts
- Destructive deployment (delete namespace)
- Manual deployment spec maintenance
