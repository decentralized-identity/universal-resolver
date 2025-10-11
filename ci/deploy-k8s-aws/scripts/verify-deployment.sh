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

################################################################################
# Slack Notification Function
################################################################################

send_slack_notification() {
    local service=$1
    local pod=$2
    local waiting_state=$3
    local logs=$4

    # Skip if webhook URL not set
    if [ -z "$SLACK_WEBHOOK_URL" ]; then
        echo "    ⚠ Skipping Slack notification (SLACK_WEBHOOK_URL not set)"
        return
    fi

    # Build GitHub workflow URL
    local workflow_url="${GITHUB_SERVER_URL:-https://github.com}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID}"

    # Build JSON payload using jq for proper escaping
    local slack_payload=$(jq -n \
        --arg service "$service" \
        --arg pod "$pod" \
        --arg waiting_state "$waiting_state" \
        --arg namespace "$NAMESPACE" \
        --arg logs "$logs" \
        --arg workflow_url "$workflow_url" \
        '{
          "username": "K8s Deployment Bot",
          "icon_emoji": ":warning:",
          "blocks": [
            {
              "type": "header",
              "text": {
                "type": "plain_text",
                "text": "⚠️ Pod in Waiting State Detected",
                "emoji": true
              }
            },
            {
              "type": "section",
              "fields": [
                {
                  "type": "mrkdwn",
                  "text": ("*Service:*\n`" + $service + "`")
                },
                {
                  "type": "mrkdwn",
                  "text": ("*Pod:*\n`" + $pod + "`")
                },
                {
                  "type": "mrkdwn",
                  "text": ("*Waiting State:*\n`" + $waiting_state + "`")
                },
                {
                  "type": "mrkdwn",
                  "text": ("*Namespace:*\n`" + $namespace + "`")
                }
              ]
            },
            {
              "type": "section",
              "text": {
                "type": "mrkdwn",
                "text": ("*Pod Logs (last 50 lines):*\n```" + $logs + "```")
              }
            },
            {
              "type": "actions",
              "elements": [
                {
                  "type": "button",
                  "text": {
                    "type": "plain_text",
                    "text": "View Workflow Run",
                    "emoji": true
                  },
                  "url": $workflow_url,
                  "style": "primary"
                }
              ]
            }
          ]
        }'
    )

    # Send to Slack
    echo "    📤 Sending Slack notification..."
    response=$(curl -s -X POST "$SLACK_WEBHOOK_URL" \
        -H "Content-Type: application/json" \
        -d "$slack_payload")

    if [ "$response" = "ok" ]; then
        echo "    ✓ Slack notification sent successfully"
    else
        echo "    ✗ Failed to send Slack notification: $response"
    fi
}

echo "===================================================================="
echo "Verifying deployment status in namespace: $NAMESPACE"
echo "===================================================================="
echo ""

