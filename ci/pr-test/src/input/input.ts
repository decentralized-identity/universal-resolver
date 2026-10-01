/** The files of the pull request and of its base branch. */
export interface TestInput {
  /** Directory with application.yml, docker-compose.yml and .env as they are in the pull request. */
  head: string;
  /** Directory with the same files on the base branch, to find what the pull request changes. */
  base: string;
  /** Line describing what is tested, e.g. the pull request. */
  subject?: string;
  /** Removes temporary files. */
  dispose(): Promise<void>;
}
