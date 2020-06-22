# Kubernetes Deployment of the Universal Resolver

## Usage with Docker
Setting the environment:

    export KUBE_CONFIG_DATA=$(cat /home/pp/dev/devops/universal-resolver-kubernetes/aws/danubetech-dev-cluster10/danubetech-dev-cluster10-KUBE_CONFIG_DATA.txt)

Build:

    docker build -t ur-deployer .

Run:

    docker run -t ur-deployer


## Usage via script
Setting the environment:

    export KUBECONFIG=/home/pp/dev/devops/universal-resolver-kubernetes/aws/danubetech-dev-cluster10/kubeconfig-danubetech-dev-cluster10.yaml

Run:

    cd scripts
    ./entrypoint.sh


## Usage as GitHub Action

Github Action workflow-file:

```
name: CI/CD Workflow for universal-resolver

on:
  push:
    branches:
      - master
  pull_request:
    branches:
      - master

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@master
    - name: Deploy to AWS
      uses: philpotisk/universal-resolver-k8s-deployment@master
      env:
        KUBE_CONFIG_DATA: ${{secrets.KUBE_CONFIG_DATA}}
        AWS_ACCESS_KEY_ID: ${{secrets.AWS_ACCESS_KEY_ID}}
        AWS_SECRET_ACCESS_KEY: ${{secrets.AWS_SECRET_ACCESS_KEY}}
```