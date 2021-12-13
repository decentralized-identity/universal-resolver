#!/bin/sh

echo "#### Run did-test-suite ####"

echo "Running with parameters:"
sh -c "echo $*"

echo "host: $INPUT_HOST"
echo "driver_status_report: $INPUT_DRIVER_STATUS_REPORT"

node /run-did-test-suite/index.js --HOST="$INPUT_HOST" --DRIVER_STATUS_REPORT="$INPUT_DRIVER_STATUS_REPORT" --OUTPUT_PATH="$INPUT_REPORTS_FOLDER"

echo "Push report file to repo"
git status
git config --global user.email "admin@danubetech.com"
git config --global user.name "Get driver status workflow"
git fetch && git pull --ff-only
git add . && git commit -m "did-test-suite report" && git push