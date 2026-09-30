import { APPLICATION_YML, DOCKER_COMPOSE, NON_DRIVER_SERVICES } from '../config.ts';
import type { Section } from '../report/report.ts';
import { type Driver, driverUrlHost, isConfigurableUrl } from './application-yml.ts';
import type { Services } from './docker-compose.ts';

/**
 * Checks the links between drivers in application.yml and services in docker-compose.yml.
 * The driver's `url` is the only link between a DID identifier prefix and its service.
 *
 * baseServices: services on the base branch, undefined if unknown (then new services aren't checked).
 */
export function checkDriverServices(
  sections: { applicationYml: Section; dockerCompose: Section },
  drivers: Driver[],
  services: Services,
  baseServices: Set<string> | undefined,
): void {
  const referenced = new Set<string>();
  for (const driver of drivers) {
    const host = driverUrlHost(driver.url);
    if (!host) continue;
    referenced.add(host);
    if (isConfigurableUrl(driver.url) && !host.includes('.') && host !== 'localhost' && !(host in services)) {
      sections.dockerCompose.warning(
        `Service \`${host}\` referenced by driver \`${driver.pattern}\` in \`${APPLICATION_YML}\` is not defined`,
      );
    }
  }

  // A service added by the pull request is a new driver and needs a driver entry pointing to it.
  // Existing services aren't checked: maintainers may point a driver to another URL (e.g. a hosted driver).
  if (!baseServices || drivers.length === 0) return;
  const dependencies = dependenciesOf(services);
  for (const name of Object.keys(services).sort()) {
    if (baseServices.has(name) || referenced.has(name) || dependencies.has(name) || NON_DRIVER_SERVICES.has(name)) continue;
    sections.applicationYml.error(
      `No driver entry for the new service \`${name}\` from \`${DOCKER_COMPOSE}\`. Add an entry to ` +
        `\`uniresolver.drivers\` with a \`url\` pointing to \`http://${name}:<port>/\`, ` +
        'otherwise the Universal Resolver never calls the driver',
    );
  }
}

/** Services other services depend on (e.g. a driver's database); they don't need an own driver entry. */
function dependenciesOf(services: Services): Set<string> {
  const names = new Set<string>();
  for (const service of Object.values(services)) {
    const dependsOn = service.depends_on;
    if (Array.isArray(dependsOn)) dependsOn.forEach((name) => names.add(String(name)));
    else if (dependsOn && typeof dependsOn === 'object') Object.keys(dependsOn).forEach((name) => names.add(name));
    if (Array.isArray(service.links)) service.links.forEach((link) => names.add(String(link).split(':')[0] ?? ''));
  }
  return names;
}
