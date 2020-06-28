#!/bin/sh

echo "Smoketest for the Universal Resolver"

echo "Current folder"
pwd
ls -al

echo "Deployment folder"
ls -al deploy

echo "Root folder"
ls -al /

python --version

cd ci/smoke-test
python ./smoke-test.py

