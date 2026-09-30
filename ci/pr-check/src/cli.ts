import { parseArgs } from 'node:util';
import { DEFAULT_REPOSITORY } from './config.ts';

export type Options =
  | { source: 'local'; path: string; changedFiles?: string; baseCompose?: string; report?: string }
  | { source: 'github'; pr: number; repository: string; report?: string };

export const USAGE = `Usage: pr-check [--path DIR | --pr NUMBER] [options]

Checks the Universal Resolver config files (application.yml, docker-compose.yml, .env, README.md)
of a pull request for malformed content and non-public images.

Options:
  --path DIR              Repository root containing the files to check (default: .)
  --changed-files FILE    File listing the files changed by the pull request, one per line (with --path)
  --base-compose FILE     docker-compose.yml of the base branch, to find services added by the pull request (with --path)
  --pr NUMBER             Download the files of this pull request from GitHub and check them
  --repository OWNER/NAME GitHub repository of the pull request (default: ${DEFAULT_REPOSITORY})
  --report FILE           Also write the Markdown report to this file
  -h, --help              Show this help`;

export class UsageError extends Error {}

/** Empty values count as not given, so the GitHub Action can always pass every option. */
const given = (value: string | undefined): string | undefined => (value ? value : undefined);

export function parseOptions(args: string[]): Options | 'help' {
  let values;
  try {
    ({ values } = parseArgs({
      args,
      strict: true,
      options: {
        path: { type: 'string' },
        'changed-files': { type: 'string' },
        'base-compose': { type: 'string' },
        pr: { type: 'string' },
        repository: { type: 'string' },
        report: { type: 'string' },
        help: { type: 'boolean', short: 'h' },
      },
    }));
  } catch (e) {
    throw new UsageError((e as Error).message);
  }
  if (values.help) return 'help';

  const report = given(values.report);
  const pr = given(values.pr);
  if (pr === undefined) {
    if (given(values.repository)) throw new UsageError('--repository requires --pr');
    return {
      source: 'local',
      path: given(values.path) ?? '.',
      changedFiles: given(values['changed-files']),
      baseCompose: given(values['base-compose']),
      report,
    };
  }

  for (const option of ['path', 'changed-files', 'base-compose'] as const) {
    if (given(values[option])) throw new UsageError(`--${option} cannot be combined with --pr`);
  }
  if (!/^[1-9][0-9]*$/.test(pr)) throw new UsageError(`--pr must be a pull request number, got "${pr}"`);
  const repository = given(values.repository) ?? DEFAULT_REPOSITORY;
  if (!/^[\w.-]+\/[\w.-]+$/.test(repository)) throw new UsageError(`--repository must be OWNER/NAME, got "${repository}"`);
  return { source: 'github', pr: Number(pr), repository, report };
}
