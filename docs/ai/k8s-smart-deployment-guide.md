# Kubernetes Smart Deployment GitHub Action

## Quick Reference

### For Different Clusters
Update cluster configuration in `ci/deploy-k8s-aws/scripts/entrypoint.sh`:
```bash
export EKS_CLUSTER_NAME="your-cluster-name"  # Line 47
export AWS_REGION="your-region"              # Line 48
```

### Common Tasks
- **View logs**: Check "Deployments with Issues" section in GitHub Actions output
- **Debug connection**: Look for detailed debug info after connection failure
- **Test locally**: Run individual scripts with proper environment variables set
- **Update dependencies**: Modify `ci/deploy-k8s-aws/Dockerfile` for tool versions

### Key Scripts
- `entrypoint.sh`: Main orchestration script (7 steps)
- `parse-compose.sh`: Parses docker-compose.yml to JSON
- `deploy-services.sh`: Handles service deployment
- `verify-deployment.sh`: Parallel health checks and Slack notifications
- `cleanup-orphaned.sh`: Removes old resources

## Key Features

1. **Dynamic kubectl Version Matching**: Automatically installs kubectl version matching your EKS cluster version
2. **Version Checking**: Compares deployed pod versions with docker-compose.yml and only updates mismatched versions
3. **Selective Updates**: Only deploys/updates containers that have changed, avoiding unnecessary downtime
4. **Orphan Cleanup**: Removes deployments not present in docker-compose.yml
5. **ConfigMap Management**: Automatically creates ConfigMaps from `.env` file
6. **Special Case Handling**: Manages the Secret for `driver-did-btcr` container
7. **Parallel Verification**: Checks all service health in parallel for faster deployment validation
8. **Pod State Management**: Automatically detects and handles terminated/waiting pods
9. **Slack Integration**: Sends notifications for pods in problematic waiting states with logs
10. **Comprehensive Issue Tracking**: Categorizes and reports deployment issues by type

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


## Implementation Details

### Workflow Structure

The GitHub Action follows these steps:

**Step 0: Dynamic kubectl Installation**
- Queries EKS cluster version using AWS CLI
- Downloads and installs matching kubectl version
- Ensures kubectl compatibility with cluster (±1 minor version)
- Fixes deprecated kubeconfig apiVersion (v1alpha1 -> v1beta1) automatically

**Step 1: Setup Kubernetes Configuration**
- Decodes kubeconfig from base64
- Configures AWS region and credentials
- Verifies kubectl connectivity to cluster

**Step 2: Parse docker-compose.yml**
- Extracts service definitions using yq (outputs JSON format)
- Creates services.json for processing

**Step 3: Process Environment Variables**
- Reads .env file and creates ConfigMaps
- Injects environment variables into containers

**Step 4: Deploy or Update Services**
- Compares current deployments with desired state
- Only updates services with version changes
- Creates new deployments for new services

**Step 5: Handle Special Cases**
- Manages driver-did-btcr Secret deployment
- Mounts secrets as volumes

**Step 6: Clean Up Orphaned Resources**
- Removes deployments no longer in docker-compose.yml
- Cleans up associated services and configmaps

**Step 7: Verify Deployment Status (Parallel)**
- Checks all services concurrently using background processes
- Detects and handles terminated pods (auto-cleanup if running replacement exists)
- Identifies pods in waiting states (CrashLoopBackOff, ImagePullBackOff, etc.)
- Extracts pod logs for debugging
- Sends Slack notifications for pods in waiting states
- Generates comprehensive issue summary categorized by type

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

## Advanced Features (Version 2.0)

### Dynamic kubectl Version Management

**Problem Solved**: Kubernetes version skew policy requires kubectl to be within ±1 minor version of the API server. Using a static kubectl version can cause connection failures when the cluster is upgraded.

**Solution**:
- Queries EKS cluster version via `aws eks describe-cluster`
- Downloads matching kubectl version before attempting cluster connection
- Automatically fixes deprecated kubeconfig apiVersion issues
- Configures AWS region as environment variable for aws-iam-authenticator

**Configuration**:
- Cluster name: `dif-universal-resolver-prod` (hardcoded in entrypoint.sh line 47)
- AWS region: `us-east-2` (hardcoded in entrypoint.sh line 48)
- Update these values when deploying to different clusters

**Files Modified**:
- `ci/deploy-k8s-aws/Dockerfile`: Removed static kubectl installation, updated aws-iam-authenticator to v0.6.30
- `ci/deploy-k8s-aws/scripts/entrypoint.sh`: Added Step 0 for dynamic kubectl installation

### Parallel Deployment Verification

