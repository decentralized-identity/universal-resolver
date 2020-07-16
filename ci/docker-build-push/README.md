# Github Action

GitHub Action for building and publishing a Docker container to Docker Hub.


## Secrets

- `DOCKER_USERNAME` - *Required* Name of Docker Hub user which has **Write access**
- `DOCKER_PASSWORD` - *Required* Password of the Docker Hub user

Setup secrets in your GitHub project at "Settings > Secrets"

## Environment Variables


- `CONTAINER_TAG` : **mandatory**, example: 'universalresolver/driver-did-btcr:latest'
- `DOCKER_FILE` : **optional**, default is **Dockerfile**


## Example


```yaml
name: CI/CD Workflow for driver-did-btcr

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
    - name: Docker Build and Push
      uses: .ci/docker-build-push
      env:
        DOCKER_USERNAME: ${{secrets.DOCKER_USERNAME}}
        DOCKER_PASSWORD: ${{secrets.DOCKER_PASSWORD}}
        DOCKER_FILE: docker/Dockerfile
        CONTAINER_TAG: universalresolver/driver-did-btcr:latest
```

## LICENSE

Copyright (c) 2020

Licensed under the Apache2 License.