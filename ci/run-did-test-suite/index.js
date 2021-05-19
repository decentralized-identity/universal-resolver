const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');
const axios = require('axios');

const testDataSkeleton = {
    implementation: 'Universal Resolver',
    implementer: 'Decentralized Identity Foundation and Contributors',
    expectedOutcomes: {
        defaultOutcomes: [],
        invalidDidErrorOutcome: [],
        notFoundErrorOutcome: [],
        representationNotSupportedErrorOutcome: [],
        deactivatedOutcome: []
    },
    executions: []
}

const extractDid = (url) => {
    const splitUrl = url.split('/');
    return splitUrl[splitUrl.length - 1];
}

const extractMethodName = (url) => {
    return extractDid(url).split(':')[1];
}

const getTestResults = async(testData) => {
    return await axios.post('http://0.0.0.0:8080/test-suite-manager/generate-report', testData);
}

const writeFile = (testData, methodName) => {
    fs.writeFileSync(
        `/Users/devfox/testsuites/did-test-suite/packages/did-core-test-server/suites/implementations/universal-resolver-did-${methodName}.json`,
        JSON.stringify(testData)
    )
}

const getWorkingMethods = (resolutionResults) => {
    const workingMethods = [];
    const urls = Object.keys( resolutionResults );
    urls.forEach(url => {
        if (resolutionResults[url].status === 200) {
            workingMethods.push(extractMethodName(url));
        }
    })
    return Array.from(new Set(workingMethods))
}

try {
    // // `who-to-greet` input defined in action metadata file
    // const nameToGreet = core.getInput('who-to-greet');
    // console.log(`Hello ${nameToGreet}!`);
    // const time = (new Date()).toTimeString();
    // core.setOutput("time", time);
    // Get the JSON webhook payload for the event that triggered the workflow
    const payload = JSON.stringify(github.context.payload, undefined, 2)
    console.log(`The event payload: ${payload}`);

    // const filename = core.getInput('file');
    const filename = '/Users/devfox/tmp/driver-status-2021-05-19_09-51-50-UTC.json';
    console.log(`Running test-suite against ${filename}`);

    const mode = "MANUAL";
    // const mode = "AUTOMATIC";

    const rawData = fs.readFileSync(filename);
    const resolutionResults = JSON.parse(rawData);

    const workingMethods = getWorkingMethods(resolutionResults)
    const urls = Object.keys(resolutionResults);

    const testData = testDataSkeleton;
    workingMethods.forEach(methodName => {
        testData.executions = [];
        testData.expectedOutcomes.defaultOutcomes = [];

        urls.forEach(url => {
            console.log('### Key', url)
            console.log('### Value', resolutionResults[url])

            if (resolutionResults[url].status === 200 && methodName === extractMethodName(extractDid(url))) {
                testData.expectedOutcomes.defaultOutcomes[0] === undefined ?
                    testData.expectedOutcomes.defaultOutcomes[0] = 0 :
                    testData.expectedOutcomes.defaultOutcomes.push(testData.expectedOutcomes.defaultOutcomes.length)

                testData.executions.push({
                    function: 'resolveRepresentation',
                    input: {
                        did: extractDid(url),
                        resolutionOptions: {
                            accept: "application/did+ld+json"
                        }
                    },
                    output: {
                        didResolutionMetadata: resolutionResults[url].resolutionResponse["application/did+ld+json"].didResolutionMetadata,
                        didDocumentStream: JSON.stringify(resolutionResults[url].resolutionResponse["application/did+ld+json"].didDocument),
                        didDocumentMetadata: resolutionResults[url].resolutionResponse["application/did+ld+json"].didDocumentMetadata
                    }
                })

            }
        });

        if (mode === "MANUAL") writeFile(testData, methodName);
        if (mode === "AUTOMATIC") getTestResults(createSuitesInput(testData));
    })
} catch (error) {
    core.setFailed(error.message);
}