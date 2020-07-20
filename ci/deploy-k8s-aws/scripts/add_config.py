#!/usr/bin/python3

import sys
import yaml


def add_trustbloc_config(path_to_deployment):
    print('Adding config to ' + path_to_deployment)
    spec_list = []
    with open(path_to_deployment, 'r+') as spec_file:
        for spec in yaml.load_all(spec_file, Loader=yaml.FullLoader):

            if spec['kind'] == 'Deployment':
                spec['spec']['template']['spec']['containers'][0]['command'] = ['sh', '-c', 'did-method start']
                spec['spec']['template']['spec']['containers'][0]['env'] = [
                    {'name': 'DID_METHOD_HOST_URL', 'value': '0.0.0.0:8102'},
                    {'name': 'DID_METHOD_TLS_SYSTEMCERTPOOL', 'value': 'true'},
                    {'name': 'DID_METHOD_MODE', 'value': 'resolver'}
                ]
                print(spec)
                spec_list.append(spec)
            else:
                spec_list.append(spec)

        spec_file.close()

    with open(path_to_deployment, 'w') as spec_file:
        yaml.dump_all(spec_list, spec_file, default_flow_style=False)
        spec_file.close()


def main(argv):
    add_trustbloc_config('./deploy/deployment-driver-did-trustbloc.yaml')


if __name__ == "__main__":
    main(sys.argv[1:])
    print('Script finished')
