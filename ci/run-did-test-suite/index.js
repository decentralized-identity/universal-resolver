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
    const didWithParams = url.split('/');
    return didWithParams[didWithParams.length - 1].split('?')[0];
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
        if (resolutionResults[url].status === 200 && resolutionResults[url].resolutionResponse["application/did+ld+json"].didDocument.id !== undefined) {
            workingMethods.push(extractMethodName(url));
        }
    })
    return Array.from(new Set(workingMethods))
}

const createExpectedOutcomes = (testData, resolutionResult, index) => {

    if (resolutionResult.resolutionResponse["application/did+ld+json"].didDocumentMetadata.deactivated === true) {
        testData.expectedOutcomes.deactivatedOutcome[0] === undefined ?
            testData.expectedOutcomes.deactivatedOutcome[0] = index :
            testData.expectedOutcomes.deactivatedOutcome.push(index)
    } else {
        testData.expectedOutcomes.defaultOutcomes[0] === undefined ?
            testData.expectedOutcomes.defaultOutcomes[0] = index :
            testData.expectedOutcomes.defaultOutcomes.push(index)
    }
}

const resetTestData = (testData) => {
    testData.executions = [];
    testData.expectedOutcomes.defaultOutcomes = [];
    testData.expectedOutcomes.invalidDidErrorOutcome = [];
    testData.expectedOutcomes.notFoundErrorOutcome = [];
    testData.expectedOutcomes.representationNotSupportedErrorOutcome = [];
    testData.expectedOutcomes.deactivatedOutcome = [];
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
    const filename = '/Users/devfox/tmp/driver-status-2021-05-19_15-08-15-UTC.json';
    console.log(`Running test-suite against ${filename}`);

    const mode = "MANUAL";
    // const mode = "AUTOMATIC";

    const rawData = fs.readFileSync(filename);
    const resolutionResults = JSON.parse(rawData);

    const workingMethods = getWorkingMethods(resolutionResults)
    console.log('##Working methods', workingMethods)
    const urls = Object.keys(resolutionResults);

    const testData = testDataSkeleton;
    workingMethods.forEach(workingMethodName => {
        resetTestData(testData);

        let index = 0;
        urls.forEach(url => {
            if (workingMethodName === extractMethodName(extractDid(url))) {
                createExpectedOutcomes(testData, resolutionResults[url], index)

                testData.executions.push({
                    function: 'resolveRepresentation',
                    input: {
                        did: resolutionResults[url].resolutionResponse["application/did+ld+json"].didDocument.id,
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
                index++;
            }
        });

        if (mode === "MANUAL") writeFile(testData, workingMethodName);
        if (mode === "AUTOMATIC") getTestResults(createSuitesInput(testData));
    })
} catch (error) {
    core.setFailed(error.message);
}