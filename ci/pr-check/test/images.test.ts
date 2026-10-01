import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { checkImages, NOT_PULLABLE_NOTE } from '../src/checks/images.ts';
import type { Pullability } from '../src/lib/registry.ts';
import { messages, section } from './helpers.ts';

const results: Record<string, Pullability> = {
  'public:1': { pullable: true },
  'private:1': { pullable: false, level: 'error', reason: 'does not exist or requires a login' },
  'private:2': { pullable: false, level: 'error', reason: 'does not exist or requires a login' },
  'flaky:1': { pullable: false, level: 'warning', reason: 'could not be verified (ETIMEDOUT)' },
};
const resolve = async (image: string) => results[image] ?? { pullable: true as const };

describe('checkImages', () => {
  it('reports images that cannot be pulled with their services, and the note once', async () => {
    const s = section();
    const images = new Map([
      ['public:1', ['a']],
      ['private:1', ['b', 'c']],
      ['private:2', ['e']],
      ['flaky:1', ['d']],
    ]);
    await checkImages(s, images, resolve);
    assert.deepEqual(messages(s), [
      'warning: Image `flaky:1` (service `d`) could not be verified (ETIMEDOUT)',
      'error: Image `private:1` (service `b`, `c`) does not exist or requires a login',
      'error: Image `private:2` (service `e`) does not exist or requires a login',
    ]);
    assert.deepEqual(s.notes, [NOT_PULLABLE_NOTE]);
  });

  it('adds no note for images that could only not be verified', async () => {
    const s = section();
    await checkImages(s, new Map([['flaky:1', ['d']]]), resolve);
    assert.deepEqual(s.notes, []);
  });
});
