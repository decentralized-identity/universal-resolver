import type { Section } from '../report/report.ts';

const DRIVERS_HEADING = /^##\s+Drivers\s*$/;
const SEPARATOR_CELL = /^\s*:?-+:?\s*$/;
/** A Markdown link to a web page, e.g. `[did-example](https://github.com/org/driver)`. */
const WEB_LINK = /\[[^\]]+\]\(\s*https?:\/\/[^)\s]+/;

interface Line {
  number: number;
  text: string;
}

/** Cells of a Markdown table row; the leading pipe is required, the trailing one optional (like GitHub). */
export function tableCells(line: string): string[] {
  let row = line.trim();
  if (row.endsWith('|') && !row.endsWith('\\|')) row = row.slice(0, -1);
  return row.split(/(?<!\\)\|/).slice(1);
}

const isSeparatorRow = (line: string): boolean => {
  const cells = tableCells(line);
  return cells.length > 0 && cells.every((cell) => SEPARATOR_CELL.test(cell));
};

/** The lines of the section `## Drivers`, up to the next heading; undefined if there is no such section. */
function driversSection(text: string): Line[] | undefined {
  const lines = text.split(/\r?\n/);
  const start = lines.findIndex((line) => DRIVERS_HEADING.test(line));
  if (start < 0) return undefined;
  const section: Line[] = [];
  for (let index = start + 1; index < lines.length && !lines[index]?.startsWith('#'); index++) {
    section.push({ number: index + 1, text: lines[index] ?? '' });
  }
  return section;
}

/** Groups of consecutive table lines; any other line (also a blank one) ends a table. */
function tables(lines: Line[]): Line[][] {
  const result: Line[][] = [];
  let current: Line[] = [];
  for (const line of lines) {
    if (line.text.trimStart().startsWith('|')) {
      current.push(line);
    } else if (current.length) {
      result.push(current);
      current = [];
    }
  }
  if (current.length) result.push(current);
  return result;
}

/**
 * Checks the driver table in the section `## Drivers`: there is exactly one table, it is well-formed, and every
 * driver links to where issues can be reported in the first column.
 */
export function checkReadme(section: Section, text: string): void {
  const lines = driversSection(text);
  if (!lines) {
    section.error('Section `## Drivers` not found');
    return;
  }

  const [driverTable, ...otherTables] = tables(lines);
  const [header, separator, ...rows] = driverTable ?? [];
  if (!header || !separator || rows.length === 0) {
    section.error('Driver table in section `## Drivers` not found or empty');
    return;
  }

  const columns = tableCells(header.text).length;
  if (tableCells(separator.text).length !== columns || !isSeparatorRow(separator.text)) {
    section.error(`Line ${separator.number}: invalid table separator row`);
  }
  for (const row of rows) {
    const count = tableCells(row.text).length;
    if (count !== columns) section.error(`Line ${row.number}: table row has ${count} columns, expected ${columns}`);
    checkIssueTrackerLink(section, row);
  }

  // A new driver must be a row of the existing table, not a table of its own (e.g. after a blank line)
  const lastRow = rows[rows.length - 1] ?? header;
  for (const table of otherTables) {
    const first = table[0] as Line;
    section.error(
      `Line ${first.number}: a separate table in section \`## Drivers\`. Add the driver as a new row of the ` +
        `existing driver table (lines ${header.number}–${lastRow.number}), without blank lines or a new header`,
    );
    // Rows of a separate table must follow the same rules; a header and separator row are skipped
    const separatorIndex = table.findIndex((line) => isSeparatorRow(line.text));
    for (const row of separatorIndex >= 0 ? table.slice(separatorIndex + 1) : table) checkIssueTrackerLink(section, row);
  }
}

/** The first column must link to the driver's repository or issue tracker, so issues can be reported. */
function checkIssueTrackerLink(section: Section, row: Line): void {
  const firstCell = (tableCells(row.text)[0] ?? '').trim();
  if (!WEB_LINK.test(firstCell)) {
    section.error(
      `Line ${row.number}: the first column must link to the driver's repository or issue tracker, ` +
        `e.g. \`[did-example](https://github.com/org/driver)\`, found \`${firstCell.replaceAll('`', '') || '(empty)'}\``,
    );
  }
}
