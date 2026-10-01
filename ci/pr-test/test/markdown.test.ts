import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { codeBlock, FOOTER, toMarkdown, truncateLog } from '../src/report/markdown.ts';
import { TestReport } from '../src/report/report.ts';

describe('toMarkdown', () => {
  it('reports when there is nothing to test', () => {
    const markdown = toMarkdown(new TestReport());
    assert.match(markdown, /^### ✅ PR test passed\n\nNothing to test/);
    assert.ok(markdown.endsWith(`---\n\n${FOOTER[0]}\n\n${FOOTER[1]}\n`));
  });

  it('renders startups, results, failure logs and startup logs', () => {
    const report = new TestReport();
    report.subject = 'PR #1';
    report.tested.push({ pattern: '^(did:x:.+)$', reason: 'driver entry changed in `application.yml`' });
    report.startups.push({ service: 'driver-did-x', started: true, status: 'running', log: 'started\n' });
    report.results.push({ did: 'did:x:1', pattern: '^(did:x:.+)$', ok: true, durationMs: 1234, logs: [] });
    report.results.push({
      did: 'did:x:2',
      pattern: '^(did:x:.+)$',
      ok: false,
      reason: 'HTTP 500, internalError',
      durationMs: 50,
      logs: [{ service: 'driver-did-x', lines: ['WARN boom'] }],
    });
    const markdown = toMarkdown(report);

    assert.match(markdown, /^### ❌ PR test failed: 1 of 2 test identifier\(s\) failed\n\nPR #1\n/);
    assert.match(markdown, /\| Start `driver-did-x` \| ✅ running \|/);
    assert.match(markdown, /\| Resolve `did:x:1` \| ✅ resolved \(1\.2 s\) \|/);
    assert.match(markdown, /\| Resolve `did:x:2` \| ❌ HTTP 500, internalError \|/);
    assert.match(markdown, /#### `did:x:2`\n\n❌ HTTP 500, internalError\n\nLog of `driver-did-x`:\n\n```text\nWARN boom\n```/);
    assert.match(markdown, /#### Startup log of `driver-did-x`\n\n<details>\n<summary>1 line\(s\)<\/summary>\n\n```text\nstarted\n```/);
  });

  it('fails for a container that did not start', () => {
    const report = new TestReport();
    report.tested.push({ pattern: 'p', reason: 'r' });
    report.startups.push({ service: 'driver-did-x', started: false, status: 'exited (1)', log: '' });
    assert.match(toMarkdown(report), /^### ❌ PR test failed: 1 container\(s\) not started/);
  });
});

describe('codeBlock', () => {
  it('uses a fence longer than backticks in the content', () => {
    assert.deepEqual(codeBlock('a ```` b'), ['`````text', 'a ```` b', '`````']);
  });
});

describe('truncateLog', () => {
  it('keeps the end of a long log at a line boundary', () => {
    const { text, truncated } = truncateLog('line 1\nline 2\nline 3\n', 10);
    assert.equal(truncated, true);
    assert.equal(text, 'line 3');
  });
});
