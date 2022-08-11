#!/usr/bin/env python
import sys
import datetime
import logging
import re
import json
import getopt
import asyncio
import yaml
from aiohttp import ClientSession


WRITE_SUCCESS: bool = True

logging.basicConfig(
    format="%(asctime)s %(levelname)s:%(name)s: %(message)s",
    level=logging.DEBUG,
    datefmt="%H:%M:%S",
    stream=sys.stderr,
)
logger = logging.getLogger("areq")
logging.getLogger("chardet.charsetprober").disabled = True


# Create Test Data START
def parse_json_to_dict(path):
    with open(path) as file:
        raw_config = yaml.safe_load(file)
        return raw_config


def extract_did_method(did):
    return re.findall("(?<=:)(.*?)(?=:)", did)[0]


def create_test_data(drivers_config, host):
    test_data = []
    for driver in drivers_config:
        for testIdentifier in driver["testIdentifiers"]:
            if testIdentifier.startswith("did:"):
                driver_test_data = {
                    "method": extract_did_method(testIdentifier),
                    "url": host + testIdentifier
                }
                test_data.append(driver_test_data)

    return test_data


# Create Test Data END

# Run tests START
async def fetch_html(url: str, session: ClientSession):
    resp = await session.request(method="GET", url=url)
    plain_html = await resp.text()
    logger.info("Got response [%s] for URL: %s", resp.status, url)
    logger.info("With body:\n %s", plain_html)

    if resp.status == 200:
        did_document = json.loads(plain_html)
        logger.info("With didDocument:\n %s", did_document)
        result = {"status": resp.status, "resolutionResponse": {}}
        result["resolutionResponse"]["application/did+ld+json"] = did_document
        return result
    else:
        return {"status": resp.status, "error": plain_html}


async def write_one(results, data, session):
    url = data['url']
    try:
        res = await fetch_html(url=url, session=session)
        if WRITE_SUCCESS | res['status'] != 200:
            results.update({url: res})
    except asyncio.TimeoutError:
        results.update({
            url: {
                "status": 504,
                "body": "Gateway Timeout error"
            }
        })
        logger.info("Gateway Timeout error for %s", url)
    print("\n-----------------------------------------------------------------------------------------------\n")


async def run_tests(test_data):
    async with ClientSession() as session:
        tasks = []
        results = {}
        for data in test_data:
            tasks.append(
                write_one(results, data=data, session=session)
            )
        await asyncio.gather(*tasks)
        return results


# Run tests END

def main(argv):
    help_text = './get-driver-status.py -host <uni-resolver-host> -config <uni-resolver-config> -out <out-folder> ' \
                '--write200 <True/False>'
    host = 'https://dev.uniresolver.io'
    config = '/github/workspace/uni-resolver-web/src/main/resources/application.yml'
    out = './'
    try:
        opts, args = getopt.getopt(argv, "h:c:o:w", ["host=", "config=", "out=", "write200="])
    except getopt.GetoptError:
        print(help_text)
        sys.exit(2)
    for opt, arg in opts:
        if opt == '--help':
            print(help_text)
            sys.exit()
        elif opt in ("-h", "--host"):
            host = arg
        elif opt in ("-c", "--config"):
            config = arg
        elif opt in ("-o", "--out"):
            print("ARG:" + arg)
            out = arg + '/'
        elif opt in ("-w", "--write200"):
            global WRITE_SUCCESS
            if arg.lower() == 'false':
                WRITE_SUCCESS = False

    uni_resolver_path = host + "/1.0/identifiers/"
    print('Resolving for: ' + uni_resolver_path)

    # build test data
    config_dict = parse_json_to_dict(config)
    test_data = create_test_data(config_dict["uniresolver"]["drivers"], uni_resolver_path)

    # run tests
    results = asyncio.run(run_tests(test_data=test_data))

    results_timestamp = datetime.datetime.utcnow().replace(microsecond=0).isoformat()
    filename = "driver-status-" + results_timestamp + ".json"
    print('Out folder: ' + out)
    out_path = out + filename
    print('Writing to path: ' + out_path)
    with open(out_path, "a") as f:
        f.write(json.dumps(results, indent=4, sort_keys=True))


if __name__ == "__main__":
    main(sys.argv[1:])
    print('Script finished')
