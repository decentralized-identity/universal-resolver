import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { parse } from 'yaml';
import { composeOverride } from '../src/docker/override.ts';

describe('composeOverride', () => {
  const override = composeOverride({
    applicationYml: 'url: ${var:http://driver:8080/}\n',
    network: 'pr-test-1',
    services: ['driver-did-x'],
  });
  // !override/!reset are docker compose tags; read them as plain values here
  const config = parse(override, { customTags: [{ tag: '!override', collection: 'seq' }, { tag: '!reset', collection: 'seq' }] });

  it('injects the application.yml with escaped interpolation', () => {
    assert.equal(config.configs['pr-test-application-yml'].content, 'url: $${var:http://driver:8080/}\n');
    assert.deepEqual(config.services['uni-resolver-web'].configs, [{ source: 'pr-test-application-yml', target: '/pr-test/application.yml' }]);
    assert.equal(config.services['uni-resolver-web'].environment.SPRING_CONFIG_LOCATION, 'file:/pr-test/application.yml');
  });

  it('uses an own network and no fixed host ports', () => {
    assert.equal(config.networks.default.name, 'pr-test-1');
    assert.match(override, /"uni-resolver-web":\n(.*\n)*    ports: !override \["8080"\]/);
    assert.match(override, /"driver-did-x":\n    ports: !reset \[\]/);
  });
});

describe('composeOverride with platforms', () => {
  it('requests the platform only for the given services', () => {
    const override = composeOverride({
      applicationYml: '',
      network: 'n',
      services: ['driver-a', 'driver-b'],
      platforms: new Map([['driver-a', 'linux/amd64']]),
    });
    assert.match(override, /"driver-a":\n    ports: !reset \[\]\n    platform: "linux\/amd64"\n/);
    assert.match(override, /"driver-b":\n    ports: !reset \[\]\n$/);
    assert.doesNotMatch(override, /"uni-resolver-web":[\s\S]*platform:[\s\S]*"driver-a"/);
  });
});
