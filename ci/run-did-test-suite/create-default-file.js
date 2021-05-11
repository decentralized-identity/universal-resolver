const fs = require("fs")

const resolvers = []

fs.readdir('./results', (err, files) => {
    files.forEach(file => {
        console.log(file);
        resolvers.push(`require('../implementations/${file}')`)
    });
    console.log(resolvers)

    const defaultFile = require('/Users/devfox/testsuites/did-test-suite/packages/did-core-test-server/suites/did-resolution/default.js');
    console.log(defaultFile)

    defaultFile.resolvers = resolvers
    console.log(defaultFile)

    fs.writeFileSync(
        '/Users/devfox/testsuites/did-test-suite/packages/did-core-test-server/suites/did-resolution/default.js',
        `module.exports = {
            name: ${JSON.stringify(defaultFile.name)},
            resolvers: [${defaultFile.resolvers}] 
        }`
    );
});

