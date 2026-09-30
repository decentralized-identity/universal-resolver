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
    const pr = (await this.api(`pulls/${number}`)) as {
      number: number;
      title: string;
      html_url: string;
      head: { sha: string };
      base: { sha: string };
    };
    return { number: pr.number, title: pr.title, htmlUrl: pr.html_url, headSha: pr.head.sha, baseSha: pr.base.sha };
  }

  /** Files changed by the pull request; renamed files are listed with their old and new name. */
  async changedFiles(number: number): Promise<string[]> {
    const names: string[] = [];
    for (let page = 1; ; page++) {
      const files = (await this.api(`pulls/${number}/files?per_page=100&page=${page}`)) as {
        filename: string;
        previous_filename?: string;
      }[];
      for (const file of files) names.push(file.filename, ...(file.previous_filename ? [file.previous_filename] : []));
      if (files.length < 100) return names;
    }
  }

  /** Content of a file at a commit, undefined if it doesn't exist. Also serves commits of pull requests from forks. */
  async fileContent(sha: string, path: string): Promise<Buffer | undefined> {
    const url = `https://raw.githubusercontent.com/${this.repository}/${sha}/${path.split('/').map(encodeURIComponent).join('/')}`;
    const response = await fetch(url, { headers: { 'User-Agent': USER_AGENT }, signal: AbortSignal.timeout(HTTP_TIMEOUT_MS) });
    if (response.status === 404) return undefined;
    if (!response.ok) throw new Error(`GET ${url} returned HTTP ${response.status}`);
    return Buffer.from(await response.arrayBuffer());
  }

  private async api(path: string): Promise<unknown> {
    const url = `https://api.github.com/repos/${this.repository}/${path}`;
    const response = await fetch(url, {
      headers: { 'User-Agent': USER_AGENT, Accept: 'application/vnd.github+json' },
      signal: AbortSignal.timeout(HTTP_TIMEOUT_MS),
    });
    if (!response.ok) throw new Error(`GET ${url} returned HTTP ${response.status}`);
    return response.json();
  }
}
