// follow-redirects absolutely must not be used in the browser.
// Neither should the `http` and `https` modules it replaces, yet here we are.
var http = require("http");
var https = require("https");

// eslint-disable-next-line no-undef
var browser = typeof window !== "undefined" && typeof window.document !== "undefined";

module.exports = {
  http: http,
  https: https,
  wrap: browser && function (module) {
    // eslint-disable-next-line
    console.warn("Exclude follow-redirects from browser builds.");
    return module;
  },
};

/* istanbul ignore file */
