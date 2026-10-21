import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { relevantLines } from '../src/logs/relevant-lines.ts';

describe('relevantLines', () => {
  it('reduces a Java exception to the warning and the exception message', () => {
    const log = `[qtp282828951-21] WARN servlet.ResolveServlet (doGet) - Resolve problem for did:btcr2:k1q: Cannot bech32m-decode identifier: k1q
uniresolver.ResolutionException: Cannot bech32m-decode identifier: k1q
    at uniresolver.driver.did.btcr2.syntax.DidBtcr2IdentifierDecoding.didBtcr2IdentifierDecoding(DidBtcr2IdentifierDecoding.java:36)
    at java.base/java.lang.Thread.run(Unknown Source)
Caused by: org.bitcoinj.base.exceptions.AddressFormatException$InvalidChecksum: Checksum does not validate
    at org.bitcoinj.base.Bech32.decode(Bech32.java:322)
    ... 31 more
`;
    assert.deepEqual(relevantLines(log, 20), [
      '[qtp282828951-21] WARN servlet.ResolveServlet (doGet) - Resolve problem for did:btcr2:k1q: Cannot bech32m-decode identifier: k1q',
      'uniresolver.ResolutionException: Cannot bech32m-decode identifier: k1q',
    ]);
  });

  it('strips colors and ignores "error" inside DEBUG lines', () => {
    const log = [
      '\u001b[31m2026-10-01T15:20:10,606\u001b[m \u001b[36mDEBUG \u001b[m u.w.s.ResolveServlet: Incoming request',
      '\u001b[31m2026-10-01T15:20:10,610\u001b[m \u001b[33mWARN  \u001b[m u.w.s.ResolveServlet: Resolve problem for did:x:1: Method not supported',
      '2026-10-01T15:20:10,611 DEBUG u.ResolutionException: Created error resolve result: {"didResolutionMetadata":{"error":{}}}',
    ].join('\n');
    assert.deepEqual(relevantLines(log, 20), ['2026-10-01T15:20:10,610 WARN   u.w.s.ResolveServlet: Resolve problem for did:x:1: Method not supported']);
  });

  it('recognizes structured logs', () => {
    const log = '{"level":"info","msg":"request"}\n{"level":"error","msg":"not found"}\ntime=1 level=warn msg="slow"\n';
    assert.deepEqual(relevantLines(log, 20), ['{"level":"error","msg":"not found"}', 'time=1 level=warn msg="slow"']);
  });

  it('falls back to the last lines and limits the number of lines', () => {
    const log = Array.from({ length: 30 }, (_, i) => `info ${i}`).join('\n');
    const lines = relevantLines(log, 20);
    assert.equal(lines.length, 20);
    assert.equal(lines[0], 'info 10');
  });
});
