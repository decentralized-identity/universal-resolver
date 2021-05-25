const axios = require('axios');
const fs = require('fs');
const dayjs = require('dayjs')

const writeReport = (report) => {
    const timestamp = new Date().toISOString();
    fs.writeFileSync(`/Users/devfox/resolver/driver-status-reports/did-testsuite-report-${timestamp}.json`, JSON.stringify(report, null, 2))
}

const runTests = (resolvers, host) => {
    axios.post(host, [{
        name: 'did-resolution',
        resolvers: resolvers}]
    ).then(res => {
        console.log(res);
        writeReport(res.data.suitesReportJson);
    }).catch(err => console.log(err));
}

module.exports = { runTests }
