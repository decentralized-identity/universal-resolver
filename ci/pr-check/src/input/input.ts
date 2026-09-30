/** Everything the checks run on. */
export interface CheckInput {
  /** Directory containing the checked files (as they are in the pull request). */
  root: string;
  /** Files changed by the pull request, if known. */
  changedFiles?: string[];
  /** Content of docker-compose.yml on the base branch, if known. */
  baseCompose?: string;
  /** Line describing what was checked, e.g. the pull request. */
  subject?: string;
  /** Removes temporary files. */
  dispose(): Promise<void>;
}
