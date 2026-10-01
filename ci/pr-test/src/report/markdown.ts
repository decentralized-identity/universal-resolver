import { MAX_STARTUP_LOG_CHARS } from '../config.ts';
import type { LogExcerpt, ServiceStartup, TestReport } from './report.ts';

export const FOOTER = [
  'This is an automated check of your PR.',
  'For more details refer to https://github.com/decentralized-identity/universal-resolver/blob/main/docs/driver-development.md',
];

/** A fenced code block whose fence is longer than any backtick run in the content. */
export function codeBlock(content: string, language = 'text'): string[] {
  const longest = Math.max(2, ...[...content.matchAll(/`+/g)].map((m) => m[0].length));
  const fence = '`'.repeat(longest + 1);
  return [`${fence}${language}`, content, fence];
}

/** The end of a long log, so the report stays within the size limit of a GitHub comment. */
export function truncateLog(log: string, maxChars = MAX_STARTUP_LOG_CHARS): { text: string; truncated: boolean } {
  const trimmed = log.trimEnd();
  if (trimmed.length <= maxChars) return { text: trimmed, truncated: false };
  const tail = trimmed.slice(-maxChars);
  return { text: tail.slice(tail.indexOf('\n') + 1), truncated: true };
}

function heading(report: TestReport): string {
  if (report.passed) return '### ✅ PR test passed';
  const failures = [
    report.problems.length ? `${report.problems.length} problem(s)` : '',
    report.startups.some((s) => !s.started) ? `${report.startups.filter((s) => !s.started).length} container(s) not started` : '',
    report.failedCount ? `${report.failedCount} of ${report.results.length} test identifier(s) failed` : '',
  ].filter(Boolean);
  return `### ❌ PR test failed: ${failures.join(', ')}`;
}

const excerpt = ({ service, lines }: LogExcerpt): string[] => [
  '',
  `Log of \`${service}\`:`,
  '',
  ...codeBlock(lines.length ? lines.join('\n') : '(no log lines during the request)'),
];

function startupLog(startup: ServiceStartup): string[] {
  const { text, truncated } = truncateLog(startup.log);
  const lineCount = text ? text.split('\n').length : 0;
  return [
    '',
    `#### Startup log of \`${startup.service}\``,
    '',
    '<details>',
    `<summary>${lineCount} line(s)${truncated ? ', truncated to the last part (full log in the workflow run)' : ''}</summary>`,
    '',
    ...codeBlock(text || '(empty)'),
    '',
    '</details>',
  ];
}

export function toMarkdown(report: TestReport): string {
  const lines = [heading(report)];
  if (report.subject) lines.push('', report.subject);

  if (report.nothingToTest) {
    lines.push('', 'Nothing to test: the PR changes no driver entry in `application.yml` and no service in `docker-compose.yml`.');
  } else {
    lines.push('', 'Tested drivers:', '');
    lines.push(...report.tested.map((t) => `- \`${t.pattern}\`: ${t.reason}`));

    if (report.startups.length || report.results.length) {
      lines.push('', '| Test | Status |', '|------|--------|');
      for (const s of report.startups) lines.push(`| Start \`${s.service}\` | ${s.started ? '✅' : '❌'} ${s.status} |`);
      for (const r of report.results) {
        const status = r.ok ? `✅ resolved (${(r.durationMs / 1000).toFixed(1)} s)` : `❌ ${r.reason ?? 'failed'}`;
        lines.push(`| Resolve \`${r.did}\` | ${status.replaceAll('|', '\\|')} |`);
      }
    }

    for (const problem of report.problems) {
      lines.push('', `- ❌ ${problem.message}`);
      for (const log of problem.logs ?? []) lines.push(...excerpt(log).map((line) => (line ? `  ${line}` : line)));
    }

    for (const result of report.results.filter((r) => !r.ok)) {
      lines.push('', `#### \`${result.did}\``, '', `❌ ${result.reason ?? 'failed'}`);
      for (const log of result.logs) lines.push(...excerpt(log));
    }

    for (const startup of report.startups) lines.push(...startupLog(startup));
  }

  // Each footer line as its own paragraph, so it's also on its own line on GitHub
  lines.push('', '---');
  for (const line of FOOTER) lines.push('', line);
  return `${lines.join('\n')}\n`;
}
