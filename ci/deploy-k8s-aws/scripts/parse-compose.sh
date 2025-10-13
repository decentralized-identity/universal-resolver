#!/bin/bash

################################################################################
# Parse docker-compose.yml
#
# This script parses the docker-compose.yml file and extracts service
# information into a JSON file (services.json) for further processing.
#
# Extracted information:
# - Service name
# - Container image (with tag)
# - Exposed ports
# - Environment variables
# - Environment file references
# - Volumes
# - Command
# - Restart policy
#
# Usage:
#   ./parse-compose.sh [path-to-docker-compose.yml]
#
# Output:
#   services.json - JSON array containing service definitions
################################################################################

set -e

# Default docker-compose file location
COMPOSE_FILE="${1:-docker-compose.yml}"

# Validate that docker-compose.yml exists
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "Error: docker-compose.yml not found at $COMPOSE_FILE"
    exit 1
fi

echo "Parsing $COMPOSE_FILE..."

# Extract services information using yq
# For each service, create a JSON object with all relevant fields
yq eval -o=json '.services | to_entries | .[] | {
  "name": .key,
  "image": .value.image,
  "ports": .value.ports,
  "environment": .value.environment,
  "env_file": .value.env_file,
  "volumes": .value.volumes,
  "command": .value.command,
  "restart": .value.restart
}' "$COMPOSE_FILE" > services.json

# Validate JSON output
if [ ! -s services.json ]; then
    echo "Error: Failed to parse services from docker-compose.yml"
    exit 1
fi

echo "Services parsed successfully:"
cat services.json

echo ""
echo "Total services found: $(cat services.json | jq -s 'length')"
