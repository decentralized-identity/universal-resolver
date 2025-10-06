# Kubernetes Smart Deployment GitHub Action

## Key Features

1. **Version Checking**: Compares deployed pod versions with docker-compose.yml and only updates mismatched versions
2. **Selective Updates**: Only deploys/updates containers that have changed, avoiding unnecessary downtime
3. **Orphan Cleanup**: Removes deployments not present in docker-compose.yml
4. **ConfigMap Management**: Automatically creates ConfigMaps from `.env` file
5. **Special Case Handling**: Manages the Secret for `driver-did-btcr` container

## How It Works

### 1. Smart Deployment Logic

- Parses your `docker-compose.yml` to extract service definitions
- Checks existing deployments in your namespace
- Only updates deployments when image versions differ
- Creates new deployments for services not yet deployed
- Removes orphaned deployments no longer in docker-compose.yml

### 2. Environment Variable Management

- Reads `.env` file and creates a ConfigMap named `app-config`
- Automatically injects environment variables into containers
- Respects both `.env` file and inline environment variables from docker-compose.yml

### 3. Resource Management

- Creates Kubernetes Deployments from docker-compose services
- Automatically creates Services for containers with exposed ports
- Labels all resources with `managed-by: github-action` for easy tracking

### 4. Special Cases

- Handles the `driver-did-btcr` Secret deployment
- Mounts secrets as volumes in the appropriate container

## Configuration Required

1. **Update the namespace**: Change the `NAMESPACE` environment variable to your actual namespace

2. **Secret paths**: Adjust the secret file paths for `driver-did-btcr` based on your repository structure

3. **Trigger conditions**: Modify the `on:` section based on when you want deployments to trigger

## Benefits Over Namespace Recreation

- **Zero downtime** for unchanged services
- **Faster deployments** - only updates what's necessary
- **Resource efficiency** - maintains persistent volumes and configurations
- **Better observability** - tracks what actually changed
- **Rollback capability** - can revert individual service updates

## Usage

Simply replace your current AWS deployment action with this new action. It will:
1. Read your `docker-compose.yml`
2. Compare with current cluster state
3. Apply only necessary changes
4. Clean up any orphaned resources
5. Verify deployment health

The action provides a summary at the end showing the status of all deployments and services, making it easy to verify successful deployment.

## Additional Feature Possibilities

Would you like me to add any additional features, such as:
- Support for health checks
- Rollback mechanisms
- Blue-green deployment strategies
- Resource limits and requests based on docker-compose settings
- Persistent volume handling

## Implementation Details

### Workflow Structure

The GitHub Action follows these steps:

1. **Checkout repository** - Gets the latest code including docker-compose.yml and .env files
2. **Install required tools** - Sets up yq for YAML parsing and jq for JSON processing
3. **Parse docker-compose.yml** - Extracts service definitions into a processable format
4. **Process environment variables** - Creates/updates ConfigMaps from .env file
5. **Deploy or Update Services** - Main logic for comparing and updating deployments
6. **Handle driver-did-btcr Secret** - Special case for secret management
7. **Clean up orphaned deployments** - Removes resources no longer in docker-compose.yml
8. **Verify deployment status** - Ensures all deployments are healthy
9. **Summary** - Provides deployment summary in GitHub Actions UI

### Key Functions

#### `extract_image_info()`
Extracts image name and tag from Docker image strings, defaulting to "latest" if no tag is specified.

#### `create_deployment_yaml()`
Generates Kubernetes Deployment manifests from docker-compose service definitions, including:
- Container specifications
- Port mappings
- Environment variable configurations
- Image pull policies (Always for "latest", IfNotPresent for versioned)

#### `create_service_yaml()`
Creates Kubernetes Service manifests for services with exposed ports, mapping external ports to container ports.

### Version Comparison Logic

The action performs intelligent version checking:
1. Fetches current image from deployed pods
2. Compares with image specified in docker-compose.yml
3. Only triggers update if versions differ
4. Uses `kubectl rollout status` to ensure successful deployment

### Cleanup Strategy

Orphaned resources are identified by:
1. Listing all deployments with `managed-by=github-action` label
2. Comparing with services defined in docker-compose.yml
3. Removing deployments, services, and configmaps not in source of truth

### Error Handling

- Uses `set -e` for fail-fast behavior
- Implements timeout for rollout status checks
- Provides detailed logging for debugging
- Always generates summary even on failure