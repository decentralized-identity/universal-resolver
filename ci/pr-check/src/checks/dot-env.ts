import type { Section } from '../report/report.ts';

const LINE = /^(?:export\s+)?([A-Za-z_][\w.-]*)\s*=(.*)$/;

/** Checks the .env syntax and returns the defined variable names. */
export function checkDotEnv(section: Section, text: string): Set<string> {
  const keys = new Map<string, number>();
  text.split(/\r?\n/).forEach((raw, index) => {
    const number = index + 1;
    const line = raw.trim();
    if (!line || line.startsWith('#')) return;

    const match = LINE.exec(line);
    if (!match) {
      section.error(`Line ${number}: expected \`KEY=value\`, found \`${line.slice(0, 80)}\``);
      return;
    }
    const key = match[1] ?? '';
    const value = (match[2] ?? '').trim();
    if (raw !== raw.trimStart()) section.warning(`Line ${number}: leading whitespace before \`${key}\``);
    const quote = value[0];
    if ((quote === '"' || quote === "'") && (value.length < 2 || !value.endsWith(quote))) {
      section.error(`Line ${number}: unterminated quoted value for \`${key}\``);
    }
    const first = keys.get(key);
    if (first !== undefined) {
      section.warning(`Line ${number}: \`${key}\` is already defined on line ${first}, the last value wins`);
    } else {
      keys.set(key, number);
    }
  });
  return new Set(keys.keys());
}