# Create temporary directory for parallel processing results
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Function to check a single service
check_service() {
    local service=$1
    local output_file="$TEMP_DIR/${service}.txt"
    local status_file="$TEMP_DIR/${service}.status"
    local issues_file="$TEMP_DIR/${service}.issues"

    {
        echo "Checking service: $service"

        # Check if deployment exists
        if ! kubectl get deployment "$service" -n "$NAMESPACE" &>/dev/null; then
            echo "  ✗ Deployment not found"
            echo "unhealthy" > "$status_file"
            echo "Deployment not found" > "$issues_file"
            return
        fi

        # Get deployment status
        ready=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
        desired=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "0")
        available=$(kubectl get deployment "$service" -n "$NAMESPACE" -o jsonpath='{.status.availableReplicas}' 2>/dev/null || echo "0")

        # Handle empty responses (convert to 0)
        ready=${ready:-0}
        desired=${desired:-1}
        available=${available:-0}

        # Check for terminated pods
        terminated_pods=$(kubectl get pods -n "$NAMESPACE" -l app="$service" -o json 2>/dev/null | \
            jq -r '.items[] | select(.status.phase == "Failed" or .status.phase == "Succeeded" or
                   (.status.containerStatuses[]? | select(.state.terminated != null))) | .metadata.name' 2>/dev/null || echo "")

        running_pods=$(kubectl get pods -n "$NAMESPACE" -l app="$service" -o json 2>/dev/null | \
            jq -r '.items[] | select(.status.phase == "Running") | .metadata.name' 2>/dev/null || echo "")

        # Check for waiting pods with issues
        waiting_pods=$(kubectl get pods -n "$NAMESPACE" -l app="$service" -o json 2>/dev/null | \
            jq -r '.items[] | select(.status.phase == "Pending" or
                   (.status.containerStatuses[]? | select(.state.waiting != null)) or
                   (.status.initContainerStatuses[]? | select(.state.waiting != null))) | .metadata.name' 2>/dev/null || echo "")

        # Handle terminated pods
        if [ ! -z "$terminated_pods" ]; then
            if [ ! -z "$running_pods" ]; then
                # Running pods exist, clean up terminated ones
                echo "  ⚠ Found terminated pods, cleaning up..."
                for pod in $terminated_pods; do
                    reason=$(kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.reason}' 2>/dev/null || \
                             kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.containerStatuses[0].state.terminated.reason}' 2>/dev/null || echo "Terminated")
                    echo "    Deleting terminated pod: $pod (Reason: $reason)"
                    kubectl delete pod "$pod" -n "$NAMESPACE" --wait=false 2>/dev/null || true
                done
            else
                # No running pods, this is an issue
                echo "  ✗ Found terminated pods with no running replacements"
                for pod in $terminated_pods; do
                    reason=$(kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.reason}' 2>/dev/null || \
                             kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.containerStatuses[0].state.terminated.reason}' 2>/dev/null || echo "Terminated")
                    echo "    Terminated pod: $pod (Reason: $reason)"
                    echo "TERMINATED|$pod|$reason" >> "$issues_file"
                done
            fi
        fi

        # Handle waiting pods
        if [ ! -z "$waiting_pods" ]; then
            echo "  ⚠ Found pods in waiting state"
            for pod in $waiting_pods; do
                # Check init containers first
                init_reason=$(kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.initContainerStatuses[?(@.state.waiting)].state.waiting.reason}' 2>/dev/null || echo "")
                init_message=$(kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.initContainerStatuses[?(@.state.waiting)].state.waiting.message}' 2>/dev/null || echo "")

                # Check regular containers
                container_reason=$(kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.containerStatuses[?(@.state.waiting)].state.waiting.reason}' 2>/dev/null || echo "")
                container_message=$(kubectl get pod "$pod" -n "$NAMESPACE" -o jsonpath='{.status.containerStatuses[?(@.state.waiting)].state.waiting.message}' 2>/dev/null || echo "")

                # Use whichever has a value
                reason="${init_reason:-$container_reason}"
                message="${init_message:-$container_message}"

                if [ -z "$reason" ]; then
                    reason="PodInitializing"
                fi

                echo "    Waiting pod: $pod (Reason: $reason)"
                if [ ! -z "$message" ]; then
                    echo "      Message: $message"
                fi

                # Get logs from the pod (if available)
                echo "    Fetching logs..."
                logs=$(kubectl logs "$pod" -n "$NAMESPACE" --tail=50 --all-containers=true 2>&1 || echo "No logs available")

                # Truncate logs if too long (keep last 1500 chars for Slack)
                # Handle both short and long logs properly
                if [ ${#logs} -gt 1500 ]; then
                    logs_truncated="${logs: -1500}"
                else
                    logs_truncated="$logs"
                fi

                # Save to temp file for Slack notification
                echo "$logs" > "$TEMP_DIR/${service}_${pod}.logs"

                # Send Slack notification
                send_slack_notification "$service" "$pod" "$reason" "$logs_truncated"

                echo "WAITING|$pod|$reason|$message|$logs_truncated" >> "$issues_file"
            done
        fi

        # Check if deployment is healthy
        if [ "$ready" == "$desired" ] && [ "$available" == "$desired" ]; then
            echo "  ✓ Healthy: $ready/$desired replicas ready"

            # Mark as healthy only if no unresolved terminated pods
            if [ ! -f "$issues_file" ]; then
                echo "healthy" > "$status_file"
            else
                echo "unhealthy" > "$status_file"
            fi

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
            echo "unhealthy" > "$status_file"
            echo "Unhealthy: $ready/$desired replicas ready, $available available" >> "$issues_file"

            # Show pod status for debugging
            echo "    Pod status:"
            kubectl get pods -n "$NAMESPACE" -l app="$service" -o wide 2>/dev/null || echo "    No pods found"

            # Show recent events
            echo "    Recent events:"
            kubectl get events -n "$NAMESPACE" --field-selector involvedObject.name="$service" --sort-by='.lastTimestamp' | tail -5 || echo "    No events found"
        fi

        echo ""
    } > "$output_file"
}

