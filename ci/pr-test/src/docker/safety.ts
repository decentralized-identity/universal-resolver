import { isRecord } from '../lib/yaml.ts';
import type { Services } from '../plan/changes.ts';

const HOST_NAMESPACES = ['network_mode', 'pid', 'ipc', 'uts', 'userns_mode', 'cgroup'] as const;

/**
 * The services come from an untrusted pull request and run on the CI runner. Rejects settings that give a
 * container access to the host: privileged mode, host namespaces, capabilities, devices and bind mounts.
 *
 * services: resolved configuration (`docker compose config`); names: the services that will be started.
 */
export function unsafeSettings(services: Services, names: string[]): string[] {
  const problems: string[] = [];
  for (const name of names) {
    const service = services[name] ?? {};
    const problem = (setting: string) => problems.push(`Service \`${name}\` uses \`${setting}\`, which is not allowed in a test run`);

    if (service.privileged === true) problem('privileged: true');
    for (const key of HOST_NAMESPACES) {
      if (typeof service[key] === 'string' && service[key] === 'host') problem(`${key}: host`);
    }
    for (const key of ['cap_add', 'devices', 'device_cgroup_rules', 'security_opt'] as const) {
      if (Array.isArray(service[key]) && service[key].length > 0) problem(key);
    }
    if (Array.isArray(service.volumes)) {
      for (const volume of service.volumes) {
        if (isRecord(volume) && volume.type === 'bind') problem(`a bind mount of \`${String(volume.source)}\``);
      }
    }
  }
  return problems;
}

/** The services and everything they depend on, as docker compose starts them. */
export function withDependencies(services: Services, names: string[]): string[] {
  const result = new Set<string>();
  const visit = (name: string) => {
    if (result.has(name)) return;
    result.add(name);
    const dependsOn = services[name]?.depends_on;
    const dependencies = Array.isArray(dependsOn) ? dependsOn.map(String) : isRecord(dependsOn) ? Object.keys(dependsOn) : [];
    dependencies.forEach(visit);
  };
  names.forEach(visit);
  return [...result];
}
