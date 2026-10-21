import { execFile } from 'node:child_process';
import { existsSync } from 'node:fs';
import { join } from 'node:path';
import { promisify } from 'node:util';
import { DOCKER_COMPOSE, DOT_ENV } from '../config.ts';

const run = promisify(execFile);

export interface ComposeMessage {
  level: 'error' | 'warning';
  message: string;
}

export type ComposeConfigResult =
  | { available: false }
  | { available: true; config: Record<string, unknown> | undefined; messages: ComposeMessage[] };

const LOG_LINE = /level=(\w+)\s+msg="(.*)"$/;

/**
 * Validates docker-compose.yml with `docker compose config`, interpolating variables from .env.
 * Returns the resolved configuration (undefined if invalid) and the messages docker compose logged.
 */
export async function composeConfig(root: string): Promise<ComposeConfigResult> {
  const args = ['--project-directory', root, '-f', join(root, DOCKER_COMPOSE)];
  if (existsSync(join(root, DOT_ENV))) args.push('--env-file', join(root, DOT_ENV));
  args.push('config', '--format', 'json');

  let stdout = '';
  let stderr = '';
  let failed = false;
  try {
    // Minimal environment: the checked files are untrusted
    ({ stdout, stderr } = await run('docker-compose', args, {
      env: { PATH: process.env.PATH ?? '', HOME: '/tmp' },
      timeout: 120_000,
      maxBuffer: 64 * 1024 * 1024,
    }));
  } catch (e) {
    const error = e as NodeJS.ErrnoException & { stdout?: string; stderr?: string; killed?: boolean };
    if (error.code === 'ENOENT') return { available: false };
    if (error.killed) return { available: true, config: undefined, messages: [{ level: 'error', message: 'timed out' }] };
    failed = true;
    stderr = error.stderr ?? String(error);
  }

  const messages = parseMessages(stderr, root);
  if (failed && !messages.some((m) => m.level === 'error')) {
    messages.push({ level: 'error', message: '`docker compose config` failed' });
  }
  let config: Record<string, unknown> | undefined;
  if (!failed) {
    try {
      config = JSON.parse(stdout) as Record<string, unknown>;
    } catch {
      config = undefined;
    }
  }
  return { available: true, config, messages };
}

export function parseMessages(stderr: string, root: string): ComposeMessage[] {
  return stderr
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const match = LOG_LINE.exec(line);
      const [level, message] = match ? [match[1] ?? 'error', match[2] ?? line] : ['error', line];
      return {
        level: ['warning', 'warn', 'info', 'debug'].includes(level) ? 'warning' : 'error',
        message: message.replaceAll(`${root}/`, '').replaceAll('\\"', '"'),
      };
    });
}
