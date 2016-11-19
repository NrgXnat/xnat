NRG Test Framework
================================

The NRG test framework provides the base functionality and implementation for
unit and integration testing functions in the NRG/XNAT libraries.


Building
--------

To build the NRG test framework, invoke Maven with the desired lifecycle phase.
For example, the following command cleans previous builds, builds a new jar file, 
creates archives containing the source code and JavaDocs for the library, runs the 
library's unit tests, and installs the jar into the local repository:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
mvn clean install
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
