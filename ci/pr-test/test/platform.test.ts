import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { choosePlatform, parsePlatforms } from '../src/docker/platform.ts';

describe('parsePlatforms', () => {
  it('reads multi-platform images and skips attestations', () => {
    const manifest = [
      { Descriptor: { platform: { architecture: 'amd64', os: 'linux' } } },
      { Descriptor: { platform: { architecture: 'unknown', os: 'unknown' } } },
      { Descriptor: { platform: { architecture: 'arm64', os: 'linux', variant: 'v8' } } },
    ];
    assert.deepEqual(parsePlatforms(manifest), ['linux/amd64', 'linux/arm64']);
  });

  it('reads single-platform images', () => {
    assert.deepEqual(parsePlatforms({ Descriptor: { platform: { architecture: 'amd64', os: 'linux' } } }), ['linux/amd64']);
  });
});

describe('choosePlatform', () => {
  it('runs natively available images natively', () => {
    assert.equal(choosePlatform(['linux/amd64', 'linux/arm64'], 'linux/arm64'), undefined);
    assert.equal(choosePlatform(['linux/amd64'], 'linux/amd64'), undefined);
  });

  it('falls back to linux/amd64 for images without a native variant', () => {
    assert.equal(choosePlatform(['linux/amd64'], 'linux/arm64'), 'linux/amd64');
  });

  it('leaves the decision to docker if the platforms are unknown or amd64 is missing', () => {
    assert.equal(choosePlatform(undefined, 'linux/arm64'), undefined);
    assert.equal(choosePlatform([], 'linux/arm64'), undefined);
    assert.equal(choosePlatform(['linux/s390x'], 'linux/arm64'), undefined);
  });
});
