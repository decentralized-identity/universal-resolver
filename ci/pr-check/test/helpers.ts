import { Section } from '../src/report/report.ts';

export const section = (): Section => new Section('test', true);

/** Messages of a section, prefixed with the level, for compact assertions. */
export const messages = (s: Section): string[] => s.findings.map((f) => `${f.level}: ${f.message}`);
