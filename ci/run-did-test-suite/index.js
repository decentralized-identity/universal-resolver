const core = require('@actions/core');
const github = require('@actions/github');
const fs = require('fs');
const axios = require('axios');

const testDataSkeleton = {
    implementation: 'Universal Resolver',
    implementer: 'Danubetech GmbH',
    expectedOutcomes: {
        defaultOutcomes: [0,4,5,6],
        invalidDidErrorOutcome: [1],
        notFoundErrorOutcome: [2],
        representationNotSupportedErrorOutcome: [],
        deactivatedOutcome: [3]
    },
    executions: []
}

const extractDid = (url) => {
    const splitUrl = url.split('/');
    return splitUrl[splitUrl.length - 1];
}

const getTestResults = async(testData) => {
    return await axios.post('http://0.0.0.0:8080/test-suite-manager/generate-report', testData);
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
    const filename = '/Users/devfox/tmp/driver-status-2021-04-19_15-58-42-UTC.json';
    console.log(`Running test-suite against ${filename}`);

    const rawData = fs.readFileSync(filename);
    const resolutionResults = JSON.parse(rawData);
    console.log(resolutionResults)

    const testData = testDataSkeleton;
    const keys = Object.keys( resolutionResults );
    keys.forEach(key => {
        console.log('### Key', key)
        console.log('### Value', resolutionResults[key])

        if (resolutionResults[key].status === 200) {
            testData.executions.push({
                function: 'resolve',
                input: {
                    did: extractDid(key),
                    resolutionOptions: {}
                },
                output: {
                    didResolutionMetadata: resolutionResults[key].didResolutionMetadata,
                    didDocument: resolutionResults[key].didDocument,
                    didDocumentMetadata: resolutionResults[key].didDocumentMetadata
                }
            })
        }
    });

    console.log(testData);

    testResults = getTestResults(testData);

    console.log('###TestResults')
    console.log(testResults)

} catch (error) {
    core.setFailed(error.message);
}