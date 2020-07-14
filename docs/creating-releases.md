# Universal Resolver — Creating Releases

This guide explains the release process of the universal resolver.

## Version Number

The concept of versioning is in-line with Semantic Versioning as described here https://semver.org/. 
Each release is defined by the version number, which is composed by the three parts MAJOR.MINOR.PATCH. 
1. MAJOR version for incompatible API changes.
2. MINOR version when adding functionality in a backwards compatible manner.
3. PATCH version when making backwards compatible bug fixes.
Further details should be taken from the link above.

## Release Process
For each minor release there is a dedicated branch following the naming pattern: release-x.x.x (e.g. release-0.1.x). In this branch the code-base for this particular release will be tested. For this the code will be deployed in a staging environment, where manual and automatic testing are taking place. Once the release fulfills the desired standards, it will be packaged and released to the GitHub release page.

Steps for releasing a new version:
1. Create a release branch from the master.

    `git checkout -b release-0.1.x`

    `git push --set-upstream origin release-0.1.x`

Open universal-resolver/.github/workflows/universal-resolver-ws.yml and adjust the CONTAINER_TAG to the specific release number. For example: 0.1.0
In the same file, change also the branch the CI/CD workflow is listening on, to 0.1.0. Change the KUBE_KONFIG_DATA to the staging system (currently deployed on AWS).

2. After the build-job got executed, the Docker image should be stored at Docker Hub. You can check the website or simply run the container by specifying the tag: docker pull phil21/uni-resolver-web:3.1.0

3. Tag the release: 
    
    `git tag v0.1.0`

    `git push origin --tags`

Afterwards the release will be automatically shown on GitHub under the releases.

