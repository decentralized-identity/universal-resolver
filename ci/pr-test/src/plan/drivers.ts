import { isRecord, parseYaml } from '../lib/yaml.ts';

export interface Driver {
  pattern: string;
  url: string;
  testIdentifiers: string[];
  /** The complete entry, to detect changes. */
  entry: Record<string, unknown>;
}

/** `${variable}` or `${variable:default}` as used in driver URLs. */
const PLACEHOLDER = /^\$\{([\w.-]+)(?::(.*))?}$/;

/** Driver entries of application.yml. Entries without a pattern or url are skipped (reported by the PR check). */
export function parseDrivers(applicationYml: string): Driver[] {
  const config = parseYaml(applicationYml, 'application.yml');
  const uniresolver = isRecord(config) ? config.uniresolver : undefined;
  const entries = isRecord(uniresolver) && Array.isArray(uniresolver.drivers) ? uniresolver.drivers : [];
  return entries.filter(isRecord).flatMap((entry) => {
    if (typeof entry.pattern !== 'string' || typeof entry.url !== 'string') return [];
    const identifiers = Array.isArray(entry.testIdentifiers) ? entry.testIdentifiers.filter((id) => typeof id === 'string') : [];
    return [{ pattern: entry.pattern, url: entry.url.trim(), testIdentifiers: identifiers, entry }];
  });
}

/**
 * The URL the resolver uses for a driver: a `${variable:default}` placeholder resolves to the variable
 * if it is set in the resolver's environment, otherwise to the default.
 */
export function effectiveUrl(url: string, environment: Record<string, unknown>): string {
  const placeholder = PLACEHOLDER.exec(url);
  if (!placeholder) return url;
  const value = environment[placeholder[1] ?? ''];
  return typeof value === 'string' && value ? value : (placeholder[2] ?? '');
}

/** Host of a URL, e.g. the docker compose service a driver URL points to. */
export const urlHost = (url: string): string | undefined => /^https?:\/\/([^:/]+)/.exec(url)?.[1];
