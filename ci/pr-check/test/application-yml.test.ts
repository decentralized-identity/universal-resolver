import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { checkApplicationYml, driverUrlHost, isConfigurableUrl } from '../src/checks/application-yml.ts';
import { messages, section } from './helpers.ts';

const yml = (drivers: string) => `uniresolver:\n  drivers:\n${drivers}`;
const BTCR = `    - pattern: "^(did:btcr:.+)$"
      url: \${uniresolver_web_driver_url_did_btcr:http://driver-did-btcr:8080/}
      testIdentifiers:
        - did:btcr:xz35-jznz-q9yu-ply
`;

describe('checkApplicationYml', () => {
  it('returns the drivers of a valid file', () => {
    const s = section();
    const drivers = checkApplicationYml(s, yml(BTCR));
    assert.deepEqual(messages(s), []);
    assert.deepEqual(drivers, [
      { pattern: '^(did:btcr:.+)$', url: '${uniresolver_web_driver_url_did_btcr:http://driver-did-btcr:8080/}' },
    ]);
  });

  it('reports invalid YAML with its position', () => {
    const s = section();
    checkApplicationYml(s, 'uniresolver:\n  drivers:\n  - a: 1\n - b: 2\n');
    assert.match(messages(s)[0] ?? '', /^error: Invalid YAML at line 4, column 1: /);
  });

  it('reports duplicate keys', () => {
    const s = section();
    checkApplicationYml(s, yml(BTCR.replace('      url:', '      pattern: "^(did:x:.+)$"\n      url:')));
    assert.match(messages(s)[0] ?? '', /^error: Invalid YAML at line 4, column 7: Map keys must be unique$/);
  });

  it('requires a non-empty driver list', () => {
    const s = section();
    checkApplicationYml(s, 'uniresolver:\n  drivers: []\n');
    assert.deepEqual(messages(s), ['error: `uniresolver.drivers` must be a non-empty list']);
  });

  it('requires pattern, url and test identifiers', () => {
    const s = section();
    const drivers = checkApplicationYml(s, yml('    - pattern: "^(did:x:.+)$"\n'));
    assert.deepEqual(drivers, []);
    assert.deepEqual(messages(s), [
      'error: Driver #1 (`^(did:x:.+)$`): required key `url` is missing or empty',
      'error: Driver #1 (`^(did:x:.+)$`): at least one entry in `testIdentifiers` is required',
    ]);
  });

  it('rejects an empty test identifier list', () => {
    const s = section();
    checkApplicationYml(s, yml(BTCR.replace(/testIdentifiers:[\s\S]*/, 'testIdentifiers: []\n')));
    assert.deepEqual(messages(s), ['error: Driver #1 (`^(did:btcr:.+)$`): at least one entry in `testIdentifiers` is required']);
  });

  it('warns about unknown keys and test identifiers not matching the pattern', () => {
    const s = section();
    checkApplicationYml(s, yml(`${BTCR}        - did:other:123\n      propertyEndpoint: "true"\n`));
    assert.deepEqual(messages(s), [
      'warning: Driver #1 (`^(did:btcr:.+)$`): unknown key `propertyEndpoint` is ignored (typo? allowed: acceptHeaderValue, acceptHeaderValueDereference, pattern, propertiesEndpoint, supportsDereference, supportsOptions, testIdentifiers, traits, url)',
      'warning: Driver #1 (`^(did:btcr:.+)$`): test identifier `did:other:123` does not match the pattern',
    ]);
  });

  it('reports duplicate patterns', () => {
    const s = section();
    checkApplicationYml(s, yml(BTCR + BTCR));
    assert.deepEqual(messages(s), ['error: Driver #2 (`^(did:btcr:.+)$`): duplicate pattern, already used by driver #1']);
  });
});

describe('driverUrlHost', () => {
  it('uses the default of a placeholder', () => {
    assert.equal(driverUrlHost('${var:http://driver-did-btcr:8080/}'), 'driver-did-btcr');
    assert.equal(isConfigurableUrl('${var:http://driver-did-btcr:8080/}'), true);
  });

  it('handles plain URLs', () => {
    assert.equal(driverUrlHost('https://did.driver.moncon.co/1.0/identifiers/$1'), 'did.driver.moncon.co');
    assert.equal(isConfigurableUrl('https://did.driver.moncon.co/'), false);
  });
});
