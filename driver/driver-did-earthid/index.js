const express = require('express');
const axios = require('axios');
const app = express();

const PORT = process.env.PORT || 8080;

// Base URL for your DID resolver API (the API you implemented for resolving DIDs)
const RESOLVER_URL = process.env.RESOLVER_URL || 'https://did.myearth.id/v1/resolve';

/**
 * Endpoint to resolve a DID
 */
app.get('/1.0/identifiers/:did', async (req, res) => {
  const { did } = req.params;

  try {
    // Call your custom DID resolver API
    const response = await axios.get(`${RESOLVER_URL}/${encodeURIComponent(did)}`);

    res.json({
      didDocument: response.data.didDocument,
      didResolutionMetadata: { contentType: 'application/did+ld+json' },
      didDocumentMetadata: {}
    });
  } catch (error) {
    res.status(404).json({
      didResolutionMetadata: { error: 'notFound', message: error.message },
      didDocument: null,
      didDocumentMetadata: {}
    });
  }
});

app.listen(PORT, () => {
  console.log(`Driver for did:earthid running on port ${PORT}`);
});
