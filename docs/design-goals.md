# Universal Resolver — Design Goals

The main design objectives of the UR are:
 
* **Generic:** The UR will be a neutral implementation that will be usable as broadly as possible, i.e. not limited towards a specific use case or higher-level protocol.
* **Simple:** The most common uses of the UR should be very simple to execute (e.g. "give me the DDO or the Hub endpoint for this identifier").
* **Extensible:** The UR may ship by default with a few key implementations of common identifier systems, but must also be extensible to easily add new ones through a plugin mechanism.

The following functionality is out of scope for the UR:

* The UR will not address registration or modification of identifiers and associated data.
* The UR will not involve authentication or authorization of the UR user, i.e. all its functionality will be publicly usable by anyone. [debatable?]
* The UR will not implement any functionality that builds on top of resolution, such as the DIF Hub protocols, Blockstack storage layer, XDI, DID Auth, DKMS, etc. [debatable?], but it will be able to serve as a building block for such higher-level protocols.
