#!/usr/bin/env bash

echo "Pulling did-status-generator image"
docker pull oydeu/did-status-generator:latest

echo "Running did-status-generator image"
docker run --rm oydeu/did-status-generator:latest > result.json
echo "Set result.json to result_var"
result_var=$(cat result.json)

echo "Checkout to did-lint-reports branch"
git fetch
git switch did-lint-reports --force

echo "Replace old result.json with result_var"
echo "$result_var" > result.json

echo "Push result file to repo"
git config --global user.email "admin@danubetech.com"
git config --global user.name "DID Lint check workflow"
git status
git add result.json
git commit -m "DID Lint check reports"
git push origin did-lint-reports:did-lint-reports
