export const APPLICATION_YML = 'uni-resolver-web/src/main/resources/application.yml';
export const DOCKER_COMPOSE = 'docker-compose.yml';
export const DOT_ENV = '.env';
export const README = 'README.md';

/** The only files a driver pull request may change, in report order. */
export const CHECKED_FILES = [APPLICATION_YML, DOCKER_COMPOSE, DOT_ENV, README] as const;
export type CheckedFile = (typeof CHECKED_FILES)[number];

export const DEFAULT_REPOSITORY = 'decentralized-identity/universal-resolver';

/** Services in docker-compose.yml that are not drivers. */
export const NON_DRIVER_SERVICES: ReadonlySet<string> = new Set(['uni-resolver-web']);

export const USER_AGENT = 'universal-resolver-pr-check';
export const HTTP_TIMEOUT_MS = 20_000;
