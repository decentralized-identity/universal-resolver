import { DOT_ENV } from '../config.ts';
import { composeConfig } from '../lib/compose.ts';
import { isRecord, parseYaml } from '../lib/yaml.ts';
import type { Section } from '../report/report.ts';

export type Services = Record<string, Record<string, unknown>>;

export interface Compose {
  services: Services;
  /** Image reference → names of the services using it (variables interpolated where possible). */
  images: Map<string, string[]>;
}

/** docker compose warnings that are not reported. */
const IGNORED_WARNINGS = [
  /the attribute `version` is obsolete/,
  // Reported by the own check "Variable ... is not defined in .env"
  /variable is not set\. Defaulting to a blank string/,
];

/** `${VAR}`, `${VAR:-default}`, ... (`$$` escapes). */
const VARIABLE = /(?<!\$)\$\{([A-Za-z_]\w*)/g;

/** Services of a docker-compose.yml, undefined if it can't be parsed. */
export function serviceNames(text: string): Set<string> | undefined {
  const yaml = parseYaml(text);
  return yaml.ok && isRecord(yaml.value) && isRecord(yaml.value.services) ? new Set(Object.keys(yaml.value.services)) : undefined;
}

/**
 * Checks docker-compose.yml, also with `docker compose config`.
 * envKeys: variables defined in .env, undefined if .env is missing or invalid.
 */
export async function checkDockerCompose(
  section: Section,
  root: string,
  text: string,
  envKeys: Set<string> | undefined,
): Promise<Compose | undefined> {
  const yaml = parseYaml(text);
  if (!yaml.ok) {
    section.error(yaml.error);
    return undefined;
  }
  const services = isRecord(yaml.value) ? yaml.value.services : undefined;
  if (!isRecord(services) || Object.keys(services).length === 0) {
    section.error('`services` must be a non-empty mapping');
    return undefined;
  }

  for (const [name, service] of Object.entries(services)) {
    if (!isRecord(service)) section.error(`Service \`${name}\` must be a mapping`);
    else if (!service.image && !service.build) section.error(`Service \`${name}\` has neither \`image\` nor \`build\``);
  }

  const resolved = await validateWithCompose(section, root);

  if (envKeys) {
    const variables = new Set([...text.matchAll(VARIABLE)].map((m) => m[1] ?? ''));
    for (const variable of [...variables].filter((v) => !envKeys.has(v)).sort()) {
      section.warning(`Variable \`\${${variable}}\` is not defined in \`${DOT_ENV}\``);
    }
  }

  const validServices = Object.fromEntries(Object.entries(services).filter(([, s]) => isRecord(s))) as Services;
  return { services: validServices, images: imagesOf(resolved ?? validServices) };
}

/** Runs `docker compose config`, reports its messages and returns the resolved services. */
async function validateWithCompose(section: Section, root: string): Promise<Services | undefined> {
  const result = await composeConfig(root);
  if (!result.available) {
    section.warning('`docker-compose` binary not found, skipped compose validation');
    return undefined;
  }
  for (const { level, message } of result.messages) {
    if (level === 'error') section.error(`docker compose: ${message}`);
    else if (!IGNORED_WARNINGS.some((ignored) => ignored.test(message))) section.warning(`docker compose: ${message}`);
  }
  const services = result.config?.services;
  return isRecord(services) ? (services as Services) : undefined;
}

function imagesOf(services: Services): Map<string, string[]> {
  const images = new Map<string, string[]>();
  for (const [name, service] of Object.entries(services)) {
    const image = typeof service.image === 'string' ? service.image.trim() : '';
    if (!image || image.includes('$')) continue; // not interpolated: can't be resolved
    images.set(image, [...(images.get(image) ?? []), name]);
  }
  return images;
}
