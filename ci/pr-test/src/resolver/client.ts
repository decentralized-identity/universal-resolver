import { isRecord } from '../lib/yaml.ts';

export type ResolveResult = { ok: true } | { ok: false; reason: string };

/** A resolve request: the evaluated result and, if the resolver answered, the raw response for debugging. */
export interface ResolveResponse {
  result: ResolveResult;
  status?: number;
  body?: string;
}

/**
 * Evaluates a resolve response. The DID document isn't validated against the spec: a response with a DID
 * document that has an `id` is a success.
 */
export function evaluateResponse(status: number, body: string): ResolveResult {
  let json: unknown;
  try {
    json = JSON.parse(body);
  } catch {
    return { ok: false, reason: `HTTP ${status}, the response is not JSON` };
  }
  const document = isRecord(json) ? json.didDocument : undefined;
  if (isRecord(document) && typeof document.id === 'string' && document.id) return { ok: true };

  const metadata = isRecord(json) ? json.didResolutionMetadata : undefined;
  const error = isRecord(metadata) ? metadata.error : undefined;
  if (isRecord(error)) {
    const parts = [error.type, error.title, error.detail].filter((p) => typeof p === 'string' && p);
    return { ok: false, reason: `HTTP ${status}, ${parts.join(': ')}` };
  }
  if (typeof error === 'string') return { ok: false, reason: `HTTP ${status}, ${error}` };
  return { ok: false, reason: `HTTP ${status}, the response contains no DID document with an \`id\`` };
}

export class ResolverClient {
  readonly baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl.replace(/\/$/, '');
  }

  async resolve(did: string, timeoutMs: number): Promise<ResolveResponse> {
    try {
      const response = await fetch(`${this.baseUrl}/1.0/identifiers/${did}`, {
        headers: { Accept: 'application/did-resolution' },
        signal: AbortSignal.timeout(timeoutMs),
      });
      const body = await response.text();
      return { result: evaluateResponse(response.status, body), status: response.status, body };
    } catch (e) {
      if ((e as Error).name === 'TimeoutError') {
        return { result: { ok: false, reason: `no response within ${timeoutMs / 1000} s` } };
      }
      const cause = (e as { cause?: { code?: string } }).cause;
      return { result: { ok: false, reason: `request failed (${cause?.code ?? (e as Error).message})` } };
    }
  }

  /** Waits until the resolver answers requests; returns whether it became ready in time. */
  async waitUntilReady(timeoutMs: number): Promise<boolean> {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      try {
        const response = await fetch(`${this.baseUrl}/1.0/methods`, { signal: AbortSignal.timeout(5_000) });
        if (response.ok) return true;
      } catch {
        // not ready yet
      }
      await new Promise((resolve) => setTimeout(resolve, 2_000));
    }
    return false;
  }
}
