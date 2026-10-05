import { mkdir, mkdtemp, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { INPUT_FILES } from '../config.ts';
import { GitHubClient } from '../lib/github.ts';
import type { TestInput } from './input.ts';

/** Downloads the input files of a pull request's head commit and of its merge base into a temporary directory. */
export async function loadPullRequestInput(options: { repository: string; pr: number }): Promise<TestInput> {
  const github = new GitHubClient(options.repository);
  const pr = await github.pullRequest(options.pr);
  const root = await mkdtemp(join(tmpdir(), 'pr-test-'));
  const dispose = () => rm(root, { recursive: true, force: true });

  const download = async (sha: string, directory: string) => {
    await Promise.all(
      INPUT_FILES.map(async (name) => {
        const content = await github.fileContent(sha, name);
        if (content === undefined) return;
        await mkdir(dirname(join(directory, name)), { recursive: true });
        await writeFile(join(directory, name), content);
      }),
    );
  };

  try {
    await Promise.all([download(pr.headSha, join(root, 'head')), download(pr.mergeBaseSha, join(root, 'base'))]);
    return {
      head: join(root, 'head'),
      base: join(root, 'base'),
      subject: `PR [#${pr.number}](${pr.htmlUrl}) "${pr.title}" at commit \`${pr.headSha.slice(0, 8)}\``,
      dispose,
    };
  } catch (e) {
    await dispose();
    throw e;
  }
}
