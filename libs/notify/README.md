NRG Notification Service
================================

The NRG notification service manages the multiple entities required for a flexible
and powerful notification service. This includes:

-   Subscribers (can be users or system entities)
-   Notification categories and definitions
-   Subscription (associates a subscriber with a notification definition and one or more notification channels)
-   Notifications

Building
--------

To build the NRG notification service, invoke Maven with the desired lifecycle phase.
For example, the following command cleans previous builds, builds a new jar file, 
creates archives containing the source code and JavaDocs for the library, runs the 
library's unit tests, and installs the jar into the local repository:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
mvn clean install
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
