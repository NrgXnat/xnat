NRG Parent
==========

The NRG parent project is a master reference for other libraries in the NRG
library family. It comprises primarily metadata used when building downstream
dependencies. This includes:

-   Values for common properties like source encoding and Java source and target
    versions

-   Versions of dependencies and plugins

-   Build configurations for Maven plugins

This allows for easily managing the build environment across the set of NRG
libraries, as well as determining the version matrix required for consumers of
the NRG libraries.

Building
--------

To build the NRG parent project, invoke Maven with the desired lifecycle phase.
For example, the following command cleans previous builds, builds a new jar file, 
creates archives containing the source code and JavaDocs for the library, runs the 
library's unit tests, and installs the jar into the local repository:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
mvn clean install
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~