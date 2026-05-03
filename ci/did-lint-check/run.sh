#!/usr/bin/env bash

set -euo pipefail

DID_RESOLVER="${DID_RESOLVER:-https://resolver.identity.foundation/1.0/identifiers/}"
CONFIG_FILE="${CONFIG_FILE:-uni-resolver-web/src/main/resources/application.yml}"
ENTRY_DISABLE_THRESHOLD="${ENTRY_DISABLE_THRESHOLD:-3}"
WORK_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "$WORK_DIR"
}
trap cleanup EXIT

echo "Pulling did-status-generator image"
docker pull oydeu/did-status-generator:latest

echo "Running did-status-generator image"
docker run --rm -e "DID_RESOLVER=${DID_RESOLVER}" oydeu/did-status-generator:latest > "$WORK_DIR/result.json"

echo "Loading previous report-only entry disable state"
git fetch origin
if git show origin/did-lint-reports:entry-disable-state.json > "$WORK_DIR/previous-entry-disable-state.json" 2>/dev/null; then
  echo "Found previous entry disable state"
else
  echo '{"version":1,"entries":{}}' > "$WORK_DIR/previous-entry-disable-state.json"
  echo "No previous entry disable state found"
fi

echo "Computing report-only entry disable state"
python3 ./ci/did-lint-check/entry_disable_policy.py \
  --config "$CONFIG_FILE" \
  --did-lint-result "$WORK_DIR/result.json" \
  --state "$WORK_DIR/previous-entry-disable-state.json" \
  --state-out "$WORK_DIR/entry-disable-state.json" \
  --report-out "$WORK_DIR/entry-disable-report.json" \
  --disabled-out "$WORK_DIR/disabled-entries.txt" \
  --threshold "$ENTRY_DISABLE_THRESHOLD"

echo "Checkout to did-lint-reports branch"
git checkout -B did-lint-reports origin/did-lint-reports

echo "Replace old DIDLint and report-only entry disable results"
cp "$WORK_DIR/result.json" result.json
cp "$WORK_DIR/entry-disable-state.json" entry-disable-state.json
cp "$WORK_DIR/entry-disable-report.json" entry-disable-report.json
cp "$WORK_DIR/disabled-entries.txt" disabled-entries.txt

echo "Push result file to repo"
git config --global user.email "admin@danubetech.com"
git config --global user.name "DID Lint check workflow"
git status
git add result.json entry-disable-state.json entry-disable-report.json disabled-entries.txt
if git diff --cached --quiet; then
  echo "No DIDLint report changes to push"
else
  git commit -m "DID Lint check reports"
  git push origin did-lint-reports:did-lint-reports
fi
