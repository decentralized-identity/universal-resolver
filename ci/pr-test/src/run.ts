import { randomBytes } from 'node:crypto';
import { existsSync } from 'node:fs';
import { readFile } from 'node:fs/promises';
import { join } from 'node:path';
import { setTimeout as sleep } from 'node:timers/promises';
import { APPLICATION_YML, RELEVANT_LOG_LINES, RESOLVER_PORT, RESOLVER_SERVICE } from './config.ts';
import { ComposeProject, resolvedServices } from './docker/compose.ts';
import { composeOverride } from './docker/override.ts';
import { platformOverrides } from './docker/platform.ts';
import { unsafeSettings, withDependencies } from './docker/safety.ts';
import { SelfContainer } from './docker/self-container.ts';
import type { TestInput } from './input/input.ts';
import { relevantLines } from './logs/relevant-lines.ts';
import { parseDrivers } from './plan/drivers.ts';
import { type PlannedDriver, planTests } from './plan/plan.ts';
import type { LogExcerpt } from './report/report.ts';
import { TestReport } from './report/report.ts';
import { ResolverClient } from './resolver/client.ts';

export interface RunOptions {
  startupWaitS: number;
  resolveTimeoutS: number;
  /** Progress output, e.g. to the workflow log. */
  log: (message: string) => void;
}

const RESOLVER_READY_TIMEOUT_MS = 60_000;

const readOptional = async (path: string): Promise<string> => (existsSync(path) ? readFile(path, 'utf8') : '');

/** Selects the drivers changed by the pull request, starts them with the resolver and resolves their test identifiers. */
export async function runTest(input: TestInput, options: RunOptions): Promise<TestReport> {
  const report = new TestReport();
  report.subject = input.subject;

  const applicationYml = await readFile(join(input.head, APPLICATION_YML), 'utf8');
  const [baseApplicationYml, headServices, baseServices] = await Promise.all([
    readOptional(join(input.base, APPLICATION_YML)),
    resolvedServices(input.head),
    resolvedServices(input.base),
  ]);
  const plan = planTests({
    headDrivers: parseDrivers(applicationYml),
    baseDrivers: baseApplicationYml ? parseDrivers(baseApplicationYml) : [],
    headServices,
    baseServices,
  });
  report.tested.push(...plan.drivers.map((p) => ({ pattern: p.driver.pattern, reason: p.reason })));
  report.problems.push(...plan.problems.map((message) => ({ message })));
  if (plan.drivers.length === 0) return report;

  const services = withDependencies(headServices, [RESOLVER_SERVICE, ...plan.services]);
  const unsafe = unsafeSettings(headServices, services);
  if (unsafe.length) {
    report.problems.push(...unsafe.map((message) => ({ message })));
    return report;
  }

  const platforms = await platformOverrides(headServices, services);
  for (const [service, platform] of platforms) {
    options.log(`The image of ${service} is not available for this machine's platform, using ${platform} (emulated)`);
  }

  const name = `pr-test-${randomBytes(4).toString('hex')}`;
  const project = await ComposeProject.create({
    name,
    directory: input.head,
    override: composeOverride({
      applicationYml,
      network: name,
      services: services.filter((s) => s !== RESOLVER_SERVICE),
      platforms,
    }),
  });
  const self = SelfContainer.detect();
  try {
    await runInProject(project, self, plan.drivers, services, report, options);
  } finally {
    options.log('Removing the containers');
    await self?.disconnect(project.name);
    await project.down();
  }
  return report;
}

async function runInProject(
  project: ComposeProject,
  self: SelfContainer | undefined,
  drivers: PlannedDriver[],
  services: string[],
  report: TestReport,
  options: RunOptions,
): Promise<void> {
  options.log(`Pulling and starting ${services.join(', ')}`);
  try {
    await project.pull(services);
    await project.up(services);
  } catch (e) {
    report.problems.push({ message: `Starting the containers failed: ${(e as Error).message}` });
    return;
  }

  options.log(`Waiting ${options.startupWaitS} s for the containers to start`);
  await sleep(options.startupWaitS * 1000);

  // A driver counts as started if its container runs, even if the application inside logs errors
  const driverServices = services.filter((s) => s !== RESOLVER_SERVICE);
  for (const service of driverServices) {
    const [state, log] = await Promise.all([project.state(service), project.logs(service)]);
    report.startups.push({ service, started: state.running, status: state.status, log });
    options.log(`::group::Startup log of ${service} (${state.status})\n${log}\n::endgroup::`);
  }

  const resolver = await connectResolver(project, self);
  if (!(await project.state(RESOLVER_SERVICE)).running || !(await resolver.waitUntilReady(RESOLVER_READY_TIMEOUT_MS))) {
    const lines = relevantLines(await project.logs(RESOLVER_SERVICE), RELEVANT_LOG_LINES);
    report.problems.push({ message: `The resolver \`${RESOLVER_SERVICE}\` did not start`, logs: [{ service: RESOLVER_SERVICE, lines }] });
    return;
  }

  const started = new Set(report.startups.filter((s) => s.started).map((s) => s.service));
  for (const { driver, service } of drivers) {
    if (service && service !== RESOLVER_SERVICE && !started.has(service)) continue; // reported as not started
    for (const did of driver.testIdentifiers) {
      options.log(`Resolving ${did}`);
      const since = new Date(Date.now() - 200);
      const result = await resolver.resolve(did, options.resolveTimeoutS * 1000);
      const durationMs = Date.now() - since.getTime();
      const logs: LogExcerpt[] = [];
      if (!result.ok) {
        await sleep(500); // let the containers flush their logs
        const until = new Date();
        for (const s of [...new Set([service, RESOLVER_SERVICE].filter((x): x is string => !!x))]) {
          logs.push({ service: s, lines: relevantLines(await project.logs(s, { since, until }), RELEVANT_LOG_LINES) });
        }
      }
      report.results.push({ did, pattern: driver.pattern, ok: result.ok, reason: result.ok ? undefined : result.reason, durationMs, logs });
    }
  }
}

/** Client for the resolver: via the published port, or via the test network when running in a container. */
async function connectResolver(project: ComposeProject, self: SelfContainer | undefined): Promise<ResolverClient> {
  if (self) {
    await self.connect(project.name);
    return new ResolverClient(`http://${RESOLVER_SERVICE}:${RESOLVER_PORT}`);
  }
  const address = await project.port(RESOLVER_SERVICE, RESOLVER_PORT);
  const port = address.split(':').pop();
  return new ResolverClient(`http://127.0.0.1:${port}`);
}
