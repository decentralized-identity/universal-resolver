# Universal Resolver - Architecture and Drivers

## Architecture

The Universal Resolver's architecture consists of the following:

* Abstract interface definition of resolution / discovery operations.
* Extensible mechanism for "drivers".
* A base set of drivers we want to support initially.
* Shared components that will be useful across multiple drivers.

![System Architecture](/docs/figures/architecture.png)

The Universal Resolver's main task is to provide an API wrapper around one or
more "drivers".  It does so by running a client for each system in
a colocated container or VM (bold boxes).  The "Service Orchestrator" is
responsible for spawning the required naming system clients, feeding them their
configuration data, and forwarding HTTP requests to each of them (see below).

### Drivers

Drivers come in two flavors:  a lightweight driver, and a full
node driver.  The difference between the two is that a full node will synchronize its
identifier state with a blockchain or distributed ledger, and host all the requisite
state (e.g. the chain state) and run all the requisite software (e.g. blockchain
peers) locally.  **This is a resource-intensive configuration, and is meant
primarily for dedicated servers.**

A lightweight driver will instead contact a full node running on an external host.
The details as to which host to contact (and how to contact it) will be fed to the lightweight driver's
container by the service orchestrator upon instantiation.  This configuration is
meant for devices that cannot reliably synchronize state with the naming
service, due to e.g. resource constraints and offline operating modes (such as
laptops and mobile phones).

Note that a driver may internally fulfill resolution in different ways, e.g.:

* The plugin for BNS may be able to access a local full Bitcoin/Virtualchain node, or it may itself call a remote API.
* The plugin for Sovrin DIDs may dynamically construct a DDO rather than retrieving an actual DDO file from the Sovrin Ledger.

The interface to both lightweight and full node drivers is identical.
