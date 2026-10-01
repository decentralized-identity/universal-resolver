import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { parseOptions, UsageError } from '../src/cli.ts';

describe('parseOptions', () => {
  it('parses a pull request with defaults', () => {
    assert.deepEqual(parseOptions(['--pr', '580']), {
      source: 'github',
      pr: 580,
      repository: 'decentralized-identity/universal-resolver',
      report: undefined,
      startupWaitS: 60,
      resolveTimeoutS: 120,
    });
  });

  it('parses local directories as passed by the GitHub Action', () => {
    assert.deepEqual(parseOptions(['--path', 'head', '--base-path', 'base', '--report', '', '--startup-wait', '10']), {
      source: 'local',
      path: 'head',
      basePath: 'base',
      report: undefined,
      startupWaitS: 10,
      resolveTimeoutS: 120,
    });
  });

  it('rejects missing and invalid options', () => {
    assert.throws(() => parseOptions([]), UsageError);
    assert.throws(() => parseOptions(['--path', 'head']), UsageError);
    assert.throws(() => parseOptions(['--pr', '1', '--path', 'x']), UsageError);
    assert.throws(() => parseOptions(['--pr', '1', '--timeout', 'soon']), UsageError);
  });
});
