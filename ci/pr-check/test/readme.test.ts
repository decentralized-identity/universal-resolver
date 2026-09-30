import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { checkReadme, tableCells } from '../src/checks/readme.ts';
import { messages, section } from './helpers.ts';

const README = `# Universal Resolver

## Drivers

| Driver | Version |
|--------|---------|
| [did-btcr](https://example.com) | 0.1 |
| [did-web](https://example.com) | 1.0

## More Information
`;

describe('checkReadme', () => {
  it('accepts a well-formed table, also rows without trailing pipe', () => {
    const s = section();
    checkReadme(s, README);
    assert.deepEqual(messages(s), []);
  });

  it('reports rows with a wrong number of columns', () => {
    const s = section();
    checkReadme(s, README.replace('| 1.0', '| 1.0 | extra |'));
    assert.deepEqual(messages(s), ['error: Line 8: table row has 3 columns, expected 2']);
  });

  it('reports a missing section', () => {
    const s = section();
    checkReadme(s, '# Universal Resolver\n');
    assert.deepEqual(messages(s), ['error: Section `## Drivers` not found']);
  });

  it('reports a table interrupted by text', () => {
    const s = section();
    checkReadme(s, README.replace('| [did-web]', 'oops\n| [did-web]'));
    assert.deepEqual(messages(s), ['error: Line 8: driver table interrupted by a non-table line']);
  });
});

describe('tableCells', () => {
  it('keeps escaped pipes inside a cell', () => {
    assert.deepEqual(tableCells('| a \\| b | c |'), [' a \\| b ', ' c ']);
  });
});
