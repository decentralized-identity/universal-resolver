import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { checkDriverServices } from '../src/checks/driver-services.ts';
import type { Services } from '../src/checks/docker-compose.ts';
import { messages, section } from './helpers.ts';

const drivers = [
  { pattern: '^(did:btcr:.+)$', url: '${var:http://driver-did-btcr:8080/}' },
  { pattern: '^(did:moncon:.+)$', url: 'https://did.driver.moncon.co/1.0/identifiers/$1' },
];
const services: Services = {
  'uni-resolver-web': { image: 'web' },
  'driver-did-btcr': { image: 'btcr' },
  'driver-did-moncon': { image: 'moncon' },
};

function check(allServices: Services, baseServices: Set<string> | undefined, driverList = drivers) {
  const sections = { applicationYml: section(), dockerCompose: section() };
  checkDriverServices(sections, driverList, allServices, baseServices);
  return { applicationYml: messages(sections.applicationYml), dockerCompose: messages(sections.dockerCompose) };
}

describe('checkDriverServices', () => {
  it('does not check existing services, e.g. a driver pointing to a hosted URL', () => {
    assert.deepEqual(check(services, new Set(Object.keys(services))), { applicationYml: [], dockerCompose: [] });
  });

  it('requires a driver entry for a new service', () => {
    const result = check({ ...services, 'driver-did-mst': { image: 'mst' } }, new Set(Object.keys(services)));
    assert.equal(result.applicationYml.length, 1);
    assert.match(result.applicationYml[0] ?? '', /^error: No driver entry for the new service `driver-did-mst`/);
  });

  it('accepts a new service a new driver points to, whatever its name', () => {
    const withHacera = [...drivers, { pattern: '^(did:hcr:.+)$', url: '${var:http://hacera-did-driver:8080/}' }];
    const result = check({ ...services, 'hacera-did-driver': { image: 'hacera' } }, new Set(Object.keys(services)), withHacera);
    assert.deepEqual(result, { applicationYml: [], dockerCompose: [] });
  });

  it('accepts a new service another service depends on', () => {
    const all = { ...services, 'driver-did-btcr': { image: 'btcr', depends_on: ['btcr-db'] }, 'btcr-db': { image: 'postgres' } };
    assert.deepEqual(check(all, new Set(Object.keys(services))).applicationYml, []);
  });

  it('skips new services when the base branch is unknown', () => {
    assert.deepEqual(check({ ...services, 'driver-did-mst': { image: 'mst' } }, undefined).applicationYml, []);
  });

  it('warns about a driver pointing to a service that does not exist', () => {
    const missing = [{ pattern: '^(did:x:.+)$', url: '${var:http://driver-did-x:8080/}' }];
    assert.deepEqual(check(services, undefined, missing).dockerCompose, [
      'warning: Service `driver-did-x` referenced by driver `^(did:x:.+)$` in `uni-resolver-web/src/main/resources/application.yml` is not defined',
    ]);
  });
});
