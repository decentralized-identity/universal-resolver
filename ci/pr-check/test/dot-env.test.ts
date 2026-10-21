import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { checkDotEnv } from '../src/checks/dot-env.ts';
import { messages, section } from './helpers.ts';

describe('checkDotEnv', () => {
  it('returns the defined keys and ignores comments and blank lines', () => {
    const s = section();
    const keys = checkDotEnv(s, '# comment\n\nA=1\nexport B="two"\nC=\n');
    assert.deepEqual(messages(s), []);
    assert.deepEqual([...keys], ['A', 'B', 'C']);
  });

  it('reports malformed lines and unterminated quotes', () => {
    const s = section();
    checkDotEnv(s, 'this is broken\nFOO="bar\n');
    assert.deepEqual(messages(s), [
      'error: Line 1: expected `KEY=value`, found `this is broken`',
      'error: Line 2: unterminated quoted value for `FOO`',
    ]);
  });

  it('warns about duplicate keys and leading whitespace', () => {
    const s = section();
    checkDotEnv(s, 'A=1\n  A=2\n');
    assert.deepEqual(messages(s), [
      'warning: Line 2: leading whitespace before `A`',
      'warning: Line 2: `A` is already defined on line 1, the last value wins',
    ]);
  });
});
