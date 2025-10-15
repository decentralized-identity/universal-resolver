#!/bin/bash

################################################################################
# Deploy or Update Kubernetes Services
#
# This script implements smart deployment logic that:
# - Only updates deployments when container images have changed
# - Creates new deployments for services not yet deployed
# - Creates corresponding Kubernetes Services for containers with exposed ports
# - Handles environment variables via ConfigMaps
#
# The script processes services defined in services.json (created by parse-compose.sh)
# and compares them with existing deployments in the cluster.
#
# Prerequisites:
# - services.json must exist (created by parse-compose.sh)
# - kubectl must be configured and authenticated
# - NAMESPACE environment variable must be set
# - ConfigMap 'app-config' should exist (created by process-env.sh)
#
# Usage:
#   NAMESPACE=uni-resolver ./deploy-services.sh
#
# Output:
#   - Deployments and Services created/updated in the cluster
#   - processed_services.txt - List of processed service names
################################################################################

set -e

# Validate prerequisites
if [ ! -f "services.json" ]; then
    echo "Error: services.json not found. Run parse-compose.sh first."
    exit 1
fi

if [ -z "$NAMESPACE" ]; then
    echo "Error: NAMESPACE environment variable is not set"
    exit 1
fi

echo "Starting deployment process for namespace: $NAMESPACE"

################################################################################
# Function: extract_image_info
# Extracts image name and tag from a Docker image string
#
# Arguments:
#   $1 - Full image string (e.g., "registry/image:tag")
#
# Returns:
#   Echoes: "image-name tag"
################################################################################
extract_image_info() {
    local image=$1
    local name=$(echo "$image" | cut -d: -f1 | rev | cut -d/ -f1 | rev)
    local tag=$(echo "$image" | cut -d: -f2)
    # Default to 'latest' if no tag specified
    [ "$tag" == "$image" ] && tag="latest"
    echo "$name $tag"
}

################################################################################
# Function: create_deployment_yaml
# Generates a Kubernetes Deployment manifest for a service
#
# Arguments:
#   $1 - Service name
#   $2 - Container image (full path with tag)
#   $3 - Ports (JSON array)
#   $4 - Environment variables (JSON object)
#   $5 - Environment file reference (from docker-compose)
#
# Output:
#   deployment-${service_name}.yaml
################################################################################
create_deployment_yaml() {
    local service_name=$1
    local image=$2
    local ports=$3
    local env_vars=$4
    local env_file=$5

    echo "Generating deployment manifest for $service_name..."

    # Create base deployment manifest
    cat << EOF > "deployment-${service_name}.yaml"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${service_name}
  namespace: ${NAMESPACE}
  labels:
    app: ${service_name}
    managed-by: github-action
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${service_name}
  template:
    metadata:
      labels:
        app: ${service_name}
    spec:
      containers:
      - name: ${service_name}
        image: ${image}
        imagePullPolicy: $([ "${image##*:}" == "latest" ] && echo "Always" || echo "IfNotPresent")
EOF

    # Add container ports if defined
    if [ ! -z "$ports" ] && [ "$ports" != "null" ]; then
        echo "        ports:" >> "deployment-${service_name}.yaml"
        # Parse ports and add containerPort entries
        # Handles both "host:container" and "container" port formats
        echo "$ports" | jq -r '.[] | gsub("^\""; "") | gsub("\"$"; "")' | while read -r port_mapping; do
            # Extract container port (format: "host:container" or just "container")
            container_port=$(echo "$port_mapping" | awk -F: '{print ($NF)}')
            echo "        - containerPort: $container_port" >> "deployment-${service_name}.yaml"
        done
    fi

    # Add environment variables from ConfigMap
    if [ "$env_file" != "null" ] || [ "$env_vars" != "null" ]; then
        echo "        envFrom:" >> "deployment-${service_name}.yaml"
        echo "        - configMapRef:" >> "deployment-${service_name}.yaml"
        echo "            name: app-config" >> "deployment-${service_name}.yaml"
    fi

    # Add specific environment variables if defined in docker-compose
    if [ "$env_vars" != "null" ] && [ "$env_vars" != "{}" ]; then
        echo "        env:" >> "deployment-${service_name}.yaml"
        echo "$env_vars" | jq -r 'to_entries | .[] | "        - name: " + .key + "\n          value: \"" + (.value | tostring) + "\""' >> "deployment-${service_name}.yaml"
    fi

    echo "✓ Deployment manifest created: deployment-${service_name}.yaml"
}

