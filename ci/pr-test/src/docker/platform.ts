import { exec } from '../lib/exec.ts';
import { isRecord } from '../lib/yaml.ts';

/**
 * Drivers only need to provide Linux amd64 images (the platform of the GitHub runners). On other machines,
 * e.g. a Mac with Apple silicon, such images run through emulation if the platform is requested explicitly.
 */
export const FALLBACK_PLATFORM = 'linux/amd64';

/** Platform of the Docker daemon, e.g. `linux/arm64`. */
export async function hostPlatform(): Promise<string | undefined> {
  const result = await exec('docker', ['version', '--format', '{{.Server.Os}}/{{.Server.Arch}}']);
  return result.code === 0 ? result.stdout.trim() : undefined;
}

/**
 * Platforms an image is available for, e.g. `linux/amd64`, from `docker manifest inspect --verbose`
 * (a list for multi-platform images, a single entry otherwise). Undefined if the registry can't tell.
 */
export async function imagePlatforms(image: string): Promise<string[] | undefined> {
  const result = await exec('docker', ['manifest', 'inspect', '--verbose', image], { timeoutMs: 60_000 });
  if (result.code !== 0) return undefined;
  try {
    return parsePlatforms(JSON.parse(result.stdout));
  } catch {
    return undefined;
  }
}

export function parsePlatforms(manifest: unknown): string[] {
  const entries = Array.isArray(manifest) ? manifest : [manifest];
  return entries.flatMap((entry) => {
    const descriptor = isRecord(entry) ? entry.Descriptor : undefined;
    const platform = isRecord(descriptor) ? descriptor.platform : undefined;
    if (!isRecord(platform) || typeof platform.os !== 'string' || typeof platform.architecture !== 'string') return [];
    // Attestations (build provenance, SBOM) are listed with os/architecture "unknown"
    if (platform.os === 'unknown') return [];
    return [`${platform.os}/${platform.architecture}`];
  });
}

/**
 * The platform to request for an image: undefined if the image runs natively on the host (or its platforms
 * are unknown, so docker decides), the fallback platform if only that is available.
 */
export function choosePlatform(available: string[] | undefined, host: string | undefined): string | undefined {
  if (!available?.length || !host || available.includes(host)) return undefined;
  return available.includes(FALLBACK_PLATFORM) ? FALLBACK_PLATFORM : undefined;
}

/** Services whose image needs the fallback platform on this host, with that platform. */
export async function platformOverrides(services: Record<string, Record<string, unknown>>, names: string[]): Promise<Map<string, string>> {
  const host = await hostPlatform();
  const overrides = new Map<string, string>();
  await Promise.all(
    names.map(async (name) => {
      const service = services[name] ?? {};
      // A platform set in docker-compose.yml is respected
      if (typeof service.image !== 'string' || service.platform !== undefined) return;
      const platform = choosePlatform(await imagePlatforms(service.image), host);
      if (platform) overrides.set(name, platform);
    }),
  );
  return overrides;
}
