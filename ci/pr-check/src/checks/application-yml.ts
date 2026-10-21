import type { Section } from '../report/report.ts';
import { isRecord, parseYaml } from '../lib/yaml.ts';

export interface Driver {
  pattern: string;
  url: string;
}

/**
 * Keys bound by uniresolver.web.config.DriverConfigs.DriverConfig.
 * Spring ignores unknown keys silently, so a typo simply disables the setting.
 */
const DRIVER_KEYS = new Set([
  'pattern',
  'url',
  'propertiesEndpoint',
  'supportsOptions',
  'supportsDereference',
  'acceptHeaderValue',
  'acceptHeaderValueDereference',
  'testIdentifiers',
  'traits',
]);
const SCALAR_KEYS = [...DRIVER_KEYS].filter((key) => key !== 'testIdentifiers' && key !== 'traits');

/** `${variable}` or `${variable:default}` as used in driver URLs. */
const PLACEHOLDER = /^\$\{([\w.-]+)(?::(.*))?}$/;

/** Host a driver URL points to, using the default of a `${variable:default}` placeholder. */
export function driverUrlHost(url: string): string | undefined {
  const placeholder = PLACEHOLDER.exec(url.trim());
  const effective = placeholder ? (placeholder[2] ?? '') : url.trim();
  return /^https?:\/\/([^:/]+)/.exec(effective)?.[1];
}

/** Whether the driver URL can be configured with a variable (and defaults to a docker compose service). */
export const isConfigurableUrl = (url: string): boolean => PLACEHOLDER.test(url.trim());

/** Checks application.yml and returns the valid driver entries. */
export function checkApplicationYml(section: Section, text: string): Driver[] {
  const yaml = parseYaml(text);
  if (!yaml.ok) {
    section.error(yaml.error);
    return [];
  }
  const uniresolver = isRecord(yaml.value) ? yaml.value.uniresolver : undefined;
  const entries = isRecord(uniresolver) ? uniresolver.drivers : undefined;
  if (!Array.isArray(entries) || entries.length === 0) {
    section.error('`uniresolver.drivers` must be a non-empty list');
    return [];
  }

  const drivers: Driver[] = [];
  const patterns = new Map<string, number>();
  entries.forEach((entry: unknown, index) => {
    const number = index + 1;
    if (!isRecord(entry)) {
      section.error(`Driver #${number} must be a mapping`);
      return;
    }
    const label = typeof entry.pattern === 'string' ? `Driver #${number} (\`${entry.pattern}\`)` : `Driver #${number}`;
    const valid = checkDriver(section, label, entry);

    if (typeof entry.pattern === 'string') {
      const first = patterns.get(entry.pattern);
      if (first !== undefined) section.error(`${label}: duplicate pattern, already used by driver #${first}`);
      else patterns.set(entry.pattern, number);
    }
    if (valid) drivers.push({ pattern: entry.pattern as string, url: entry.url as string });
  });
  return drivers;
}

/** Checks one driver entry; returns whether it has a usable pattern and url. */
function checkDriver(section: Section, label: string, driver: Record<string, unknown>): boolean {
  let valid = true;
  for (const key of ['pattern', 'url']) {
    const value = driver[key];
    if (typeof value !== 'string' || !value.trim()) {
      section.error(`${label}: required key \`${key}\` is missing or empty`);
      valid = false;
    }
  }
  for (const key of Object.keys(driver).filter((k) => !DRIVER_KEYS.has(k))) {
    section.warning(`${label}: unknown key \`${key}\` is ignored (typo? allowed: ${[...DRIVER_KEYS].sort().join(', ')})`);
  }
  for (const key of SCALAR_KEYS) {
    const value = driver[key];
    if (value !== undefined && value !== null && typeof value === 'object') {
      section.error(`${label}: \`${key}\` must be a scalar value`);
    }
  }
  if (driver.traits !== undefined && driver.traits !== null && !isRecord(driver.traits)) {
    section.error(`${label}: \`traits\` must be a mapping`);
  }
  checkTestIdentifiers(section, label, driver);
  return valid;
}

function checkTestIdentifiers(section: Section, label: string, driver: Record<string, unknown>): void {
  const identifiers = driver.testIdentifiers;
  if (!Array.isArray(identifiers) || identifiers.length === 0) {
    if (identifiers !== undefined && identifiers !== null && !Array.isArray(identifiers)) {
      section.error(`${label}: \`testIdentifiers\` must be a list of strings`);
    } else {
      section.error(`${label}: at least one entry in \`testIdentifiers\` is required`);
    }
    return;
  }
  if (!identifiers.every((id) => typeof id === 'string')) {
    section.error(`${label}: \`testIdentifiers\` must be a list of strings`);
    return;
  }
  if (typeof driver.pattern !== 'string') return;

  let regex: RegExp;
  try {
    regex = new RegExp(driver.pattern);
  } catch (e) {
    // JavaScript and Java regex dialects differ slightly, so don't fail hard
    section.warning(`${label}: pattern could not be compiled (${(e as Error).message}); please verify it is a valid Java regex`);
    return;
  }
  for (const id of identifiers as string[]) {
    if (!regex.test(id)) section.warning(`${label}: test identifier \`${id}\` does not match the pattern`);
  }
}
