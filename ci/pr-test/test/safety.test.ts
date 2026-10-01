import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { unsafeSettings, withDependencies } from '../src/docker/safety.ts';

describe('unsafeSettings', () => {
  it('accepts a plain service with a named volume', () => {
    const services = { driver: { image: 'x', volumes: [{ type: 'volume', source: 'data', target: '/data' }] } };
    assert.deepEqual(unsafeSettings(services, ['driver']), []);
  });

  it('rejects host access', () => {
    const services = {
      driver: {
        image: 'x',
        privileged: true,
        network_mode: 'host',
        cap_add: ['SYS_ADMIN'],
        volumes: [{ type: 'bind', source: '/var/run/docker.sock', target: '/var/run/docker.sock' }],
      },
    };
    assert.deepEqual(unsafeSettings(services, ['driver']), [
      'Service `driver` uses `privileged: true`, which is not allowed in a test run',
      'Service `driver` uses `network_mode: host`, which is not allowed in a test run',
      'Service `driver` uses `cap_add`, which is not allowed in a test run',
      'Service `driver` uses `a bind mount of `/var/run/docker.sock``, which is not allowed in a test run',
    ]);
  });

  it('only checks the given services', () => {
    assert.deepEqual(unsafeSettings({ other: { privileged: true } }, ['driver']), []);
  });
});

describe('withDependencies', () => {
  it('adds dependencies recursively, in both depends_on formats', () => {
    const services = {
      web: {},
      driver: { depends_on: { db: { condition: 'service_started' } } },
      db: { depends_on: ['cache'] },
      cache: {},
      unrelated: {},
    };
    assert.deepEqual(withDependencies(services, ['web', 'driver']), ['web', 'driver', 'db', 'cache']);
  });
});
