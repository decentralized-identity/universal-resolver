const axios = require('axios');
const fs = require('fs');

const runTests = (resolvers, host, outputPath) => {
    axios.post(host, [{
        name: 'did-resolution',
        resolvers: resolvers}]
    ).then(res => {
        const timestamp = new Date().toISOString().split('.')[0];
        fs.writeFileSync(`${outputPath}/did-testsuite-report-${timestamp}.json`, JSON.stringify(res.data.suitesReportJson, null, 2))
    }).catch(err => console.log(err));
}

module.exports = { runTests }
