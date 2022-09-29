#!/usr/bin/python3

import sys
import os
import getopt
import yaml
import subprocess
from shutil import copy
import pathlib

# CONSTANTS you may need to change:
PROD_DOMAIN_NAME = 'resolver.identity.foundation'
DEV_DOMAIN_NAME = 'dev.uniresolver.io'
UNIVERSAL_RESOLVER_FRONTEND_TAG = "universalresolver/uni-resolver-frontend:latest;"
NAMESPACE = "uni-resolver-dev"


def init_deployment_dir(outputdir):
    if os.path.exists(outputdir + '/' + 'deploy.sh'):
        os.remove(outputdir + '/' + 'deploy.sh')
    if not os.path.exists(outputdir):
        os.makedirs(outputdir)
    fout = open(outputdir + '/' + 'deploy.sh', "a+")
    fout.write(f'kubectl delete all --all -n {NAMESPACE}\n')
    fout.write('./namespace-setup.sh\n')
    fout.write(f'kubectl apply -n {NAMESPACE} -f uni-resolver-ingress.yaml\n')
    fout.close()
    subprocess.call(['chmod', "a+x", outputdir + '/' + 'deploy.sh'])


def add_deployment(deployment_file, outputdir):
    fout = open(outputdir + '/' + 'deploy.sh', "a+")
    fout.write(f'kubectl apply -n {NAMESPACE} -f %s \n' % deployment_file)
    fout.close()


def get_container_name(container_tag):
    split_list = container_tag.split('/')
    return split_list[-1].split(':')[0]


def get_container_port(ports):
    port = ports[0].split(':')
    return port[1]


def generate_deployment_specs(containers, outputdir):
    for container in containers:
        container_tag = containers[container]['image']
        container_port = get_container_port(containers[container]['ports'])
        fin = open("k8s-template.yaml", "rt")
        deployment_file = "deployment-%s.yaml" % container
        fout = open(outputdir + '/' + deployment_file, "wt")
        print('Writing file: ' + outputdir + '/' + deployment_file + ' for container: ' + container)
        for line in fin:
            fout.write(line.replace('{{containerName}}', container).replace('{{containerTag}}', container_tag).replace('{{containerPort}}', container_port))
        add_deployment(deployment_file, outputdir)
        fin.close()
        fout.close()


def find_in_dir(key, dictionary):
    for k, v in dictionary.items():
        if k == key:
            yield v
        elif isinstance(v, dict):
            for result in find_in_dir(key, v):
                yield result


def load_containers(file_name):
    with open(file_name, 'r') as file:
        full_config = yaml.full_load(file)

    print(full_config)
    return full_config['services']


def get_container_tags(containers):
    container_tags = ''
    for x in find_in_dir("image", containers):
        container_tags += x + ';'

    return container_tags


