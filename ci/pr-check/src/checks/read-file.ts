import { readFile } from 'node:fs/promises';
import { join } from 'node:path';
import type { Section } from '../report/report.ts';

/** Reads a checked file as UTF-8 text, reporting a missing or undecodable file. */
export async function readCheckedFile(section: Section, root: string, name: string): Promise<string | undefined> {
  let content: Buffer;
  try {
    content = await readFile(join(root, name));
  } catch {
    section.error('File not found');
    return undefined;
  }
  try {
    return new TextDecoder('utf-8', { fatal: true }).decode(content);
  } catch {
    section.error('File is not valid UTF-8');
    return undefined;
  }
}