**Problem Solved**: Sequential health checks for 20+ services took 40-60 seconds, slowing down the deployment pipeline.

**Solution**:
- Each service verification runs as a background process
- All services checked concurrently
- Results written to temporary files and displayed in order
- Reduces verification time from 40-60s to ~5s

**Implementation**:
- `check_service()` function runs in parallel for all services
- Uses temporary directory (`mktemp -d`) for result storage
- `wait` ensures all checks complete before summary

**Files Modified**:
- `ci/deploy-k8s-aws/scripts/verify-deployment.sh`: Refactored verification logic (lines 49-145)

### Pod State Management

**Terminated Pods**:
Automatically detects pods in terminated states:
- Failed
- Succeeded
- Completed
- Error
- OOMKilled
- ContainerCannotRun
- DeadlineExceeded
- Evicted
- ContainerStatusUnknown
- PreStopHookError

**Behavior**:
- If running replacement exists: Auto-deletes terminated pod (cleanup)
- If no running replacement: Adds to "Deployments with Issues" report

**Waiting Pods**:
Detects pods stuck in waiting states:
- PodInitializing
- ContainerCreating
- CrashLoopBackOff
- ImagePullBackOff
- ErrImagePull
- ErrImageNeverPull
- CreateContainerConfigError
- InvalidImageName
- CreateContainerError
- PreCreateHookError
- PostStartHookError
- RunContainerError

**Behavior**:
- Extracts last 50 lines of pod logs using `kubectl logs`
- Displays logs in verification summary
- Sends Slack notification with logs and workflow link
- Adds to "Deployments with Issues" report

**Files Modified**:
- `ci/deploy-k8s-aws/scripts/verify-deployment.sh`: Added pod state detection (lines 77-151)

### Slack Notification Integration

**Purpose**: Real-time alerts for pods in problematic waiting states during deployment.

**Notification Content**:
- Service name and pod name
- Waiting state reason (e.g., CrashLoopBackOff)
- Namespace
- Pod logs (last 50 lines, truncated to 1500 chars)
- "View Workflow Run" button linking directly to GitHub Actions run

**Configuration**:
- Webhook URL: Provided via `SLACK_WEBHOOK_URL` environment variable (from Vault)
- Posts to the channel configured in the webhook URL
- Automatically includes GitHub context (repository, run ID, workflow URL)

**JSON Payload Construction**:
- Uses `jq` to build payload with proper escaping
- Handles special characters, quotes, newlines in logs
- Uses Slack Block Kit format for rich formatting

**Files Modified**:
- `ci/deploy-k8s-aws/scripts/verify-deployment.sh`: Added `send_slack_notification()` function (lines 44-140)
- `ci/deploy-k8s-aws/action.yml`: Added inputs for Slack webhook and GitHub context (lines 31-61)
- `.github/workflows/kubernetes-deploy-to-cluster.yml`: Passes Slack webhook and GitHub context (lines 34-37)

### Issue Categorization and Reporting

**Deployments with Issues Section**:
The verification summary now includes a comprehensive "Deployments with Issues" section that categorizes problems:

1. **Terminated State**:
   - Lists pods without running replacements
   - Shows termination reason

2. **Waiting State**:
   - Lists pods stuck in waiting states
   - Shows waiting reason and message
   - Displays last 50 lines of logs

3. **Other Issues**:
   - Deployment not found
   - Unhealthy replica counts
   - Other deployment problems

**Example Output**:
```
====================================================================
Deployments with Issues
====================================================================

Service: driver-did-example

  Waiting State:
    - Pod: driver-did-example-7b5c9d8f6-xyz12
      Reason: CrashLoopBackOff
      Message: Back-off restarting failed container
      Logs (last 50 lines):
        exec /usr/local/bin/python: exec format error

  Other Issues:
    - Unhealthy: 1/2 replicas ready, 1 available
```

**Files Modified**:
- `ci/deploy-k8s-aws/scripts/verify-deployment.sh`: Enhanced summary section (lines 377-424)

## Configuration Requirements

### Cluster-Specific Configuration

Update these hardcoded values in `ci/deploy-k8s-aws/scripts/entrypoint.sh` for your cluster:
```bash
export EKS_CLUSTER_NAME="dif-universal-resolver-prod"  # Line 47
export AWS_REGION="us-east-2"                          # Line 48
```

### Required Secrets and Environment Variables

**GitHub Secrets**:
- `KUBE_CONFIG_DATA_BASE64_UNI_RESOLVER_PROD`: Base64-encoded kubeconfig

