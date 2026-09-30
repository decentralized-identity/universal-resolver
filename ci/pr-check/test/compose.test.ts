import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { parseMessages } from '../src/lib/compose.ts';

describe('parseMessages', () => {
  it('parses docker compose log lines and strips the project directory', () => {
    const stderr = [
      'time="2026-09-29T16:05:14Z" level=warning msg="/w/docker-compose.yml: the attribute `version` is obsolete"',
      'validating /w/docker-compose.yml: services.x additional properties \'imagee\' not allowed',
      '',
    ].join('\n');
    assert.deepEqual(parseMessages(stderr, '/w'), [
      { level: 'warning', message: 'docker-compose.yml: the attribute `version` is obsolete' },
      { level: 'error', message: "validating docker-compose.yml: services.x additional properties 'imagee' not allowed" },
    ]);
  });
});
