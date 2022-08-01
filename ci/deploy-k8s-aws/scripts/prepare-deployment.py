#!/usr/bin/python3

import sys
import os
import getopt
import yaml
import subprocess
from shutil import copy
import pathlib

# CONSTANTS you may need to change:
DEFAULT_DOMAIN_NAME = 'did.civic.com'
UNIVERSAL_RESOLVER_FRONTEND_TAG = "universalresolver/uni-resolver-frontend:latest;"


def init_deployment_dir(outputdir):
    if os.path.exists(outputdir + '/' + 'deploy.sh'):
        os.remove(outputdir + '/' + 'deploy.sh')
    if not os.path.exists(outputdir):
        os.makedirs(outputdir)
    fout = open(outputdir + '/' + 'deploy.sh', "a+")
    fout.write('kubectl delete all --all -n did\n')
    fout.write('./namespace-setup.sh\n')
    fout.write('kubectl apply -n did -f uni-resolver-ingress.yaml\n')
    fout.close()
    subprocess.call(['chmod', "a+x", outputdir + '/' + 'deploy.sh'])


def add_deployment(deployment_file, outputdir):
    fout = open(outputdir + '/' + 'deploy.sh', "a+")
    fout.write('kubectl apply -n did -f %s \n' % deployment_file)
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
        
        fin.close()
        fout.close()
        # If there is a configmap-<driver>.yaml file, create a ConfigMap for it and add a volumeMounts mapping for it:
        add_driver_configmap_volume(outputdir, container)
        add_deployment(deployment_file, outputdir)

def add_driver_configmap_volume(outputdir, container):
    """
    If there is a file named /app-specs/configmap-<container>.yaml for this driver,
    add a 'kubectl apply' command for it, 
    and add a 'volume' descriptor to the Deployment yaml, referencing the configmap.
    NOTE: This currently only supports volumes, not environment variables. 
    """

    deployment_file = "deployment-%s.yaml" % container
    with open(outputdir + '/' + deployment_file, 'r') as infile:
        input_deployment_contents = infile.read()

    configmap_filename = 'configmap-%s.yaml' % container
    configmap_path = '/app-specs/%s' % configmap_filename

    if not os.path.exists(configmap_path):
        print('No configmap file found for driver ' + container)
        output_deployment_contents = input_deployment_contents.replace('{{configMapVolume}}', '')
    else:
        print('Configmap found for driver ' + container + ' . Adding configmap volume to the deployment yaml.')
        # Copy the configmap definition and add a 'kubectl apply' command for it:
        copy(configmap_path, outputdir + '/' + configmap_filename)
        add_deployment(configmap_filename, outputdir)
        
        # Write the volume mapping definition to the driver Deployment spec:
        volume_name = 'configmap-volume-%s' % container
        configmap_name = 'configmap-%s' % container

        configmap_txt = '  volumeMounts:\n'
        configmap_txt += '          - mountPath: /usr/src/app/config\n'
        configmap_txt += '            name: ' + volume_name + '\n'
        configmap_txt += '      volumes:\n'
        configmap_txt += '        - configMap:\n'
        configmap_txt += '            name: ' + configmap_name + '\n'
        configmap_txt += '          name: ' + volume_name + '\n'

        print(configmap_txt)

        output_deployment_contents = input_deployment_contents.replace('{{configMapVolume}}', configmap_txt)

    with open(outputdir + '/' + deployment_file, 'w') as outfile:
        outfile.write(output_deployment_contents)
    
    

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
    global DEFAULT_DOMAIN_NAME
    print("Generating uni-resolver-ingress.yaml")
    fout = open(outputdir + '/uni-resolver-ingress.yaml', "wt")
    fout.write('apiVersion: networking.k8s.io/v1\n')
    fout.write('kind: Ingress\n')
    fout.write('metadata:\n')
    fout.write('  name: \"uni-resolver-web\"\n')
    fout.write('  namespace: \"did\"\n')
    fout.write('  annotations:\n')
    fout.write('    nginx.ingress.kubernetes.io/rewrite-target: /$2\n')
#     fout.write('    alb.ingress.kubernetes.io/scheme: internet-facing\n')
#     fout.write('    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-2:332553390353:certificate/925fce37-d446-4af3-828e-f803b3746af0\n')
#     fout.write('    alb.ingress.kubernetes.io/listen-ports: \'[{"HTTP": 80}, {"HTTPS":443}]\'\n')
#     fout.write('    alb.ingress.kubernetes.io/actions.ssl-redirect: \'{"Type": "redirect", "RedirectConfig": { "Protocol": "HTTPS", "Port": "443", "StatusCode": "HTTP_301"}}\'\n')
    fout.write('  labels:\n')
    fout.write('    app: \"uni-resolver-web\"\n')
    fout.write('spec:\n')
    fout.write('  rules:\n')
    fout.write('    - host: ' + DEFAULT_DOMAIN_NAME + '\n')
    fout.write('      http:\n')
    fout.write('        paths:\n')
    fout.write('          - path: /()(1.0/.*)\n')
    fout.write('            pathType: ImplementationSpecific\n')
    fout.write('            backend:\n')
    fout.write('              service:\n')
    fout.write('                name: uni-resolver-web\n')
    fout.write('                port:\n')
    fout.write('                  number: 8080\n')
    fout.write('          - path: /()(.*)\n')
    fout.write('            pathType: ImplementationSpecific\n')
    fout.write('            backend:\n')
    fout.write('              service:\n')
    fout.write('                name: uni-resolver-frontend\n')
    fout.write('                port:\n')
    fout.write('                  number: 7081\n')

    fout.write('          - path: /registrar(/|$)(.*)\n')
    fout.write('            pathType: ImplementationSpecific\n')
    fout.write('            backend:\n')
    fout.write('              service:\n')
    fout.write('                name: uni-registrar-frontend\n')
    fout.write('                port:\n')
    fout.write('                  number: 8080\n')

    fout.write('          - path: /registrar/()(1.0/.*)\n')
    fout.write('            pathType: ImplementationSpecific\n')
    fout.write('            backend:\n')
    fout.write('              service:\n')
    fout.write('                name: uni-registrar-web\n')
    fout.write('                port:\n')
    fout.write('                  number: 8080\n')

    for container in containers:
        print(container)
        print(containers[container]['ports'])
        container_port = get_container_port(containers[container]['ports'])
        if container == 'uni-resolver-web':  # this is the default-name, hosted at: DEFAULT_DOMAIN_NAME
            continue
        subpath = container.replace('did', '').replace('driver', '').replace('uni-resolver', '').replace('-',
                                                                                                                   '')
        print('Adding path: ' + subpath)

        fout.write('          - path: /' + subpath + '(/|$)(.*)\n')
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

    # Payer key for the sol did driver:
    # NOTE: Before running this, the real key should be put into the yaml file.
    copy('/app-specs/secret-driver-did-sol.yaml', outputdir)
    add_deployment('secret-driver-did-sol.yaml', outputdir)
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
