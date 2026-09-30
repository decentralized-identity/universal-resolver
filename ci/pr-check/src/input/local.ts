import { readFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import type { CheckInput } from './input.ts';

/** Files in a local directory, e.g. the checkout in the GitHub Action. */
export async function loadLocalInput(options: {
  path: string;
  changedFiles?: string;
  baseCompose?: string;
}): Promise<CheckInput> {
  const changedFiles = options.changedFiles
    ? (await readFile(options.changedFiles, 'utf8'))
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean)
    : undefined;
  const baseCompose = options.baseCompose ? await readFile(options.baseCompose, 'utf8') : undefined;

  return {
    root: resolve(options.path),
    changedFiles,
    baseCompose,
    dispose: async () => {},
  };
}
