# EarthID DID Resolver Driver

This is the driver for resolving `did:earthid` DIDs.

## Usage

Once the driver is included in the Universal Resolver setup, it can be used to resolve EarthID DIDs as follows:

```bash
curl -X GET http://localhost:8080/1.0/identifiers/did:earthid:did:earthid:66dab28d276a3d08d4f92fd2





Step 1: Running and Testing the Driver Locally
1.1 Start the Docker container for your custom driver:
Make sure you have Docker installed and running on your local machine. Then, run the following command to start the EarthID driver container:

bash
Copy code
docker-compose up -d driver-did-earthid
This will start the driver-did-earthid container in detached mode.

1.2 Test the driver using curl:
Once the container is running, you can test it by resolving a did:earthid DID using the following curl command:

bash
Copy code
curl -X GET http://localhost:8080/1.0/identifiers/did:earthid:<test-did>
Replace <test-did> with a valid EarthID DID. This will trigger the driver to resolve the DID and return its DID Document.

1.3 Expected Output Example:
The following is an example of the expected output:

json
Copy code
{
  "@context": "https://www.w3.org/ns/did/v1",
  "id": "did:earthid:<example>",
  "verificationMethod": [
    {
      "id": "did:earthid:<example>#keys-1",
      "type": "Ed25519VerificationKey2018",
      "controller": "did:earthid:<example>",
      "publicKeyBase58": "<public-key>"
    }
  ],
  "authentication": [
    "did:earthid:<example>#keys-1"
  ]
}