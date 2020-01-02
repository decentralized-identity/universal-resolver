# Universal Resolver — Branching Strategy

The goals for our branching strategy are:
* The master-branch should be deployable at any time. This implies a stable build and the assurance that the core-functionality is provided at any time when cloning the master.
* The master-branch should stay active. As collaboratively working with multiple developers, we encourage merging the code as frequently as possible. This will potentially disclose issues at an early stage and facilitate the repair. Furthermore, it makes clear that the master-branch is the preferred choice that newcomers want to clone.
* In order not to waste time the branching strategy should stay as simple as possible.

Among a bunch of various strategies we have chosen *GitHub Flow*, which is a lightweight branching-strategy encouraged by the GitHub dev-teams. Details can be found here: https://guides.github.com/introduction/flow/
The following recipe shortly describes the typical steps that developers need to be take into account when updating the code base: 

![](https://hackernoon.com/hn-images/1*iHPPa72N11sBI_JSDEGxEA.png)

1. ***Create a branch***: When implementing a new feature, some improvement or a bugfix, a new branch should be created of the master-branch. In order to preserve an organized git-repo, the new branches should go under: `topic/<your-branch-name>` , whereas the name of the branch should reflect the intention of the branch itself. Use the GitHub-issue name as part of the branch-name, if there is a corresponding issue available.

2. ***Commit some code***: Add your changes to the new branch and commit regularly with a descriptive commit-message. This builds up a transparent history of work and makes a roll back of changes easier.
3. ***Open a Pull Request (PR)***: Once your changes are complete (or you want some feedback at an early stage of development) open a PR against the master-branch. A PR initiates a discussion with the maintainer, which will review your code at this point. Furthermore, the [[CI/CD process|Continuous-Integration-and-Delivery]] will be kicked off and your code will be deployed to the dev-system. 
4. ***Discuss, review code and deployment***: Wait for feedback of the maintainer and check the deployment at the dev-system. In case of contributing a new driver the maintainer will also add the deployment-scripts in the scope of this PR. You may also be requested to make some changes to your code. Finally, the new changes should be safely incorporated to the master-branch and the updated universal-resolver is running smoothly in dev-environment.
5. ***Merge to the master-branch***: If all parties involved in the discussion are satisfied, the maintainer will merge the PR into the master-branch and will close the PR itself. You are free to delete your branch as all changes have already been incorporated in the master.
