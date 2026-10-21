import type { ResolveResponse } from './client.ts';

/** Maximum characters of a response body in the log; DID documents are usually far smaller. */
const MAX_BODY_CHARS = 100_000;

/**
 * The response of a test identifier for the workflow log (debugging only, not part of the report), as a
 * collapsible group in GitHub Actions: a summary line and the body, pretty-printed if it is JSON.
 */
export function responseLog(did: string, response: ResolveResponse, durationMs: number): string {
  const { result, status, body } = response;
  const outcome = result.ok ? 'resolved' : `failed: ${result.reason}`;
  const summary = `Response for ${did} (${status === undefined ? 'no response' : `HTTP ${status}`}, ${(durationMs / 1000).toFixed(1)} s, ${outcome})`;
  return `::group::${summary}\n${formatBody(body)}\n::endgroup::`;
}

function formatBody(body: string | undefined): string {
  if (body === undefined) return '(no response body)';
  if (!body.trim()) return '(empty response body)';
  let text = body;
  try {
    text = JSON.stringify(JSON.parse(body), null, 2);
  } catch {
    // not JSON, log as it is
  }
  return text.length > MAX_BODY_CHARS ? `${text.slice(0, MAX_BODY_CHARS)}\n… (truncated, ${text.length} characters)` : text;
}
