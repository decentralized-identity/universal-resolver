import { RESOLVER_SERVICE } from '../config.ts';
import { changedDrivers, changedServices, type Services } from './changes.ts';
import { type Driver, effectiveUrl, urlHost } from './drivers.ts';

export interface PlannedDriver {
  driver: Driver;
  /** docker compose service the driver URL points to; undefined for an external URL (e.g. a hosted driver). */
  service?: string;
  /** Why the driver is tested. */
  reason: string;
}

export interface TestPlan {
  drivers: PlannedDriver[];
  /** Driver services to start in addition to the resolver. */
  services: string[];
  /** Problems that prevent testing a driver, e.g. its service doesn't exist. */
  problems: string[];
}

/**
 * Selects what to test: drivers whose entry in application.yml changed, and all drivers pointing to a service
 * that changed in docker-compose.yml.
 */
export function planTests(input: {
  baseDrivers: Driver[];
  headDrivers: Driver[];
  baseServices: Services;
  headServices: Services;
}): TestPlan {
  const drivers = changedDrivers(input.baseDrivers, input.headDrivers);
  const services = changedServices(input.baseServices, input.headServices);
  const environment = (input.headServices[RESOLVER_SERVICE]?.environment ?? {}) as Record<string, unknown>;

  const planned: PlannedDriver[] = [];
  const problems: string[] = [];
  for (const driver of input.headDrivers) {
    const url = effectiveUrl(driver.url, environment);
    const host = urlHost(url);
    const service = host && host in input.headServices ? host : undefined;

    let reason: string | undefined;
    const change = drivers.get(driver.pattern);
    if (change) reason = change === 'new' ? 'new driver in `application.yml`' : 'driver entry changed in `application.yml`';
    else if (service && services.has(service)) reason = `service \`${service}\` changed in \`docker-compose.yml\``;
    if (!reason) continue;

    if (!service && host && !host.includes('.') && host !== 'localhost') {
      problems.push(`Driver \`${driver.pattern}\` points to \`${url}\`, but there is no service \`${host}\` in \`docker-compose.yml\``);
      continue;
    }
    if (driver.testIdentifiers.length === 0) {
      problems.push(`Driver \`${driver.pattern}\` has no \`testIdentifiers\``);
      continue;
    }
    planned.push({ driver, service, reason });
  }

  const toStart = [...new Set(planned.flatMap((p) => (p.service && p.service !== RESOLVER_SERVICE ? [p.service] : [])))];
  return { drivers: planned, services: toStart.sort(), problems };
}
