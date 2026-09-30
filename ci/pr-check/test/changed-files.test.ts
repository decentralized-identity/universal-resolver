import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { checkChangedFiles } from '../src/checks/changed-files.ts';
import { messages, section } from './helpers.ts';

describe('checkChangedFiles', () => {
  it('accepts the checked files', () => {
    const s = section();
    checkChangedFiles(s, ['uni-resolver-web/src/main/resources/application.yml', 'docker-compose.yml', '.env', 'README.md']);
    assert.deepEqual(messages(s), []);
  });

  it('reports every other file once, including a README outside the root', () => {
    const s = section();
    checkChangedFiles(s, ['driver-did-mst/Readme.md', 'docker-compose.yml', 'driver-did-mst/Dockerfile', 'driver-did-mst/Dockerfile']);
    assert.equal(s.errors.length, 2);
    assert.match(s.errors[0]?.message ?? '', /^`driver-did-mst\/Dockerfile` must not be changed/);
    assert.match(s.errors[1]?.message ?? '', /^`driver-did-mst\/Readme.md` must not be changed/);
  });
});
