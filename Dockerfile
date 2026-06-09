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

# Container-aware heap sizing. Percentages refer to the cgroup limit;
# they're harmless under plain `docker run` as well (default 75% MaxRAM).
ENV CATALINA_OPTS="-XX:+UseContainerSupport \
    -XX:InitialRAMPercentage=${XNAT_INIT_HEAP} \
    -XX:MinRAMPercentage=${XNAT_MIN_HEAP} \
    -XX:MaxRAMPercentage=${XNAT_MAX_HEAP} \
    -Dxnat.home=${XNAT_HOME}"

ENV XNAT_HOME=${XNAT_HOME} \
    XNAT_DATASOURCE_USERNAME=${XNAT_DATASOURCE_USERNAME} \
    PGPASSWORD=${XNAT_DATASOURCE_PASSWORD}

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
RUN unzip -o -d ${TOMCAT_XNAT_FOLDER_PATH} /tmp/ROOT.war && rm /tmp/ROOT.war

VOLUME ["/data/xnat"]
EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/entrypoint.sh", "/usr/local/tomcat/bin/catalina.sh", "run"]
