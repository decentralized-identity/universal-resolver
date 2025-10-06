There is a new Github workflow at .github/workflows/kubernetes-deploy-to-cluster-improved.yml which is described at docs/ai/k8s-smart-deployment-guide.md and that
solves the problems described in docs/ai/k8s-smart-deployment-initial-prompt.md

We want to refactor the Github Action that is used at ci/deploy-k8s-aws

Following steps are needed:
1. In the file ci/deploy-k8s-aws/Dockerfile we setup the environment for connection to AWS cluster. This has to be preserved
2. The new Github workflow at .github/workflows/kubernetes-deploy-to-cluster-improved.yml does everything inline with Bash. Extract these to Bash scripts and use
   them as it is done now at ci/deploy-k8s-aws/scripts/entrypoint.sh
3. Feel free to remove all .py files and .yaml files in all subfolders of ci/deploy-k8s-aws if they are not needed anymore
4. Create an action.yml file at ci/deploy-k8s-aws which uses the latest API for Github Actions (as of October 2025)
5. The workflow .github/workflows/kubernetes-deploy-to-cluster-improved.yml should be treated as an inspiration for the Bash script at ci/deploy-k8s-aws/scripts
   and the workflow .github/workflows/kubernetes-deploy-to-cluster.yml is the one that will be used after changes are applied.
6. Bash scripts should follow best coding practices with extensive comments