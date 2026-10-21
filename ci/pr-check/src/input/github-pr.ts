import { mkdir, mkdtemp, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { CHECKED_FILES, DOCKER_COMPOSE } from '../config.ts';
import { GitHubClient } from '../lib/github.ts';
import type { CheckInput } from './input.ts';

/** Downloads the checked files of a pull request's head commit into a temporary directory. */
export async function loadPullRequestInput(options: { repository: string; pr: number }): Promise<CheckInput> {
  const github = new GitHubClient(options.repository);
  const pr = await github.pullRequest(options.pr);
  const root = await mkdtemp(join(tmpdir(), 'pr-check-'));
  const dispose = () => rm(root, { recursive: true, force: true });

  try {
    const downloads = Promise.all(
      CHECKED_FILES.map(async (name) => {
        const content = await github.fileContent(pr.headSha, name);
        if (content === undefined) return; // reported as "File not found"
        await mkdir(dirname(join(root, name)), { recursive: true });
        await writeFile(join(root, name), content);
      }),
    );
    const [changedFiles, baseCompose] = await Promise.all([
      github.changedFiles(pr.number),
      github.fileContent(pr.baseSha, DOCKER_COMPOSE),
      downloads,
    ]);

    return {
      root,
      changedFiles,
      baseCompose: baseCompose?.toString('utf8'),
      subject: `PR [#${pr.number}](${pr.htmlUrl}) "${pr.title}" at commit \`${pr.headSha.slice(0, 8)}\``,
      dispose,
    };
  } catch (e) {
    await dispose();
    throw e;
  }
}