# Launch parallel checks for all services
pids=()
for service in $(cat services.json | jq -r '.name'); do
    check_service "$service" &
    pids+=($!)
done

# Wait for all background processes to complete
echo "Checking $(cat services.json | jq -s 'length') services in parallel..."
for pid in "${pids[@]}"; do
    wait "$pid"
done
echo ""

# Display results in order
for service in $(cat services.json | jq -r '.name'); do
    if [ -f "$TEMP_DIR/${service}.txt" ]; then
        cat "$TEMP_DIR/${service}.txt"
    fi
done

# Count healthy/unhealthy services from status files
failed=0
total=0
for service in $(cat services.json | jq -r '.name'); do
    total=$((total + 1))
    if [ -f "$TEMP_DIR/${service}.status" ]; then
        status=$(cat "$TEMP_DIR/${service}.status")
        if [ "$status" != "healthy" ]; then
            failed=$((failed + 1))
        fi
    else
        # Service check didn't complete
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

# Check for deployments with issues
deployments_with_issues=()
for service in $(cat services.json | jq -r '.name'); do
    if [ -f "$TEMP_DIR/${service}.issues" ]; then
        deployments_with_issues+=("$service")
    fi
done

# Display deployments with issues if any exist
if [ ${#deployments_with_issues[@]} -gt 0 ]; then
    echo "===================================================================="
    echo "Deployments with Issues"
    echo "===================================================================="
    echo ""
    for service in "${deployments_with_issues[@]}"; do
        echo "Service: $service"
        echo ""

        # Separate terminated and waiting issues
        has_terminated=false
        has_waiting=false
        has_other=false

        # Check what types of issues exist
        if grep -q "^TERMINATED|" "$TEMP_DIR/${service}.issues" 2>/dev/null; then
            has_terminated=true
        fi
        if grep -q "^WAITING|" "$TEMP_DIR/${service}.issues" 2>/dev/null; then
            has_waiting=true
        fi
        if grep -qv "^TERMINATED|" "$TEMP_DIR/${service}.issues" 2>/dev/null && \
           grep -qv "^WAITING|" "$TEMP_DIR/${service}.issues" 2>/dev/null; then
            has_other=true
        fi

        # Display terminated pods
        if [ "$has_terminated" = true ]; then
            echo "  Terminated State:"
            grep "^TERMINATED|" "$TEMP_DIR/${service}.issues" 2>/dev/null | while IFS='|' read -r type pod reason; do
                echo "    - Pod: $pod"
                echo "      Reason: $reason"
            done
            echo ""
        fi

        # Display waiting pods
        if [ "$has_waiting" = true ]; then
            echo "  Waiting State:"
            grep "^WAITING|" "$TEMP_DIR/${service}.issues" 2>/dev/null | while IFS='|' read -r type pod reason message logs; do
                echo "    - Pod: $pod"
                echo "      Reason: $reason"
                if [ ! -z "$message" ]; then
                    echo "      Message: $message"
                fi
                if [ -f "$TEMP_DIR/${service}_${pod}.logs" ]; then
                    echo "      Logs (last 50 lines):"
                    cat "$TEMP_DIR/${service}_${pod}.logs" | sed 's/^/        /'
                fi
            done
            echo ""
        fi

        # Display other issues (unhealthy, deployment not found, etc.)
        if [ "$has_other" = true ]; then
            echo "  Other Issues:"
            grep -v "^TERMINATED|" "$TEMP_DIR/${service}.issues" 2>/dev/null | \
            grep -v "^WAITING|" 2>/dev/null | while read -r issue; do
                echo "    - $issue"
            done
            echo ""
        fi
    done
fi

echo "===================================================================="

# Determine exit code based on failures
if [ "$failed" -eq 0 ]; then
    echo "✓ All deployments are healthy!"
    exit 0
else
    echo "✗ Warning: $failed out of $total deployments are not fully ready"
    echo "  Check the logs above for details"
    exit 1
fi
