# Universal Resolver — Continuous Integration and Delivery

This section describes the building-blocks and ideas of the implemented CI/CD pipeline. In case of issues or requests in the scope of CI/CD, please directly consult the maintainers of the repository.

## Intro

The CI/CD pipeline helps achieving the following goals:
* Detection of problems asap
* Short and robust release cycles
* Avoidance of repetitive, manual tasks
* Increase of Software Quality 

After every code change the CI/CD pipeline builds all software packages/Docker containers automatically. Once the containers are built, they are automatically deployed to the dev-system. By these measures building as well as deployment issues are immediately discovered. Once the freshly built containers are deployed, automatic tests are run in order to verify the software and to detect functional issues.

## Building Blocks

The CI/CD pipeline is constructed by using GitHub Actions. The workflow (workflow file https://github.com/philpotisk/universal-resolver/blob/master/.github/workflows/universal-resolver-ws.yml ) is run after every push to the master and on ever PR against master.
The workflow consists of several steps. Each step is implemented as Docker container that performs the relevant actions. Currently, the two main steps are: 

1. Building the resolver
2. Deploying the resolver

The build-step uses this container https://github.com/philpotisk/github-action-docker-build-push, which generically builds a Docker image and pushes it to Docker Hub at https://hub.docker.com/u/universalresolver
The second step takes the image and deploys it (create or update) to the configured Kubernetes cluster. The Docker container fulfilling this step can be found here: https://github.com/philpotisk/github-action-deploy-eks

## Steps of the CI/CD Workflow

![](https://user-images.githubusercontent.com/55081379/68245944-2a78db00-0018-11ea-8ebe-22c19d5ad096.PNG)

1. Dev pushes code to GitHub
2. GitHub Actions (GHA) is triggerd by the „push“ event, clones the repo and runs the workflow for every container:
    * (a) Docker build
    * (b) Docker push (Docker image goes to DockerHub)
    * (c) Deploy to Kubernetes
    * (d) Runs a test-container
3. Manual and automated feedback
