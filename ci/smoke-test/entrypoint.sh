#!/bin/sh

echo "Smoketest for the Universal Resolver"

ls -al 

pwd

ls -al /

python --version

python ./ci/smoke-test.py

