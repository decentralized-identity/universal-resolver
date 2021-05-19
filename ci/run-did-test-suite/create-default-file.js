const fs = require("fs")


fs.readdir('/Users/devfox/testsuites/did-test-suite/packages/did-core-test-server/suites/implementations', (err, files) => {

    const resolvers = []

    files.forEach(file => {
        console.log(file);
        resolvers.push(`require('../implementations/${file}')`)
    });
    console.log(resolvers)

    fs.writeFileSync(
        '/Users/devfox/testsuites/did-test-suite/packages/did-core-test-server/suites/did-resolution/default.js',
        `module.exports = {
            name: '7.1 DID Resolution',
            resolvers: [${resolvers}] 
        }`
    );
});

