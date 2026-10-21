import { isDeepStrictEqual } from 'node:util';
import type { Driver } from './drivers.ts';

export type Services = Record<string, Record<string, unknown>>;

/** Driver entries that are new or changed (e.g. test identifiers or url), matched by pattern. */
export function changedDrivers(base: Driver[], head: Driver[]): Map<string, 'new' | 'changed'> {
  const baseByPattern = new Map(base.map((driver) => [driver.pattern, driver.entry]));
  const changes = new Map<string, 'new' | 'changed'>();
  for (const driver of head) {
    const before = baseByPattern.get(driver.pattern);
    if (before === undefined) changes.set(driver.pattern, 'new');
    else if (!isDeepStrictEqual(before, driver.entry)) changes.set(driver.pattern, 'changed');
  }
  return changes;
}

/**
 * Services that are new or changed (e.g. image tag or variables). Compares the configurations resolved by
 * `docker compose config`, so changed values in .env count as well.
 */
export function changedServices(base: Services, head: Services): Set<string> {
  return new Set(Object.keys(head).filter((name) => !isDeepStrictEqual(base[name], head[name])));
}
