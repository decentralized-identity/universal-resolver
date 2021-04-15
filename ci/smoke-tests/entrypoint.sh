#!/bin/sh

echo "#### Smoketest for the Universal Resolver Kubernetes Deployment ####"

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

python --version

python /smoke-tests/smoke-test.py --host "$INPUT_HOST" --config "$INPUT_CONFIG" --out "$INPUT_OUT_FOLDER"

if "$INPUT_KEEP_RESULT";
  then
    echo "Push result file to repo"
    git config --global user.email "admin@danubetech.com"
    git config --global user.name "Smoke tests workflow"
    git add . && git commit -m "Smoke test results" && git push
  else
    cat -b /smoke-tests/smoke-tests-result-*.json
fi