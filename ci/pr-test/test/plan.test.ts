import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import type { Services } from '../src/plan/changes.ts';
import { effectiveUrl, parseDrivers } from '../src/plan/drivers.ts';
import { planTests } from '../src/plan/plan.ts';

const yml = (drivers: string) => `uniresolver:\n  drivers:\n${drivers}`;
const driver = (method: string, url: string, ids = [`did:${method}:1`]) =>
  `    - pattern: "^(did:${method}:.+)$"\n      url: ${url}\n      testIdentifiers:\n${ids.map((id) => `        - ${id}\n`).join('')}`;

const BASE_YML = yml(
  driver('btcr2', 'http://driver-did-btcr2:8080/') +
    driver('web', '${uniresolver_web_driver_url_did_web:http://uni-resolver-driver-did-uport:8081/}') +
    driver('ethr', '${uniresolver_web_driver_url_did_ethr:http://uni-resolver-driver-did-uport:8081/}') +
    driver('moncon', 'https://did.driver.moncon.co/1.0/identifiers/$1'),
);
const BASE_SERVICES: Services = {
  'uni-resolver-web': { image: 'web:latest', environment: {} },
  'driver-did-btcr2': { image: 'btcr2:1' },
  'uni-resolver-driver-did-uport': { image: 'uport:1' },
};

function plan(headYml: string, headServices: Services) {
  return planTests({ baseDrivers: parseDrivers(BASE_YML), headDrivers: parseDrivers(headYml), baseServices: BASE_SERVICES, headServices });
}
const summary = (p: ReturnType<typeof plan>) => ({
  drivers: p.drivers.map((d) => `${d.driver.pattern} → ${d.service ?? 'external'}: ${d.reason}`),
  services: p.services,
  problems: p.problems,
});

describe('planTests', () => {
  it('tests nothing if nothing changed', () => {
    assert.deepEqual(summary(plan(BASE_YML, BASE_SERVICES)), { drivers: [], services: [], problems: [] });
  });

  it('tests a driver whose test identifiers changed', () => {
    const head = BASE_YML.replace('did:btcr2:1', 'did:btcr2:2');
    assert.deepEqual(summary(plan(head, BASE_SERVICES)), {
      drivers: ['^(did:btcr2:.+)$ → driver-did-btcr2: driver entry changed in `application.yml`'],
      services: ['driver-did-btcr2'],
      problems: [],
    });
  });

  it('tests all drivers pointing to a changed service, but only that service', () => {
    const head = { ...BASE_SERVICES, 'uni-resolver-driver-did-uport': { image: 'uport:2' } };
    assert.deepEqual(summary(plan(BASE_YML, head)), {
      drivers: [
        '^(did:web:.+)$ → uni-resolver-driver-did-uport: service `uni-resolver-driver-did-uport` changed in `docker-compose.yml`',
        '^(did:ethr:.+)$ → uni-resolver-driver-did-uport: service `uni-resolver-driver-did-uport` changed in `docker-compose.yml`',
      ],
      services: ['uni-resolver-driver-did-uport'],
      problems: [],
    });
  });

  it('tests a new driver with its new service', () => {
    const head = BASE_YML + driver('mst', 'http://driver-did-mst:8080/');
    const services = { ...BASE_SERVICES, 'driver-did-mst': { image: 'mst:1' } };
    assert.deepEqual(summary(plan(head, services)), {
      drivers: ['^(did:mst:.+)$ → driver-did-mst: new driver in `application.yml`'],
      services: ['driver-did-mst'],
      problems: [],
    });
  });

  it('tests a changed driver with an external URL without starting a service', () => {
    const head = BASE_YML.replace('did:moncon:1', 'did:moncon:2');
    assert.deepEqual(summary(plan(head, BASE_SERVICES)), {
      drivers: ['^(did:moncon:.+)$ → external: driver entry changed in `application.yml`'],
      services: [],
      problems: [],
    });
  });

  it('reports a changed driver pointing to a service that does not exist', () => {
    const head = BASE_YML + driver('mst', 'http://driver-did-mst:8080/');
    assert.deepEqual(summary(plan(head, BASE_SERVICES)).problems, [
      'Driver `^(did:mst:.+)$` points to `http://driver-did-mst:8080/`, but there is no service `driver-did-mst` in `docker-compose.yml`',
    ]);
  });

  it('reports a changed driver without test identifiers', () => {
    const head = BASE_YML.replace(/    - pattern: "\^\(did:btcr2[\s\S]*?(?=    - pattern)/, '    - pattern: "^(did:btcr2:.+)$"\n      url: http://driver-did-btcr2:8080/\n');
    assert.deepEqual(summary(plan(head, BASE_SERVICES)).problems, ['Driver `^(did:btcr2:.+)$` has no `testIdentifiers`']);
  });
});

describe('effectiveUrl', () => {
  it('uses the variable from the resolver environment if set, otherwise the default', () => {
    const url = '${uniresolver_web_driver_url_did_web:http://driver:8080/}';
    assert.equal(effectiveUrl(url, {}), 'http://driver:8080/');
    assert.equal(effectiveUrl(url, { uniresolver_web_driver_url_did_web: null }), 'http://driver:8080/');
    assert.equal(effectiveUrl(url, { uniresolver_web_driver_url_did_web: 'http://other:9000/' }), 'http://other:9000/');
    assert.equal(effectiveUrl('http://plain:8080/', {}), 'http://plain:8080/');
  });
});
