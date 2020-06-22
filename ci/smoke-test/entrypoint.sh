#!/bin/sh

echo "Smoketest for the Universal Resolver"

ls -al 

pwd

ls -al /

python --version

cd ci/smoke-test
python ./smoke-test.py

