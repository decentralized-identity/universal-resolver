#!/bin/sh

echo "Smoketest for the Universal Resolver"

echo "Current folder"
pwd
ls -al

echo "Deployment folder"
ls -al deploy

echo "Root folder"
ls -al /

echo "#### Ingress file ####"
cat /github/workspace/deploy/uni-resolver-ingress.yaml

python --version

cd ci/smoke-test
python ./smoke-test.py

