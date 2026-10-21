export const APPLICATION_YML = 'uni-resolver-web/src/main/resources/application.yml';
export const DOCKER_COMPOSE = 'docker-compose.yml';
export const DOT_ENV = '.env';

/** Files needed to start and test the drivers, from the pull request and its base branch. */
export const INPUT_FILES = [APPLICATION_YML, DOCKER_COMPOSE, DOT_ENV] as const;

export const DEFAULT_REPOSITORY = 'decentralized-identity/universal-resolver';

/** The resolver service in docker-compose.yml; it always runs, with the pull request's application.yml. */
export const RESOLVER_SERVICE = 'uni-resolver-web';
export const RESOLVER_PORT = 8080;

/** Time the containers get to start before they are checked and tested. */
export const DEFAULT_STARTUP_WAIT_S = 10;
/** Timeout per test identifier; resolving often depends on external networks. */
export const DEFAULT_RESOLVE_TIMEOUT_S = 120;
/** Maximum number of log lines reported for a failed test identifier. */
export const RELEVANT_LOG_LINES = 20;
/** Maximum characters of a startup log in the report (GitHub comments are limited to 65536 characters). */
export const MAX_STARTUP_LOG_CHARS = 12_000;

export const USER_AGENT = 'universal-resolver-pr-test';
export const HTTP_TIMEOUT_MS = 20_000;
