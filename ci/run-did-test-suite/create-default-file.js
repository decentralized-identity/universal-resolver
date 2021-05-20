const fs = require("fs")

const path = '/Users/devfox/testsuites/did-test-suite/packages/did-core-test-server/suites/implementations';
fs.readdir(path, (err, files) => {

    const resolvers = []

    files.forEach(file => {
        console.log(file);
        const fileContent = JSON.parse(fs.readFileSync(`${path}/${file}`));
        console.log(fileContent);
        if (file.startsWith('universal-resolver') || file.startsWith('resolver')) {
            resolvers.push(`require('../implementations/${file}')`)
        }
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

