import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { parseOptions, UsageError } from '../src/cli.ts';

describe('parseOptions', () => {
  it('defaults to the current directory', () => {
    assert.deepEqual(parseOptions([]), {
      source: 'local',
      path: '.',
      changedFiles: undefined,
      baseCompose: undefined,
      report: undefined,
    });
  });

  it('treats empty values as not given, as passed by the GitHub Action', () => {
    assert.deepEqual(parseOptions(['--path', '.', '--changed-files', '', '--base-compose', '']), {
      source: 'local',
      path: '.',
      changedFiles: undefined,
      baseCompose: undefined,
      report: undefined,
    });
  });

  it('parses a pull request', () => {
    assert.deepEqual(parseOptions(['--pr', '578', '--report', 'out.md']), {
      source: 'github',
      pr: 578,
      repository: 'decentralized-identity/universal-resolver',
      report: 'out.md',
    });
  });

  it('rejects invalid combinations and values', () => {
    assert.throws(() => parseOptions(['--pr', '578', '--path', '.']), UsageError);
    assert.throws(() => parseOptions(['--pr', 'abc']), UsageError);
    assert.throws(() => parseOptions(['--repository', 'a/b']), UsageError);
    assert.throws(() => parseOptions(['--unknown']), UsageError);
  });
});
