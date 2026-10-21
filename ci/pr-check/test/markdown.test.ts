import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { FOOTER, toMarkdown } from '../src/report/markdown.ts';
import { Report } from '../src/report/report.ts';

const footer = ['', '---', '', FOOTER[0], '', FOOTER[1], ''];

describe('toMarkdown', () => {
  it('renders a passed report with the footer', () => {
    const report = new Report();
    report.section('.env');
    assert.equal(
      toMarkdown(report),
      ['### ✅ PR check passed', '', '| File | Status |', '|------|--------|', '| `.env` | ✅ OK |', ...footer].join('\n'),
    );
  });

  it('renders errors before warnings and notes per section', () => {
    const report = new Report();
    report.subject = 'PR #1';
    report.section('Changed files', { code: false }).error('`x` must not be changed');
    const env = report.section('.env');
    env.warning('duplicate');
    env.error('broken');
    env.info('a note');
    assert.equal(
      toMarkdown(report),
      [
        '### ❌ PR check failed: 2 error(s), 1 warning(s)',
        '',
        'PR #1',
        '',
        '| File | Status |',
        '|------|--------|',
        '| Changed files | ❌ 1 error(s) |',
        '| `.env` | ❌ 1 error(s) |',
        '',
        '#### Changed files',
        '',
        '- ❌ `x` must not be changed',
        '',
        '#### `.env`',
        '',
        '- ❌ broken',
        '- ⚠️ duplicate',
        '',
        '> ℹ️ a note',
        ...footer,
      ].join('\n'),
    );
  });
});
