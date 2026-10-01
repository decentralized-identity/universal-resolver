import { HTTP_TIMEOUT_MS, USER_AGENT } from '../config.ts';

export interface PullRequest {
  number: number;
  title: string;
  htmlUrl: string;
  headSha: string;
  baseSha: string;
}

/** Minimal unauthenticated GitHub client for public repositories. */
export class GitHubClient {
  readonly repository: string;

  constructor(repository: string) {
    this.repository = repository;
  }

  async pullRequest(number: number): Promise<PullRequest> {
    const url = `https://api.github.com/repos/${this.repository}/pulls/${number}`;
    const response = await fetch(url, {
      headers: { 'User-Agent': USER_AGENT, Accept: 'application/vnd.github+json' },
      signal: AbortSignal.timeout(HTTP_TIMEOUT_MS),
    });
    if (!response.ok) throw new Error(`GET ${url} returned HTTP ${response.status}`);
    const pr = (await response.json()) as {
      number: number;
      title: string;
      html_url: string;
      head: { sha: string };
      base: { sha: string };
    };
    return { number: pr.number, title: pr.title, htmlUrl: pr.html_url, headSha: pr.head.sha, baseSha: pr.base.sha };
  }

  /** Content of a file at a commit, undefined if it doesn't exist. Also serves commits of pull requests from forks. */
  async fileContent(sha: string, path: string): Promise<Buffer | undefined> {
    const url = `https://raw.githubusercontent.com/${this.repository}/${sha}/${path.split('/').map(encodeURIComponent).join('/')}`;
    const response = await fetch(url, { headers: { 'User-Agent': USER_AGENT }, signal: AbortSignal.timeout(HTTP_TIMEOUT_MS) });
    if (response.status === 404) return undefined;
    if (!response.ok) throw new Error(`GET ${url} returned HTTP ${response.status}`);
    return Buffer.from(await response.arrayBuffer());
  }
}
