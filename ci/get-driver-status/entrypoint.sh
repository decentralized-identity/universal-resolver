#!/bin/sh

echo "#### Driver Status for a Universal Resolver Deployment ####"

echo "Running with parameters:"
sh -c "echo $*"

echo "host: $INPUT_HOST"
echo "config: $INPUT_CONFIG"
echo "out folder: $INPUT_OUT"
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
REPORTS_FOLDER="$INPUT_OUT/nightly-run-$DATE_WITH_TIME"
mkdir -p "$REPORTS_FOLDER"

python --version
python /get-driver-status/get-driver-status.py --host "$INPUT_HOST" --config "$INPUT_CONFIG" --out "$REPORTS_FOLDER"

echo "Switch to drivers-status-reports branch"
git config --global --add safe.directory /github/workspace
git fetch
git checkout -b driver-status-reports origin/driver-status-reports

if "$INPUT_KEEP_RESULT";
  then
    echo "Push result file to repo"
    git config --global user.email "admin@danubetech.com"
    git config --global user.name "Get driver status workflow"
    # Pass driver_status_report to next step in github action
    echo "driver_status_report=$(git diff --name-only --staged)" >> "$GITHUB_ENV"
    echo "reports_folder=$REPORTS_FOLDER" >> "$GITHUB_ENV"
    git add .
    git commit -m "Get driver status results"
    git push origin driver-status-reports:driver-status-reports
  else
    cat -b /driver-status-reports/driver-status-*.json
fi
