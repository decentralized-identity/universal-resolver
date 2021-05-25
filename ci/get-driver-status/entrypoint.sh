#!/bin/sh

echo "#### Driver Status for a Universal Resolver Deployment ####"

echo "Running with parameters:"
sh -c "echo $*"

echo "host: $INPUT_HOST"
echo "config: $INPUT_CONFIG"
echo "out folder: $INPUT_OUT_FOLDER"
echo "debug: $INPUT_DEBUG"
echo "keep result: $INPUT_KEEP_RESULT"

if "$INPUT_DEBUG"; then
  echo "Current folder"
  pwd
  ls -al

  echo "Deployment folder"
  ls -al deploy

  echo "Root folder"
  ls -al /

  echo "#### Ingress file ####"
  cat /github/workspace/deploy/uni-resolver-ingress.yaml
fi

DATE_WITH_TIME=$(TZ=UTC date "+%Y-%m-%d_%H:%M:%S")
REPORTS_FOLDER="$INPUT_OUT_FOLDER/nightly-run-$DATE_WITH_TIME"
mkdir "$REPORTS_FOLDER"

python --version
python /get-driver-status/get-driver-status.py --host "$INPUT_HOST" --config "$INPUT_CONFIG" --out "$REPORTS_FOLDER"

if "$INPUT_KEEP_RESULT";
  then
    echo "Push result file to repo"
    git config --global user.email "admin@danubetech.com"
    git config --global user.name "Get driver status workflow"
    git add .
    # Pass driver_status_report to next step in github action
    echo "driver_status_report=$(git diff --name-only --staged)" >> "$GITHUB_ENV"
    echo "reports_folder=$REPORTS_FOLDER" >> "$GITHUB_ENV"
    git commit -m "Get driver status results" && git push
  else
    cat -b /driver-status-reports/driver-status-*.json
fi