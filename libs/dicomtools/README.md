NRG DICOM Tools
================================

The NRG DICOM Tools library provides tools for manipulating and managing DICOM data files. Note that this
is different from many of the NRG DICOM libraries that work with DICOM objects directly. While the DICOM
tools may access DICOM header data and other DICOM-specific metadata to perform their tasks, they are primarily
intended to help abstract and manage the files that constitute DICOM studies.

Building
--------

To build the NRG DICOM tools library, invoke Maven with the desired lifecycle phase.
For example, the following command cleans previous builds, builds a new jar file, 
creates archives containing the source code and JavaDocs for the library, runs the 
library's unit tests, and installs the jar into the local repository:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
mvn clean install
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
