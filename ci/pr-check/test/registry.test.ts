import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { parseAuthChallenge, parseImageReference } from '../src/lib/registry.ts';

describe('parseImageReference', () => {
  const cases: [string, [string, string, string]][] = [
    ['nginx', ['registry-1.docker.io', 'library/nginx', 'latest']],
    ['nginx:1.27-alpine', ['registry-1.docker.io', 'library/nginx', '1.27-alpine']],
    ['universalresolver/driver-did-btcr:latest', ['registry-1.docker.io', 'universalresolver/driver-did-btcr', 'latest']],
    ['docker.io/uport/uni-resolver-driver-did-uport:5.0.3', ['registry-1.docker.io', 'uport/uni-resolver-driver-did-uport', '5.0.3']],
    ['ghcr.io/org/team/image:1.0', ['ghcr.io', 'org/team/image', '1.0']],
    ['localhost:5000/image', ['localhost:5000', 'image', 'latest']],
    ['python@sha256:abc', ['registry-1.docker.io', 'library/python', 'sha256:abc']],
    ['quay.io/org/image:1@sha256:abc', ['quay.io', 'org/image', 'sha256:abc']],
  ];
  for (const [image, [registry, repository, reference]] of cases) {
    it(image, () => assert.deepEqual(parseImageReference(image), { registry, repository, reference }));
  }
});

describe('parseAuthChallenge', () => {
  it('parses a bearer challenge', () => {
    assert.deepEqual(parseAuthChallenge('Bearer realm="https://auth.docker.io/token",service="registry.docker.io"'), {
      scheme: 'bearer',
      params: { realm: 'https://auth.docker.io/token', service: 'registry.docker.io' },
    });
  });

  it('handles a missing header', () => {
    assert.deepEqual(parseAuthChallenge(null), { scheme: '', params: {} });
  });
});
