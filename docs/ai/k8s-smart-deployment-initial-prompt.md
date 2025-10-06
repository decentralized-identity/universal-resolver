We are running a K8s cluster with just 1 Namespace. The containers/pods to deploy are taken from a docker-compose.yml file (this is the source of truth). Around half of the images are with tags latest and to make sure we have the current version of the containers deployed, we went for the approach to delete and recreate the whole Namespace. We are currently in the process of phasing out the latest tags so the original reasoning of pulling every image is not of a concern anymore. In fact we are seeing performance issues and downtime with the approach to recreate the whole Namespace on every deployment. We do have the need to deploy certain containers for test purpose from time to time. In addition there will be changes to the version number of the versioned containers.

This should be done with a Github Action (replace the current AWS deployment Action). This Action already has access to the respective Cluster and has kubectl installed.

We want to achieve following:
* Check the deployed versions of the pods against the versions of the docker-compose.yml file. If they mismatch use the one in the docker-compose.yml *
* Check if there are containers in the docker-compose.yml file that are not deployed. If yes, then deploy them
* Check if there are containers deployed that are not in the docker-compose.yml file. If yes, then remove the deployment and other resources (Services, ConfigMaps, ...)

There is also a .env file which holds the environment variables but currently we don't utilise this. In fact we are having a custom script with a custom yml file where we manually add the env variables we need (most of containers have default values in the Dockerfile that work just fine).

Here we want to achieve following:
* Use env variables from .env file and the mappings inside the docker-compose.yml and create ConfigMaps out of them

In addition there is a exceptional case for the application driver-did-btcr where we need a Secret deployed for one container. The secrets are part of the Github repo and are already retrieved in the AWS Kubernetes deployment Action.