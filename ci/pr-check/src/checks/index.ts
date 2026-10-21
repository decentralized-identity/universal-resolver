import { APPLICATION_YML, DOCKER_COMPOSE, DOT_ENV, README } from '../config.ts';
import type { CheckInput } from '../input/input.ts';
import { Report } from '../report/report.ts';
import { checkApplicationYml } from './application-yml.ts';
import { checkChangedFiles } from './changed-files.ts';
import { checkDockerCompose, serviceNames } from './docker-compose.ts';
import { checkDotEnv } from './dot-env.ts';
import { checkDriverServices } from './driver-services.ts';
import { checkImages } from './images.ts';
import { readCheckedFile } from './read-file.ts';
import { checkReadme } from './readme.ts';

export const CHANGED_FILES_SECTION = 'Changed files';

/** Runs all checks; every checked file is always checked. */
export async function runChecks(input: CheckInput): Promise<Report> {
  const report = new Report();
  report.subject = input.subject;

  if (input.changedFiles) checkChangedFiles(report.section(CHANGED_FILES_SECTION, { code: false }), input.changedFiles);
  const applicationYml = report.section(APPLICATION_YML);
  const dockerCompose = report.section(DOCKER_COMPOSE);
  const dotEnv = report.section(DOT_ENV);
  const readme = report.section(README);

  const [applicationYmlText, dockerComposeText, dotEnvText, readmeText] = await Promise.all([
    readCheckedFile(applicationYml, input.root, APPLICATION_YML),
    readCheckedFile(dockerCompose, input.root, DOCKER_COMPOSE),
    readCheckedFile(dotEnv, input.root, DOT_ENV),
    readCheckedFile(readme, input.root, README),
  ]);

  const drivers = applicationYmlText === undefined ? [] : checkApplicationYml(applicationYml, applicationYmlText);
  const envKeys = dotEnvText === undefined ? undefined : checkDotEnv(dotEnv, dotEnvText);
  if (readmeText !== undefined) checkReadme(readme, readmeText);

  const compose =
    dockerComposeText === undefined ? undefined : await checkDockerCompose(dockerCompose, input.root, dockerComposeText, envKeys);
  if (compose) {
    await checkImages(dockerCompose, compose.images);
    const baseServices = input.baseCompose === undefined ? undefined : serviceNames(input.baseCompose);
    checkDriverServices({ applicationYml, dockerCompose }, drivers, compose.services, baseServices);
  }
  return report;
}
