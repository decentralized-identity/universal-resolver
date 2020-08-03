import os
import sys
import getopt


def substitute_values(file_content, url, cert):
    file_content = file_content.replace('RPC_URL_TESTNET', url).replace('RPC_CERT_TESTNET', cert)
    print(file_content)
    return file_content


def main(argv):
    url: str = ""
    cert: str = ""

    try:
        opts, args = getopt.getopt(argv, "u:c", ["url=", "cert="])
    except getopt.GetoptError:
        sys.exit(2)

    for opt, arg in opts:
        if opt in ("-u", "--url"):
            url = arg
        elif opt in ("-c", "--cert"):
            cert = arg

    btcr_deployment_read = open('./deploy/deployment-driver-did-btcr.yaml', 'rt')
    file_content = btcr_deployment_read.read()
    btcr_deployment_read.close()

    updated_content = substitute_values(file_content, url, cert)

    btcr_deployment_write = open('./deploy/deployment-driver-did-btcr.yaml', 'wt')
    btcr_deployment_write.write(updated_content)
    btcr_deployment_write.close()


if __name__ == "__main__":
    main(sys.argv[1:])
    print('%s script finished' % os.path.basename(__file__))
