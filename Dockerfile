# XNAT runtime image
# Built by .github/workflows/build-publish.yml. Expects:
#   ./docker-context/xnat.war       — WAR with logback already rewritten to
#                                     ConsoleAppender by the workflow.
#   ./docker/entrypoint.sh          — runtime helper (timezone handling).
#   ./docker/make-xnat-config.sh    — generates default xnat-conf.properties.

# Base image is parameterised so the same Dockerfile can serve both
# 1.10.x (java 21 / tomcat:9-jdk21-temurin) and 1.9.x (java 8 /
# tomcat:9.0.93-jdk8). Override at build time:
#   docker build --build-arg TOMCAT_BASE=tomcat:9.0.93-jdk8 ...
ARG TOMCAT_BASE=tomcat:9-jdk21-temurin
FROM ${TOMCAT_BASE}

# -----------------------------------------------------------------------------
# Build-time arguments. All have safe DEFAULT values intended for dev only;
# any production deployment MUST override the *_PASSWORD / *_USERNAME / *_URL
# entries at runtime via `docker run -e ...` or the orchestrator's secrets.
# -----------------------------------------------------------------------------
ARG XNAT_ROOT=/data/xnat
ARG XNAT_HOME=/data/xnat/home
ARG XNAT_DATASOURCE_DRIVER=org.postgresql.Driver
ARG XNAT_DATASOURCE_URL=jdbc:postgresql://xnat-postgresql/xnat
ARG XNAT_DATASOURCE_USERNAME=xnat
ARG XNAT_DATASOURCE_PASSWORD=xnat
ARG XNAT_SMTP_ENABLED=false
ARG TOMCAT_XNAT_FOLDER=ROOT
ARG TOMCAT_XNAT_FOLDER_PATH=${CATALINA_HOME}/webapps/${TOMCAT_XNAT_FOLDER}
ARG XNAT_MIN_HEAP=10.0
ARG XNAT_INIT_HEAP=20.0
ARG XNAT_MAX_HEAP=66.0
ARG XNAT_ACTIVEMQ=xnat-activemq

# Non-root runtime uid:gid. 1000:1000 = the helm chart's `tomcat` user/group,
# matching its securityContext runAsUser: 1000 / fsGroup: 1000, so the image is
# a drop-in for the chart with no runAsUser/runAsGroup override needed.
ARG XNAT_UID=1000
ARG XNAT_GID=1000

# Container-aware heap sizing. Percentages refer to the cgroup limit;
# they're harmless under plain `docker run` as well (default 75% MaxRAM).
ENV CATALINA_OPTS="-XX:+UseContainerSupport \
    -XX:InitialRAMPercentage=${XNAT_INIT_HEAP} \
    -XX:MinRAMPercentage=${XNAT_MIN_HEAP} \
    -XX:MaxRAMPercentage=${XNAT_MAX_HEAP} \
    -Dxnat.home=${XNAT_HOME}"

ENV XNAT_HOME=${XNAT_HOME} \
    XNAT_DATASOURCE_USERNAME=${XNAT_DATASOURCE_USERNAME} \
    PGPASSWORD=${XNAT_DATASOURCE_PASSWORD} \
    TZ=Etc/UTC

# -----------------------------------------------------------------------------
# Helper scripts. Copied from ./docker/ in the build context.
# -----------------------------------------------------------------------------
COPY docker/make-xnat-config.sh /usr/local/bin/make-xnat-config.sh
COPY docker/entrypoint.sh       /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/*.sh

# Diagnostic / runtime tools expected by deploy environments
# (dcmtk for DICOM debug, netcat/ping for connectivity checks, unzip
# for the WAR expansion below).
RUN apt-get update && apt-get install -y --no-install-recommends \
        unzip \
        netcat-traditional \
        iputils-ping \
        net-tools \
        traceroute \
        dcmtk \
    && rm -rf /var/lib/apt/lists/*

# XNAT directory layout — pre-created so a fresh container has somewhere
# to write before any volume is mounted. Most of these are intended to
# be replaced by volume mounts in production.
RUN rm -rf ${CATALINA_HOME}/webapps/* && mkdir -p \
        ${TOMCAT_XNAT_FOLDER_PATH} \
        ${XNAT_HOME}/config \
        ${XNAT_HOME}/logs \
        ${XNAT_HOME}/plugins \
        ${XNAT_HOME}/work \
        ${XNAT_ROOT}/archive \
        ${XNAT_ROOT}/build \
        ${XNAT_ROOT}/cache \
        ${XNAT_ROOT}/ftp \
        ${XNAT_ROOT}/pipeline \
        ${XNAT_ROOT}/prearchive

# Seed default xnat-conf.properties (driver / url / hibernate / activemq /
# multipart limits). Run-time env overrides via the entrypoint.
RUN /usr/local/bin/make-xnat-config.sh

# Expand the WAR into webapps/ROOT/. The workflow has already
# rewritten WEB-INF/classes/logback.xml to ConsoleAppender via
# scripts/edit-log.py, so no further log config patching needed here.
COPY docker-context/xnat.war /tmp/ROOT.war
# Explode the WAR and, in the SAME layer, chown + group-write (g=u) the exploded
# webapp to the non-root uid, so the recursive chown doesn't force a second
# copy-up of the whole webapp into a later layer.
RUN unzip -o -d ${TOMCAT_XNAT_FOLDER_PATH} /tmp/ROOT.war && rm /tmp/ROOT.war \
 && chown -R ${XNAT_UID}:${XNAT_GID} ${CATALINA_HOME}/webapps \
 && chmod -R g=u ${CATALINA_HOME}/webapps

# Non-root: own the remaining writable paths as ${XNAT_UID}:${XNAT_GID},
# group-writable (g=u) to match the chart's runAsUser: 1000 / fsGroup: 1000.
# ${XNAT_ROOT} (=/data/xnat) covers ${XNAT_HOME} and the data dirs beneath it;
# ${CATALINA_HOME}/{conf,work,temp,logs} let Tomcat write on a standalone run.
# Runs before VOLUME so the seeded /data/xnat ownership sticks.
RUN chown -R ${XNAT_UID}:${XNAT_GID} ${XNAT_ROOT} ${CATALINA_HOME}/conf ${CATALINA_HOME}/work ${CATALINA_HOME}/temp ${CATALINA_HOME}/logs \
 && chmod -R g=u ${XNAT_ROOT} ${CATALINA_HOME}/conf ${CATALINA_HOME}/work ${CATALINA_HOME}/temp ${CATALINA_HOME}/logs

VOLUME ["/data/xnat"]
EXPOSE 8080

USER ${XNAT_UID}:${XNAT_GID}
ENTRYPOINT ["/usr/local/bin/entrypoint.sh", "/usr/local/tomcat/bin/catalina.sh", "run"]
