const axios = require('axios');

const runTests = (resolvers, host) => {
    axios.post(host, [{name: 'did-resolution',
        resolvers: resolvers}]).then(res => console.log(res)).catch(err => console.log(err));
}

module.exports = { runTests }
