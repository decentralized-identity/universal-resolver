import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { responseLog } from '../src/resolver/response-log.ts';

describe('responseLog', () => {
  it('logs a resolved response as a group with the pretty-printed body', () => {
    const log = responseLog('did:x:1', { result: { ok: true }, status: 200, body: '{"didDocument":{"id":"did:x:1"}}' }, 1234);
    assert.equal(
      log,
      '::group::Response for did:x:1 (HTTP 200, 1.2 s, resolved)\n{\n  "didDocument": {\n    "id": "did:x:1"\n  }\n}\n::endgroup::',
    );
  });

  it('logs failures, non-JSON bodies and missing responses', () => {
    const failed = responseLog('did:x:2', { result: { ok: false, reason: 'HTTP 502, the response is not JSON' }, status: 502, body: '<html>Bad Gateway</html>' }, 50);
    assert.equal(failed, '::group::Response for did:x:2 (HTTP 502, 0.1 s, failed: HTTP 502, the response is not JSON)\n<html>Bad Gateway</html>\n::endgroup::');

    const timeout = responseLog('did:x:3', { result: { ok: false, reason: 'no response within 120 s' } }, 120_000);
    assert.equal(timeout, '::group::Response for did:x:3 (no response, 120.0 s, failed: no response within 120 s)\n(no response body)\n::endgroup::');
  });
});
