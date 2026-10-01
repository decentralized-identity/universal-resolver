import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { evaluateResponse } from '../src/resolver/client.ts';

describe('evaluateResponse', () => {
  it('accepts a DID document with an id, without validating it', () => {
    assert.deepEqual(evaluateResponse(200, '{"didDocument":{"id":"did:x:1"},"didResolutionMetadata":{}}'), { ok: true });
  });

  it('reports the resolution error', () => {
    const body = '{"didDocument":null,"didResolutionMetadata":{"error":{"type":"METHOD_NOT_SUPPORTED","title":"The DID method is not supported.","detail":"Method not supported: btcr2"}}}';
    assert.deepEqual(evaluateResponse(501, body), {
      ok: false,
      reason: 'HTTP 501, METHOD_NOT_SUPPORTED: The DID method is not supported.: Method not supported: btcr2',
    });
  });

  it('reports a string error, a missing id and non-JSON responses', () => {
    assert.deepEqual(evaluateResponse(404, '{"didResolutionMetadata":{"error":"notFound"}}'), { ok: false, reason: 'HTTP 404, notFound' });
    assert.deepEqual(evaluateResponse(200, '{"didDocument":{}}'), {
      ok: false,
      reason: 'HTTP 200, the response contains no DID document with an `id`',
    });
    assert.deepEqual(evaluateResponse(502, '<html>Bad Gateway</html>'), { ok: false, reason: 'HTTP 502, the response is not JSON' });
  });
});
