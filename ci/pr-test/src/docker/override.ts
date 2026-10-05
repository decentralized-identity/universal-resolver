import { RESOLVER_PORT, RESOLVER_SERVICE } from '../config.ts';

const CONFIG_NAME = 'pr-test-application-yml';
const CONFIG_TARGET = '/pr-test/application.yml';

/** Escapes `$` so docker compose doesn't interpolate it in inline content. */
const escapeInterpolation = (text: string): string => text.replaceAll('$', '$$$$');

/**
 * docker compose override for a test run:
 * - the resolver uses the pull request's application.yml, injected as inline config (no bind mount, so it also
 *   works when this tool runs in a container), replacing the one built into the image
 * - an own network, and no fixed host ports, so a run doesn't conflict with a local deployment
 * - the resolver's port is published on a random host port
 * - images not available for the host's platform run with the fallback platform (see platform.ts)
 *
 * services: the driver services; platforms: service → platform to request.
 */
export function composeOverride(options: {
  applicationYml: string;
  network: string;
  services: string[];
  platforms?: Map<string, string>;
}): string {
  const platform = (service: string): string[] => {
    const value = options.platforms?.get(service);
    return value ? [`    platform: ${JSON.stringify(value)}`] : [];
  };
  // JSON strings are valid YAML; the tags !override/!reset replace the ports of docker-compose.yml
  const lines = [
    'networks:',
    '  default:',
    `    name: ${JSON.stringify(options.network)}`,
    'configs:',
    `  ${CONFIG_NAME}:`,
    `    content: ${JSON.stringify(escapeInterpolation(options.applicationYml))}`,
    'services:',
    `  ${JSON.stringify(RESOLVER_SERVICE)}:`,
    '    configs:',
    `      - source: ${CONFIG_NAME}`,
    `        target: ${CONFIG_TARGET}`,
    '    environment:',
    `      SPRING_CONFIG_LOCATION: ${JSON.stringify(`file:${CONFIG_TARGET}`)}`,
    `    ports: !override [${JSON.stringify(String(RESOLVER_PORT))}]`,
    ...platform(RESOLVER_SERVICE),
  ];
  for (const service of options.services) {
    lines.push(`  ${JSON.stringify(service)}:`, '    ports: !reset []', ...platform(service));
  }
  return `${lines.join('\n')}\n`;
}