################################################################################
# Function: create_service_yaml
# Generates a Kubernetes Service manifest for exposing a deployment
#
# Arguments:
#   $1 - Service name
#   $2 - Ports (JSON array)
#
# Output:
#   service-${service_name}.yaml (only if ports are defined)
################################################################################
create_service_yaml() {
    local service_name=$1
    local ports=$2

    # Only create service if ports are defined
    if [ -z "$ports" ] || [ "$ports" == "null" ]; then
        return
    fi

    echo "Generating service manifest for $service_name..."

    # Create base service manifest
    cat << EOF > "service-${service_name}.yaml"
apiVersion: v1
kind: Service
metadata:
  name: ${service_name}
  namespace: ${NAMESPACE}
  labels:
    app: ${service_name}
    managed-by: github-action
spec:
  selector:
    app: ${service_name}
  ports:
EOF

    # Add port mappings
    # Handles both "host:container" and "container" port formats
    echo "$ports" | jq -r '.[] | gsub("^\""; "") | gsub("\"$"; "")' | while read -r port_mapping; do
        if [[ "$port_mapping" == *:* ]]; then
            # Format: "host:container"
            host_port=$(echo "$port_mapping" | cut -d: -f1)
            container_port=$(echo "$port_mapping" | cut -d: -f2)
        else
            # Format: "container" only
            host_port="$port_mapping"
            container_port="$port_mapping"
        fi
        echo "  - port: $host_port" >> "service-${service_name}.yaml"
        echo "    targetPort: $container_port" >> "service-${service_name}.yaml"
        echo "    protocol: TCP" >> "service-${service_name}.yaml"
    done

    echo "✓ Service manifest created: service-${service_name}.yaml"
}

################################################################################
# Main deployment logic
################################################################################

# Initialize processed services tracker
> processed_services.txt

# Initialize failed deployments tracker
> failed_deployments.txt

# Get list of current deployments in namespace
echo "Fetching current deployments in namespace $NAMESPACE..."
kubectl get deployments -n "$NAMESPACE" -o json 2>/dev/null | jq -r '.items[].metadata.name' > current_deployments.txt || touch current_deployments.txt

echo "Current deployments:"
cat current_deployments.txt

# Process each service from docker-compose.yml
echo ""
echo "Processing services from docker-compose.yml..."
cat services.json | jq -c '.' | while read -r service; do
    # Extract service properties
    name=$(echo "$service" | jq -r '.name')
    image=$(echo "$service" | jq -r '.image')
    ports=$(echo "$service" | jq -c '.ports')
    env_vars=$(echo "$service" | jq -c '.environment')
    env_file=$(echo "$service" | jq -c '.env_file')

    echo ""
    echo "===================================================================="
    echo "Processing service: $name"
    echo "  Image: $image"
    echo "===================================================================="

    # Check if deployment already exists
    if kubectl get deployment "$name" -n "$NAMESPACE" &>/dev/null; then
        echo "✓ Deployment '$name' exists, checking for updates..."

        # Get current image from deployment
        current_image=$(kubectl get deployment "$name" -n "$NAMESPACE" -o jsonpath='{.spec.template.spec.containers[0].image}')

        echo "  Current image: $current_image"
        echo "  Expected image: $image"

        # Compare images and update if different
        if [ "$current_image" != "$image" ]; then
            echo "⚠ Image mismatch detected!"
            echo "  Updating deployment with new image..."
            kubectl set image deployment/"$name" "$name=$image" -n "$NAMESPACE"

            echo "  Waiting for rollout to complete (timeout: 5 minutes)..."
            if kubectl rollout status deployment/"$name" -n "$NAMESPACE" --timeout=300s; then
                echo "✓ Deployment updated successfully"
            else
                echo "✗ Deployment rollout failed or timed out"
                echo "  ⚠ Continuing with remaining deployments..."
                echo "$name|update-timeout" >> failed_deployments.txt
            fi
        else
            echo "✓ Image is up to date, no action needed"
        fi
    else
        echo "⚡ Deployment '$name' does not exist, creating new deployment..."

        # Create deployment manifest
        create_deployment_yaml "$name" "$image" "$ports" "$env_vars" "$env_file"

        # Apply deployment
        kubectl apply -f "deployment-${name}.yaml"
        echo "✓ Deployment created"

        # Create and apply service if ports are defined
        if [ ! -z "$ports" ] && [ "$ports" != "null" ]; then
            create_service_yaml "$name" "$ports"
            kubectl apply -f "service-${name}.yaml"
            echo "✓ Service created"
        fi

        # Wait for deployment to be ready
        echo "  Waiting for deployment to become ready..."
        if kubectl rollout status deployment/"$name" -n "$NAMESPACE" --timeout=300s; then
            echo "✓ Deployment is ready"
        else
            echo "✗ Deployment failed to become ready"
            echo "  ⚠ Continuing with remaining deployments..."
            echo "$name|create-timeout" >> failed_deployments.txt
        fi
    fi

    # Mark service as processed
    echo "$name" >> processed_services.txt
done

echo ""
echo "===================================================================="
echo "All services from docker-compose.yml have been processed"
echo "===================================================================="

# Check for failed deployments
if [ -s failed_deployments.txt ]; then
    echo ""
    echo "⚠ WARNING: Some deployments encountered issues:"
    echo ""
    while IFS='|' read -r service reason; do
        case "$reason" in
            update-timeout)
                echo "  - $service: Update rollout timed out"
                ;;
            create-timeout)
                echo "  - $service: New deployment failed to become ready"
                ;;
        esac
    done < failed_deployments.txt
    echo ""
    echo "These services were deployed but may not be fully operational."
    echo "Check the verification step for current status."
    echo ""
else
    echo ""
    echo "✓ All deployments completed successfully"
    echo ""
fi
