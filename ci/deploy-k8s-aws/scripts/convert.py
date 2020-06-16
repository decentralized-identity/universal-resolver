#!/usr/bin/python3

import sys
import os
import getopt
import yaml
import subprocess

# CONSTANTS you may need to change:
DEFAULT_DOMAIN_NAME = 'uniresolver.com'
UNIVERSAL_RESOLVER_FRONTEND_TAG = "universalresolver/uni-resolver-frontend:latest;"


def initDeploymentDir(outputdir):
  if os.path.exists(outputdir + '/' + 'deploy.sh'):
    os.remove(outputdir + '/' +'deploy.sh')
  if not os.path.exists(outputdir):
    os.makedirs(outputdir)
  fout = open(outputdir + '/' + 'deploy.sh', "a+")
  fout.write('kubectl delete all --all -n uni-resolver\n')
  fout.close()
  subprocess.call(['chmod', "a+x", outputdir + '/' + 'deploy.sh'])

def addDeployment(containerName, deploymentFile, outputdir):
   fout = open(outputdir + '/' + 'deploy.sh', "a+")
   fout.write('kubectl -n uni-resolver apply -f %s \n'%deploymentFile)
   fout.close()

def getContainerNameVersion(containterTag):
  if (containterTag.find('/') < 0):
    return
  user,containerNameVersion = containterTag.split('/')
  return containerNameVersion.split(':')

def generateDeploymentSpecs(containterTags, outputdir):
    containerPort = '8080'
    for containterTag in containterTags.split(';'):
        if (containterTag == ''):
          return
        containerName, containerVersion = getContainerNameVersion(containterTag)
        fin = open("k8s-template.yaml", "rt")
        deploymentFile = "deployment-%s.yaml" % containerName
        if (containterTag == UNIVERSAL_RESOLVER_FRONTEND_TAG.replace(';','')):
          containerPort = '80'
        fout = open(outputdir + '/' + deploymentFile, "wt")
        print('Writing file: ' + outputdir + '/' + deploymentFile + ' for containter: ' + containterTag)
        for line in fin:
            fout.write(line.replace('{{containerName}}', containerName).replace('{{containterTag}}', containterTag).replace('{{containerPort}}', containerPort))
        addDeployment(containerName, deploymentFile, outputdir)
        fin.close()
        fout.close()

def findInDir(key, dictionary):
    for k, v in dictionary.items():
        if k == key:
            yield v
        elif isinstance(v, dict):
            for result in findInDir(key, v):
                yield result

def getContainerTags(fileName):
    with open(fileName, 'r') as file:
        yaml_str = file.read()
        data = yaml.safe_load(yaml_str)

    containerTags = ''
    for x in findInDir("image", data):
        containerTags += x + ';'

    return containerTags
    

def generateIngress(containterTags, outputdir):
    global DEFAULT_DOMAIN_NAME
    print("Generating uni-resolver-ingress.yaml")
    fout = open(outputdir + '/uni-resolver-ingress.yaml', "wt")
    fout.write('apiVersion: extensions/v1beta1\n')
    fout.write('kind: Ingress\n')
    fout.write('metadata:\n')
    fout.write('  name: \"uni-resolver-web\"\n')
    fout.write('  namespace: \"uni-resolver\"\n')
    fout.write('  annotations:\n')
    fout.write('    kubernetes.io/ingress.class: alb\n')
    fout.write('    alb.ingress.kubernetes.io/scheme: internet-facing\n')
    fout.write('  labels:\n')
    fout.write('    app: \"uni-resolver-web\"\n')
    fout.write('spec:\n')
    fout.write('  rules:\n')
    fout.write('    - host: ' + DEFAULT_DOMAIN_NAME + '\n')
    fout.write('      http:\n')
    fout.write('        paths:\n')
    fout.write('          - path: /1.0/*\n')
    fout.write('            backend:\n')
    fout.write('              serviceName: "uni-resolver-web"\n')
    fout.write('              servicePort: 8080\n')
    fout.write('    - host: ' + DEFAULT_DOMAIN_NAME + '\n')
    fout.write('      http:\n')
    fout.write('        paths:\n')
    fout.write('          - path: /*\n')
    fout.write('            backend:\n')
    fout.write('              serviceName: "uni-resolver-frontend"\n')
    fout.write('              servicePort: 80\n')

    for containterTag in containterTags.split(';'):
      if (containterTag == ''):
        return
      containerName, containerVersion = getContainerNameVersion(containterTag)
      if (containerName == 'uni-resolver-web'): # this is the default-name, hosted at: DEFAULT_DOMAIN_NAME
        continue
      subDomainName = containerName.replace('did', '').replace('driver', '').replace('uni-resolver', '').replace('-', '')
      print('Adding domain: ' + subDomainName + '.' + DEFAULT_DOMAIN_NAME)

      fout.write('    - host: ' + subDomainName + '.' + DEFAULT_DOMAIN_NAME + '\n')
      fout.write('      http:\n')
      fout.write('        paths:\n')
      fout.write('          - path: /*\n')
      fout.write('            backend:\n')
      fout.write('              serviceName: "' + containerName + '"\n')
      fout.write('              servicePort: 8080\n')

    fout.close()

def main(argv):
   compose = 'docker-compose.yml'
   outputdir = './out'
   try:
      opts, args = getopt.getopt(argv,"hi:o:",["compose=","outputdir="])
   except getopt.GetoptError:
      print('./convert.py -i <inputfile> -o <outputdir>')
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
         print ('./convert.py -i <inputfile> -o <outputdir>')
         sys.exit()
      elif opt in ("-i", "--compose"):
         compose = arg
      elif opt in ("-o", "--outputdir"):
         outputdir = arg

   print ('Input file is:', compose)
   print ('Output dir is:', outputdir)

   initDeploymentDir(outputdir)

   containerTags = getContainerTags(compose)
   print("Container tags: " + containerTags)

   generateIngress(containerTags, outputdir)

   # we need also a spec for the frontend
   containerTags += UNIVERSAL_RESOLVER_FRONTEND_TAG
   generateDeploymentSpecs(containerTags, outputdir)


if __name__ == "__main__":
   main(sys.argv[1:])
