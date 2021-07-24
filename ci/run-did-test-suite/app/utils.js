const extractDid = (url) => {
    const didWithParams = url.split('/');
    return didWithParams[didWithParams.length - 1].split('?')[0];
}

const extractMethodName = (url) => {
    return extractDid(url).split(':')[1];
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

const getWorkingUrls = (resolutionResults) => {
    const workingUrls = [];
    const urls = Object.keys( resolutionResults );
    urls.forEach(url => {
        if (resolutionResults[url].status === 200 && resolutionResults[url].resolutionResponse["application/did+ld+json"].didDocument.id !== undefined) {
            workingUrls.push(url);
        }
    })
    return Array.from(new Set(workingUrls))
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

module.exports = {
    extractDid,
    extractMethodName,
    getWorkingMethods,
    getWorkingUrls,
    createExpectedOutcomes,
    resetTestData
}