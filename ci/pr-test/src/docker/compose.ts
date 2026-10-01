import { existsSync } from 'node:fs';
import { writeFile } from 'node:fs/promises';
import { join } from 'node:path';
import { DOCKER_COMPOSE, DOT_ENV } from '../config.ts';
import { exec, execOrThrow } from '../lib/exec.ts';
import { isRecord } from '../lib/yaml.ts';
import type { Services } from '../plan/changes.ts';

const files = (directory: string): string[] => {
  const args = ['--project-directory', directory, '-f', join(directory, DOCKER_COMPOSE)];
  if (existsSync(join(directory, DOT_ENV))) args.push('--env-file', join(directory, DOT_ENV));
  return args;
};

/**
 * Services of a docker-compose.yml as resolved by `docker compose config` (variables from .env interpolated).
 * Paths of the directory are replaced by `.`, so configurations from different directories are comparable.
 */
export async function resolvedServices(directory: string): Promise<Services> {
  if (!existsSync(join(directory, DOCKER_COMPOSE))) return {};
  const stdout = await execOrThrow('docker', ['compose', '--project-name', 'pr-test', ...files(directory), 'config', '--format', 'json']);
  const config = JSON.parse(stdout.replaceAll(directory, '.')) as { services?: unknown };
  return isRecord(config.services) ? (config.services as Services) : {};
}

export interface ContainerState {
  running: boolean;
  /** e.g. `running`, `exited (1)`, `restarting`. */
  status: string;
}

/** A docker compose project of a test run: the pull request's docker-compose.yml plus a test override. */
export class ComposeProject {
  readonly name: string;
  private readonly args: string[];

  private constructor(name: string, args: string[]) {
    this.name = name;
    this.args = args;
  }

  static async create(options: { name: string; directory: string; override: string }): Promise<ComposeProject> {
    const overrideFile = join(options.directory, 'docker-compose.pr-test.yml');
    await writeFile(overrideFile, options.override);
    return new ComposeProject(options.name, ['compose', '--project-name', options.name, ...files(options.directory), '-f', overrideFile]);
  }

  async pull(services: string[]): Promise<void> {
    await execOrThrow('docker', [...this.args, 'pull', '--quiet', ...services]);
  }

  /** Starts the services and their dependencies; always recreates the containers so the current config is used. */
  async up(services: string[]): Promise<void> {
    await execOrThrow('docker', [...this.args, 'up', '--detach', '--force-recreate', '--no-color', ...services], { timeoutMs: 600_000 });
  }

  async state(service: string): Promise<ContainerState> {
    const id = await this.containerId(service);
    if (!id) return { running: false, status: 'not created' };
    const output = await execOrThrow('docker', ['inspect', '--format', '{{json .State}}', id]);
    const state = JSON.parse(output) as { Status: string; Running: boolean; Restarting: boolean; ExitCode: number };
    const running = state.Running && !state.Restarting;
    const status = state.Status === 'exited' ? `exited (${state.ExitCode})` : state.Status;
    return { running, status };
  }

  /**
   * Container logs, optionally only the lines written in a time window. docker compose writes the container
   * output (stdout and stderr) to its stdout, and its own warnings to stderr, which are left out.
   */
  async logs(service: string, window: { since?: Date; until?: Date } = {}): Promise<string> {
    const args = [...this.args, 'logs', '--no-color', '--no-log-prefix'];
    if (window.since) args.push('--since', window.since.toISOString());
    if (window.until) args.push('--until', window.until.toISOString());
    return (await exec('docker', [...args, service])).stdout;
  }

  /** Host address a container port is published on, e.g. `0.0.0.0:32768`. */
  async port(service: string, port: number): Promise<string> {
    return (await execOrThrow('docker', [...this.args, 'port', service, String(port)])).trim().split('\n')[0] ?? '';
  }

  async containerId(service: string): Promise<string | undefined> {
    const output = await execOrThrow('docker', [...this.args, 'ps', '--all', '--quiet', service]);
    return output.trim().split('\n')[0] || undefined;
  }

  /** Stops and removes the containers, networks and volumes of the project. */
  async down(): Promise<void> {
    await exec('docker', [...this.args, 'down', '--volumes', '--remove-orphans', '--timeout', '5'], { timeoutMs: 300_000 });
  }
}
