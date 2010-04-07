=========
xdat_core
=========

Building
========

Until we have our own maven repository, it is necessary to load any non-public jars into your local repository::

    mvn install:install-file -Dfile=lib/xnatsrb-1.jar -DgroupId=org.nrg.xnat -DartifactId=xnatsrb -Dversion=1 -Dpackaging=jar
    
To build the xdat_core module::

    mvn clean install