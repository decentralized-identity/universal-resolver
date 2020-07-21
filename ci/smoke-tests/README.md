# universal-resolver-k8s-deployment-smoke-tests

This tool can be used as a standalone script for testing an existing deployment of the Uni Resolver, it can be integrated as a [github action](https://github.com/features/actions) into a github workflow, or it can be run as docker container.

## Run smoke-test script manually

    python smoke-test.py --host <host-where-resolver-is-deployed> --config <path-to-uni-resolver-config> --out <path-to-result-file> --write200 false
    
The script needs Python 3 and dependencies listed in the `requirements.txt`.   

Automatic installation of requirements:

    pip install --no-cache-dir -r requirements.txt

All arguments are optional and default values are aligned with github actions workflow.  

Default values:

    host: https://dev.uniresolver.io
    config: /github/workspace/config.json
    out: ./
    write200: True
   
 
## Use action in github workflow

    - name: Smoke Test
      uses: ./ci/smoke-tests
      with:
        host: <http-or-https>://<host>
        config: <path-to-uniresolver-config>
        out folder: <path-where-results-file-should-be-created>
        debug: <true/false>
        write200: <true/false>
        
Example can be seen in the [uni-resolver workflow configuration](https://github.com/decentralized-identity/universal-resolver/blob/master/.github/workflows/universal-resolver-build-deploy.yml)

## Run as docker container
### Build container with

    docker build -f Dockerfile -t smoke-test .
    
### Run container with

    docker run -it --rm -e HOST=<host-where-resolver-is-deployed> -e CONFIG_FILE=<path-to-config.json> --name smoke-test smoke-test
