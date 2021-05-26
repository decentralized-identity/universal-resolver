const fs = require("fs")

const generateLocalFile = (testData, methodName, path) => {
    fs.writeFileSync(
        `${path}/universal-resolver-did-${methodName}.json`,
        JSON.stringify(testData, null, 4)
    );
};

const generateDefaultFile = (path) => {
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
};

module.exports = {
    generateDefaultFile,
    generateLocalFile
}
