import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { checkImages } from '../src/checks/images.ts';
import type { Pullability } from '../src/lib/registry.ts';
import { messages, section } from './helpers.ts';

const results: Record<string, Pullability> = {
  'public:1': { pullable: true },
  'private:1': { pullable: false, level: 'error', reason: 'does not exist or requires a login' },
  'flaky:1': { pullable: false, level: 'warning', reason: 'could not be verified (ETIMEDOUT)' },
};

describe('checkImages', () => {
  it('reports images that cannot be pulled with their services', async () => {
    const s = section();
    const images = new Map([
      ['public:1', ['a']],
      ['private:1', ['b', 'c']],
      ['flaky:1', ['d']],
    ]);
    await checkImages(s, images, async (image) => results[image] ?? { pullable: true });
    assert.deepEqual(messages(s), [
      'warning: Image `flaky:1` (service `d`) could not be verified (ETIMEDOUT)',
      'error: Image `private:1` (service `b`, `c`) does not exist or requires a login',
    ]);
  });
});
