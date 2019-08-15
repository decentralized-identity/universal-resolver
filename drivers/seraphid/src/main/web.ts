import express = require('express');
import Resolver from './resolver'

const app: express.Application = express();
var sidRegExp = RegExp('^(did:sid:.+)$')


app.get('/1.0/identifiers/:did', async (req, res) => {
    const did: string = req.params.did;
    if(sidRegExp.test(did)){
        try {
            const ddo = await Resolver.resolve(did);
            res.send(ddo);
        } catch (err) {
            res.status(400).send({error: err.toString()})
        }
    } else {
        res.send("invalid DID format")
    }
});

app.listen(8090, () => {
    console.log('SeraphID did resolver listening on port 8090');
});

