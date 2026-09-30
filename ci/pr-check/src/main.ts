#!/usr/bin/env node
import { writeFile } from 'node:fs/promises';
import { parseOptions, USAGE, UsageError } from './cli.ts';
import { runChecks } from './checks/index.ts';
import type { CheckInput } from './input/input.ts';
import { loadPullRequestInput } from './input/github-pr.ts';
import { loadLocalInput } from './input/local.ts';
import { writeActionOutputs } from './lib/github-actions.ts';
import { toMarkdown } from './report/markdown.ts';

/**
 * Exit codes: 0 when the check ran (passed or failed, see the report, so the GitHub Action can
 * publish it), 1 for invalid arguments, 2 if the input could not be loaded.
 */
async function main(args: string[]): Promise<number> {
  let options;
  try {
    options = parseOptions(args);
  } catch (e) {
    if (!(e instanceof UsageError)) throw e;
    console.error(`${e.message}\n\n${USAGE}`);
    return 1;
  }
  if (options === 'help') {
    console.log(USAGE);
    return 0;
  }

  let input: CheckInput;
  try {
    input = options.source === 'github' ? await loadPullRequestInput(options) : await loadLocalInput(options);
  } catch (e) {
    const what = options.source === 'github' ? `PR #${options.pr} from ${options.repository}` : 'the input files';
    console.error(`Could not load ${what}: ${(e as Error).message}`);
    return 2;
  }

  try {
    const report = await runChecks(input);
    const markdown = toMarkdown(report);
    console.log(markdown);
    if (options.report) await writeFile(options.report, markdown);
    await writeActionOutputs(
      {
        result: report.passed ? 'success' : 'failure',
        errors: report.errorCount,
        warnings: report.warningCount,
        report: markdown,
      },
      markdown,
    );
    return 0;
  } finally {
    await input.dispose();
  }
}

process.exitCode = await main(process.argv.slice(2));
