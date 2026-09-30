import type { Report, Section } from './report.ts';

const title = (section: Section): string => (section.code ? `\`${section.name}\`` : section.name);

function status(section: Section): string {
  if (section.errors.length) return `❌ ${section.errors.length} error(s)`;
  if (section.warnings.length) return `⚠️ ${section.warnings.length} warning(s)`;
  return '✅ OK';
}

function heading(report: Report): string {
  if (!report.passed) return `### ❌ PR check failed: ${report.errorCount} error(s), ${report.warningCount} warning(s)`;
  if (report.warningCount) return `### ✅ PR check passed with ${report.warningCount} warning(s)`;
  return '### ✅ PR check passed';
}

export function toMarkdown(report: Report): string {
  const lines = [heading(report)];
  if (report.subject) lines.push('', report.subject);

  lines.push('', '| File | Status |', '|------|--------|');
  for (const section of report.sections) lines.push(`| ${title(section)} | ${status(section)} |`);

  for (const section of report.sections) {
    if (!section.findings.length) continue;
    lines.push('', `#### ${title(section)}`, '');
    lines.push(...section.errors.map((f) => `- ❌ ${f.message}`));
    lines.push(...section.warnings.map((f) => `- ⚠️ ${f.message}`));
  }
  return `${lines.join('\n')}\n`;
}
