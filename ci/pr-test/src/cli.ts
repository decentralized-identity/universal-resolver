import { parseArgs } from 'node:util';
import { DEFAULT_REPOSITORY, DEFAULT_RESOLVE_TIMEOUT_S, DEFAULT_STARTUP_WAIT_S } from './config.ts';

interface CommonOptions {
  report?: string;
  startupWaitS: number;
  resolveTimeoutS: number;
}

export type Options =
  | (CommonOptions & { source: 'local'; path: string; basePath: string })
  | (CommonOptions & { source: 'github'; pr: number; repository: string });

export const USAGE = `Usage: pr-test (--pr NUMBER | --path DIR --base-path DIR) [options]

Starts the drivers changed by a Universal Resolver pull request together with uni-resolver-web (using the
pull request's application.yml) and resolves the drivers' test identifiers. Needs Docker with docker compose.

Options:
  --pr NUMBER             Download the files of this pull request and its base branch from GitHub
  --repository OWNER/NAME GitHub repository of the pull request (default: ${DEFAULT_REPOSITORY})
  --path DIR              Directory with application.yml, docker-compose.yml and .env of the pull request
  --base-path DIR         Directory with the same files on the base branch
  --report FILE           Also write the Markdown report to this file
  --startup-wait SECONDS  Time the containers get to start (default: ${DEFAULT_STARTUP_WAIT_S})
  --timeout SECONDS       Timeout per test identifier (default: ${DEFAULT_RESOLVE_TIMEOUT_S})
  -h, --help              Show this help

Files are expected at their repository paths, e.g. DIR/uni-resolver-web/src/main/resources/application.yml.`;

export class UsageError extends Error {}

/** Empty values count as not given, so the GitHub Action can always pass every option. */
const given = (value: string | undefined): string | undefined => (value ? value : undefined);

function seconds(value: string | undefined, option: string, fallback: number): number {
  if (!given(value)) return fallback;
  if (!/^[0-9]+$/.test(value ?? '')) throw new UsageError(`--${option} must be a number of seconds, got "${value}"`);
  return Number(value);
}

export function parseOptions(args: string[]): Options | 'help' {
  let values;
  try {
    ({ values } = parseArgs({
      args,
      strict: true,
      options: {
        pr: { type: 'string' },
        repository: { type: 'string' },
        path: { type: 'string' },
        'base-path': { type: 'string' },
        report: { type: 'string' },
        'startup-wait': { type: 'string' },
        timeout: { type: 'string' },
        help: { type: 'boolean', short: 'h' },
      },
    }));
  } catch (e) {
    throw new UsageError((e as Error).message);
  }
  if (values.help) return 'help';

  const common: CommonOptions = {
    report: given(values.report),
    startupWaitS: seconds(values['startup-wait'], 'startup-wait', DEFAULT_STARTUP_WAIT_S),
    resolveTimeoutS: seconds(values.timeout, 'timeout', DEFAULT_RESOLVE_TIMEOUT_S),
  };

  const pr = given(values.pr);
  if (pr === undefined) {
    const path = given(values.path);
    const basePath = given(values['base-path']);
    if (!path || !basePath) throw new UsageError('either --pr or both --path and --base-path are required');
    if (given(values.repository)) throw new UsageError('--repository requires --pr');
    return { ...common, source: 'local', path, basePath };
  }

  for (const option of ['path', 'base-path'] as const) {
    if (given(values[option])) throw new UsageError(`--${option} cannot be combined with --pr`);
  }
  if (!/^[1-9][0-9]*$/.test(pr)) throw new UsageError(`--pr must be a pull request number, got "${pr}"`);
  const repository = given(values.repository) ?? DEFAULT_REPOSITORY;
  if (!/^[\w.-]+\/[\w.-]+$/.test(repository)) throw new UsageError(`--repository must be OWNER/NAME, got "${repository}"`);
  return { ...common, source: 'github', pr: Number(pr), repository };
}
