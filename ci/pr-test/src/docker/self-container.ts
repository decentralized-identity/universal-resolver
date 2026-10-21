import { existsSync } from 'node:fs';
import { hostname } from 'node:os';
import { exec, execOrThrow } from '../lib/exec.ts';

/**
 * When this tool runs in a container (e.g. `docker run … pr-test`), published ports aren't reachable via
 * localhost. Instead the container joins the test network and reaches the resolver by its service name.
 */
export class SelfContainer {
  private readonly id: string;

  private constructor(id: string) {
    this.id = id;
  }

  /** The container this process runs in, undefined when running directly on the host. */
  static detect(): SelfContainer | undefined {
    return existsSync('/.dockerenv') ? new SelfContainer(hostname()) : undefined;
  }

  async connect(network: string): Promise<void> {
    await execOrThrow('docker', ['network', 'connect', network, this.id]);
  }

  /** Must happen before `docker compose down`, which can't remove a network with connected containers. */
  async disconnect(network: string): Promise<void> {
    await exec('docker', ['network', 'disconnect', '--force', network, this.id]);
  }
}
