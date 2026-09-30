import type { Section } from '../report/report.ts';

const DRIVERS_HEADING = /^##\s+Drivers\s*$/;
const SEPARATOR_CELL = /^\s*:?-+:?\s*$/;

/** Cells of a Markdown table row; the leading pipe is required, the trailing one optional (like GitHub). */
export function tableCells(line: string): string[] {
  let row = line.trim();
  if (row.endsWith('|') && !row.endsWith('\\|')) row = row.slice(0, -1);
  return row.split(/(?<!\\)\|/).slice(1);
}

/** Checks that the driver table in the section `## Drivers` is well-formed. */
export function checkReadme(section: Section, text: string): void {
  const lines = text.split(/\r?\n/);
  const start = lines.findIndex((line) => DRIVERS_HEADING.test(line));
  if (start < 0) {
    section.error('Section `## Drivers` not found');
    return;
  }

  const table: { number: number; line: string }[] = [];
  for (let index = start + 1; index < lines.length; index++) {
    const line = lines[index] ?? '';
    if (line.startsWith('#')) break;
    if (line.trimStart().startsWith('|')) {
      table.push({ number: index + 1, line });
    } else if (table.length && line.trim()) {
      section.error(`Line ${index + 1}: driver table interrupted by a non-table line`);
      break;
    } else if (table.length) {
      break;
    }
  }

  const [header, separator, ...rows] = table;
  if (!header || !separator || rows.length === 0) {
    section.error('Driver table in section `## Drivers` not found or empty');
    return;
  }
  const columns = tableCells(header.line).length;
  const separatorCells = tableCells(separator.line);
  if (separatorCells.length !== columns || !separatorCells.every((cell) => SEPARATOR_CELL.test(cell))) {
    section.error(`Line ${separator.number}: invalid table separator row`);
  }
  for (const { number, line } of rows) {
    const count = tableCells(line).length;
    if (count !== columns) section.error(`Line ${number}: table row has ${count} columns, expected ${columns}`);
  }
}
