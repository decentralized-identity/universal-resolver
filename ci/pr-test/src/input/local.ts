import { resolve } from 'node:path';
import type { TestInput } from './input.ts';

/** Files in local directories, e.g. the checkouts in the GitHub Action. */
export function loadLocalInput(options: { path: string; basePath: string }): TestInput {
  return { head: resolve(options.path), base: resolve(options.basePath), dispose: async () => {} };
}