def generate_ingress(containers, outputdir):
    global PROD_DOMAIN_NAME, DEV_DOMAIN_NAME
    print("Generating uni-resolver-ingress.yaml")
    fout = open(outputdir + '/uni-resolver-ingress.yaml', "wt")
    fout.write('apiVersion: networking.k8s.io/v1\n')
    fout.write('kind: Ingress\n')
    fout.write('metadata:\n')
    fout.write('  name: \"uni-resolver-ingress\"\n')
    fout.write('  namespace: \"uni-resolver-dev\"\n')
    fout.write('  annotations:\n')
    fout.write('    alb.ingress.kubernetes.io/scheme: internet-facing\n')
    fout.write('    alb.ingress.kubernetes.io/certificate-arn: \"arn:aws:acm:us-east-2:332553390353:certificate/925fce37-d446-4af3-828e-f803b3746af0,arn:aws:acm:us-east-2:332553390353:certificate/59fa30ca-de05-4024-8f80-fea9ab9ab8bf\"\n')
    fout.write('    alb.ingress.kubernetes.io/listen-ports: \'[{"HTTP": 80}, {"HTTPS":443}]\'\n')
    fout.write('    alb.ingress.kubernetes.io/ssl-redirect: \'443\'\n')
    fout.write('  labels:\n')
    fout.write('    app: \"uni-resolver-web\"\n')
    fout.write('spec:\n')
    fout.write('  ingressClassName: alb\n')
    fout.write('  rules:\n')
    fout.write('    - host: ' + PROD_DOMAIN_NAME + '\n')
    fout.write('      http:\n')
    fout.write('        paths:\n')
    fout.write('          - path: /1.0/*\n')
    fout.write('            pathType: ImplementationSpecific\n')
    fout.write('            backend:\n')
    fout.write('              service:\n')
    fout.write('                name: uni-resolver-web\n')
    fout.write('                port:\n')
    fout.write('                  number: 8080\n')
    fout.write('          - path: /*\n')
    fout.write('            pathType: ImplementationSpecific\n')
    fout.write('            backend:\n')
    fout.write('              service:\n')
    fout.write('                name: uni-resolver-frontend\n')
    fout.write('                port:\n')
    fout.write('                  number: 7081\n')
    fout.write('    - host: ' + DEV_DOMAIN_NAME + '\n')
    fout.write('      http:\n')
    fout.write('        paths:\n')
    fout.write('          - path: /1.0/*\n')
    fout.write('            pathType: ImplementationSpecific\n')
    fout.write('            backend:\n')
    fout.write('              service:\n')
    fout.write('                name: uni-resolver-web\n')
    fout.write('                port:\n')
    fout.write('                  number: 8080\n')
    fout.write('          - path: /*\n')
    fout.write('            pathType: ImplementationSpecific\n')
    fout.write('            backend:\n')
    fout.write('              service:\n')
    fout.write('                name: uni-resolver-frontend\n')
    fout.write('                port:\n')
    fout.write('                  number: 7081\n')
    

    for container in containers:
        print(container)
        print(containers[container]['ports'])
        container_port = get_container_port(containers[container]['ports'])
        if container == 'uni-resolver-web':  # this is the default-name, hosted at: DEFAULT_DOMAIN_NAME
            continue
        sub_domain_name = container.replace('did', '').replace('driver', '').replace('uni-resolver', '').replace('-',
                                                                                                                   '')
        print('Adding domains: ' + sub_domain_name + '.' + PROD_DOMAIN_NAME + ' and ' + sub_domain_name + '.' + DEV_DOMAIN_NAME )

        fout.write('    - host: ' + sub_domain_name + '.' + PROD_DOMAIN_NAME + '\n')
        fout.write('      http:\n')
        fout.write('        paths:\n')
        fout.write('          - path: /*\n')
        fout.write('            pathType: ImplementationSpecific\n')
        fout.write('            backend:\n')
        fout.write('              service:\n')
        fout.write('                name: ' + container + '\n')
        fout.write('                port:\n')
        fout.write('                  number: ' + container_port + '\n')
        fout.write('    - host: ' + sub_domain_name + '.' + DEV_DOMAIN_NAME + '\n')
        fout.write('      http:\n')
        fout.write('        paths:\n')
        fout.write('          - path: /*\n')
        fout.write('            pathType: ImplementationSpecific\n')
        fout.write('            backend:\n')
        fout.write('              service:\n')
        fout.write('                name: ' + container + '\n')
        fout.write('                port:\n')
        fout.write('                  number: ' + container_port + '\n')
        
    fout.close()


def copy_app_deployment_specs(outputdir):
    print('#### Current python working path')
    working_path = pathlib.Path().absolute()
    print(working_path)
    # Configmap has to be deployed before the applications
    copy('/app-specs/configmap-uni-resolver-frontend.yaml', outputdir + '/configmap-uni-resolver-frontend.yaml')
    add_deployment('configmap-uni-resolver-frontend.yaml', outputdir)
    copy('/app-specs/deployment-uni-resolver-frontend.yaml', outputdir + '/deployment-uni-resolver-frontend.yaml')
    add_deployment('deployment-uni-resolver-frontend.yaml', outputdir)
    copy('/app-specs/deployment-uni-resolver-web.yaml', outputdir + '/deployment-uni-resolver-web.yaml')
    add_deployment('deployment-uni-resolver-web.yaml', outputdir)


def main(argv):
    print('#### Current python script path')
    absolute_path = pathlib.Path(__file__).parent.absolute()
    print(absolute_path)

    compose = 'docker-compose.yml'
    outputdir = './deploy'
    try:
        opts, args = getopt.getopt(argv, "hi:o:", ["compose=", "outputdir="])
    except getopt.GetoptError:
        print('./prepare-deployment.py -i <inputfile> -o <outputdir>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('./prepare-deployment.py -i <inputfile> -o <outputdir>')
            sys.exit()
        elif opt in ("-i", "--compose"):
            compose = arg
        elif opt in ("-o", "--outputdir"):
            outputdir = arg

    print('Input file is:', compose)
    print('Output dir is:', outputdir)

    init_deployment_dir(outputdir)

    containers = load_containers(compose)
    print("Containers:")
    print(containers)

    generate_ingress(containers, outputdir)

    # generate driver specs
    generate_deployment_specs(containers, outputdir)

    # copy app deployment specs
    copy_app_deployment_specs(outputdir)

    # copy namespace files
    copy('/namespace/namespace-setup.yaml', './deploy/namespace-setup.yaml')
    copy('/namespace/namespace-setup.sh', './deploy/namespace-setup.sh')


if __name__ == "__main__":
    main(sys.argv[1:])
    print('%s script done' % os.path.basename(__file__))
