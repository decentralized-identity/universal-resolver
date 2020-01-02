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
According to the [roadmap](https://github.com/philpotisk/universal-resolver/wiki/Roadmap-Universal-Resolver) major and minor releases are planned and scheduled. Patch-versions, may have happened spontaneously according to urgent bugfixes.
For each minor release there is a dedicated branch following the naming pattern: MAJOR.MINOR.x (e.g. 3.1.x). In this branch the code-base for this particular release will be hardened. The code has to pass the static code analysis, which detects potential issues with the code in terms of quality and security. Furthermore, this code will be manually deployed in a staging/ production-like environment, where manual and automatic testing are taking place. Once the release fulfills the desired standards, it will be packaged and released to the GitHub release page, and announced at (TODO: wiki/whats-new-in-universal-resolver-<MAJOR>.x).

Steps for releasing a new version:
1. Create a release branch from the master.

    `git checkout -b 3.1.x`

    `git push --set-upstream origin 3.1.x`

Open universal-resolver/.github/workflows/universal-resolver-ws.yml and adjust the CONTAINER_TAG to the specific release number. For example: 3.1.0
In the same file, change also the branch the CI/CD workflow is listening on, to 3.1.x. In future, when there will be a staging-system in place, also the KUBE_KONFIG_DATA needs to be changed, so the deployment will go to the staging system, rather to the dev-system.

2. Run Sonar Cube analytics and review results in the online dashboard. Consider all issues and make sure, that no critical issues are flagged, before releasing the software. 

3. After the build-job got executed, the Docker image should be stored at Docker Hub. You can check the website or simply run the container by specifying the tag: docker pull phil21/uni-resolver-web:3.1.0

4. Tag the release: 
    
    `git tag v3.1.0.RELEASE`

    `git push origin --tags`

Afterwards the release will be automatically shown on GitHub under the releases.

