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

  it('reports rows after text as a separate table', () => {
    const s = section();
    checkReadme(s, README.replace('| [did-web]', 'oops\n| [did-web]'));
    assert.deepEqual(messages(s), [
      'error: Line 9: a separate table in section `## Drivers`. Add the driver as a new row of the existing driver table (lines 5–7), without blank lines or a new header',
    ]);
  });

  it('reports a new table after a blank line and a first column without link, as in PR 571', () => {
    const s = section();
    const newTable = [
      '',
      '| Method | Driver Version | Spec |',
      '| :--- | :--- | :--- |',
      '| `neuralkey` | `1.0.0` | [Spec Link](https://example.com/spec) |',
    ].join('\n');
    checkReadme(s, README.replace('| 1.0\n', `| 1.0\n${newTable}\n`));
    assert.deepEqual(messages(s), [
      'error: Line 10: a separate table in section `## Drivers`. Add the driver as a new row of the existing driver table (lines 5–8), without blank lines or a new header',
      'error: Line 12: the first column must link to the driver\'s repository or issue tracker, e.g. `[did-example](https://github.com/org/driver)`, found `neuralkey`',
    ]);
  });

  it('requires a web link in the first column of every driver', () => {
    const s = section();
    checkReadme(s, README.replace('[did-btcr](https://example.com)', 'did-btcr').replace('[did-web](https://example.com)', '[did-web](#web)'));
    assert.deepEqual(messages(s), [
      'error: Line 7: the first column must link to the driver\'s repository or issue tracker, e.g. `[did-example](https://github.com/org/driver)`, found `did-btcr`',
      'error: Line 8: the first column must link to the driver\'s repository or issue tracker, e.g. `[did-example](https://github.com/org/driver)`, found `[did-web](#web)`',
    ]);
  });
});

describe('tableCells', () => {
  it('keeps escaped pipes inside a cell', () => {
    assert.deepEqual(tableCells('| a \\| b | c |'), [' a \\| b ', ' c ']);
  });
});
