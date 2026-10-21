import { HTTP_TIMEOUT_MS, USER_AGENT } from '../config.ts';

export interface ImageReference {
  /** Registry host, Docker Hub is `registry-1.docker.io`. */
  registry: string;
  repository: string;
  /** Tag or digest. */
  reference: string;
}

export type Pullability =
  | { pullable: true }
  | { pullable: false; level: 'error' | 'warning'; reason: string };

const MANIFEST_ACCEPT = [
  'application/vnd.oci.image.index.v1+json',
  'application/vnd.oci.image.manifest.v1+json',
  'application/vnd.docker.distribution.manifest.list.v2+json',
  'application/vnd.docker.distribution.manifest.v2+json',
].join(', ');

/** Splits an image reference like `ghcr.io/org/image:tag` into registry, repository and tag or digest. */
export function parseImageReference(image: string): ImageReference {
  let [name, digest] = image.split('@', 2) as [string, string | undefined];
  let tag: string | undefined;
  const lastSlash = name.lastIndexOf('/');
  const lastColon = name.lastIndexOf(':');
  if (lastColon > lastSlash) {
    tag = name.slice(lastColon + 1);
    name = name.slice(0, lastColon);
  }

  const [first, ...rest] = name.split('/');
  let registry = 'docker.io';
  let repository = name;
  if (rest.length && first && (first.includes('.') || first.includes(':') || first === 'localhost')) {
    registry = first;
    repository = rest.join('/');
  }
  if (registry === 'docker.io' || registry === 'index.docker.io') {
    registry = 'registry-1.docker.io';
    if (!repository.includes('/')) repository = `library/${repository}`;
  }
  return { registry, repository, reference: digest ?? tag ?? 'latest' };
}

/** Parses a `WWW-Authenticate` header, e.g. `Bearer realm="...",service="..."`. */
export function parseAuthChallenge(header: string | null): { scheme: string; params: Record<string, string> } {
  const [scheme = '', ...rest] = (header ?? '').split(' ');
  const params: Record<string, string> = {};
  for (const [, key, value] of rest.join(' ').matchAll(/(\w+)="([^"]*)"/g)) {
    if (key && value !== undefined) params[key] = value;
  }
  return { scheme: scheme.toLowerCase(), params };
}

async function request(url: string, options: { method?: string; token?: string; accept?: string } = {}): Promise<Response> {
  const headers: Record<string, string> = { 'User-Agent': USER_AGENT, Accept: options.accept ?? MANIFEST_ACCEPT };
  if (options.token) headers.Authorization = `Bearer ${options.token}`;
  return fetch(url, { method: options.method ?? 'HEAD', headers, signal: AbortSignal.timeout(HTTP_TIMEOUT_MS) });
}

/** Anonymous pull token as offered by the registry's auth challenge, undefined if denied. */
async function anonymousToken(challenge: Record<string, string>, repository: string): Promise<string | undefined> {
  const url = new URL(challenge.realm ?? '');
  url.searchParams.set('scope', `repository:${repository}:pull`);
  if (challenge.service) url.searchParams.set('service', challenge.service);
  const response = await request(url.href, { method: 'GET', accept: 'application/json' });
  if (!response.ok) return undefined;
  const body = (await response.json()) as { token?: string; access_token?: string };
  return body.token ?? body.access_token;
}

/** Resolves the image manifest without credentials, like an anonymous `docker pull` would. */
export async function checkPullable(image: string): Promise<Pullability> {
  const { registry, repository, reference } = parseImageReference(image);
  const manifestUrl = `https://${registry}/v2/${repository}/manifests/${reference}`;
  try {
    let response = await request(manifestUrl);
    if (response.status === 405) response = await request(manifestUrl, { method: 'GET' }); // no HEAD support

    if (response.status === 401) {
      const { scheme, params } = parseAuthChallenge(response.headers.get('WWW-Authenticate'));
      if (scheme !== 'bearer' || !params.realm) return denied(`requires a login at \`${registry}\``);
      const token = await anonymousToken(params, repository);
      if (!token) return denied(`does not exist or requires a login at \`${registry}\``);
      response = await request(manifestUrl, { token });
    }

    if (response.ok) return { pullable: true };
    if (response.status === 401 || response.status === 403) return denied('does not exist or requires a login');
    if (response.status === 404) return denied(`not found (tag or digest \`${reference}\` does not exist)`);
    if (response.status === 429 || response.status >= 500) {
      return unverified(`could not be verified, \`${registry}\` returned HTTP ${response.status}`);
    }
    return denied(`could not be resolved, \`${registry}\` returned HTTP ${response.status}`);
  } catch (e) {
    const cause = (e as { cause?: { code?: string } }).cause;
    if (cause?.code === 'ENOTFOUND') return denied(`registry host \`${registry}\` not found`);
    // Timeouts and connection problems may be temporary
    return unverified(`could not be verified (${cause?.code ?? (e as Error).message})`);
  }
}

const denied = (reason: string): Pullability => ({ pullable: false, level: 'error', reason });
const unverified = (reason: string): Pullability => ({ pullable: false, level: 'warning', reason });