**Vault Secrets** (via `hashicorp/vault-action@v3`):
- `AWS_ACCESS_KEY_ID`: AWS access key for EKS authentication
- `AWS_SECRET_ACCESS_KEY`: AWS secret key for EKS authentication
- `RPC_URL_TESTNET`: RPC URL for driver-did-btcr (optional)
- `RPC_CERT_TESTNET`: RPC certificate for driver-did-btcr (optional)
- `SLACK_WEBHOOK_URL`: Slack incoming webhook URL for notifications (optional)

**GitHub Context** (automatically provided):
- `github.server_url`: GitHub server URL
- `github.repository`: Repository name (owner/repo)
- `github.run_id`: Workflow run ID

### Docker Image Requirements

The action runs in a Debian Bookworm container with:
- AWS CLI v2
- aws-iam-authenticator v0.6.30
- kubectl (dynamically installed to match cluster)
- yq (latest)
- jq
- curl

### Slack Webhook Setup

**Option 1: Use Existing Webhook**
If your `SLACK_WEBHOOK_URL` is already configured, no additional setup needed. Notifications will post to the channel configured in the webhook.

**Option 2: Create New Webhook**
1. Go to https://api.slack.com/messaging/webhooks
2. Create a Slack app or use existing
3. Add "Incoming Webhook" integration
4. Select your desired channel
5. Copy webhook URL and store in Vault
6. Update Vault secret path in workflow

**Note**: The Slack app needs permission to post to channels if you want to override the default channel.

## Troubleshooting

### kubectl Connection Failures

**Symptoms**: `Failed to connect to Kubernetes cluster` error

**Solutions**:
1. Verify kubeconfig is valid and base64-encoded correctly
2. Check AWS credentials have EKS access permissions
3. Ensure cluster name and region match your cluster
4. Verify aws-iam-authenticator is compatible with cluster version

**Debug Information**:
The script now provides detailed debug output on connection failure:
- kubectl version
- aws-iam-authenticator version and location
- AWS CLI version
- AWS region configuration

### Empty Logs in Slack

**Cause**: Log truncation issues with bash substring operations on short strings

**Solution**: Implemented proper conditional truncation that only truncates logs exceeding 1500 characters

### Parse Errors in services.json

**Cause**: yq output was in YAML format but jq expected JSON

**Solution**: Added `-o=json` flag to yq command in `parse-compose.sh` (line 41)

### Slow Verification

**Cause**: Sequential service checks

**Solution**: Implemented parallel verification using background processes, reducing time from 40-60s to ~5s

## Future Enhancement Possibilities

- [ ] Support for health checks and readiness probes
- [ ] Automated rollback on deployment failure
- [ ] Blue-green deployment strategies
- [ ] Resource limits and requests from docker-compose
- [ ] Persistent volume handling
- [ ] Multi-cluster deployment support
- [ ] Deployment metrics and analytics
- [ ] Integration with other notification services (Teams, Discord, PagerDuty)
- [ ] Custom retry logic for failed pods
- [ ] Automated image vulnerability scanning before deployment

## Version History

### Version 2.0 (2025-10-10)
**Major Enhancements**:
- Added dynamic kubectl version matching with EKS cluster
- Implemented parallel deployment verification (40-60s → ~5s)
- Added comprehensive pod state management (terminated/waiting states)
- Integrated Slack notifications for pods in waiting states
- Enhanced issue tracking and categorization in summary
- Fixed kubeconfig apiVersion compatibility (v1alpha1 → v1beta1)
- Updated aws-iam-authenticator to v0.6.30
- Fixed yq output format for proper JSON parsing
- Improved log extraction and display in Slack notifications
- Added detailed debug output for connection failures

**Files Modified**:
- `ci/deploy-k8s-aws/Dockerfile`
- `ci/deploy-k8s-aws/action.yml`
- `ci/deploy-k8s-aws/scripts/entrypoint.sh`
- `ci/deploy-k8s-aws/scripts/parse-compose.sh`
- `ci/deploy-k8s-aws/scripts/verify-deployment.sh`
- `.github/workflows/kubernetes-deploy-to-cluster.yml`

**Breaking Changes**:
- Cluster name and region now hardcoded (update in entrypoint.sh for different clusters)
- kubectl no longer installed in Dockerfile (dynamically installed per deployment)
- Requires additional GitHub context variables (server_url, repository, run_id)

### Version 1.0 (Initial Release)
**Core Features**:
- Smart deployment with version checking
- Selective updates for changed containers
- Orphan cleanup
- ConfigMap management
- Special case handling for driver-did-btcr
- Basic deployment verification

**Initial Implementation**:
Created by Claude Opus via chat interface as replacement for namespace recreation strategy.