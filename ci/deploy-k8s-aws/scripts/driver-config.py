#!/usr/bin/python3

import os
import sys
import yaml


def append_to_container(config, driver, spec):
    for entry in config[driver]:
        spec['spec']['template']['spec']['containers'][0][entry] = config[driver][entry]
    return spec


def add_config(config):
    for driver in config:
        spec_list = []
        with open('./deploy/deployment-' + driver + '.yaml', 'r+') as spec_file:
            for spec in yaml.load_all(spec_file, Loader=yaml.FullLoader):

                if spec['kind'] == 'Deployment':
                    changed_spec = append_to_container(config, driver, spec)
                    print(changed_spec)
                    spec_list.append(spec)
                else:
                    spec_list.append(spec)

            spec_file.close()

        with open('./deploy/deployment-' + driver + '.yaml', 'w') as spec_file:
            yaml.dump_all(spec_list, spec_file, default_flow_style=False)
            spec_file.close()


def main(argv):
    config = yaml.load(open('driver-config.yaml', 'r'), Loader=yaml.FullLoader)
    add_config(config)


if __name__ == "__main__":
    main(sys.argv[1:])
    print('%s script finished' % os.path.basename(__file__))
