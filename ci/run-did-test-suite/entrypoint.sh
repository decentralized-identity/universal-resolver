#!/bin/sh

echo "#### Run did did testsuite ####"

echo "Running with parameters:"
sh -c "echo $*"

echo "host: $INPUT_HOST"
echo "driver_status_report: $INPUT_DRIVER_STATUS_REPORT"

node index.js --HOST="$INPUT_HOST" --driver_status_report="$INPUT_DRIVER_STATUS_REPORT"

echo "Push report file to repo"
git config --global user.email "admin@danubetech.com"
git config --global user.name "Get driver status workflow"
git add .
git commit -m "Did testsuite report" && git push