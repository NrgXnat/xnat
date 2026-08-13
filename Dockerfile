# XNAT runtime image
# Built by .github/workflows/build-publish.yml. Expects:
#   ./docker-context/xnat.war       — the stock build WAR (unmodified; byte-identical
#                                     to the JFrog artifact). Console logging is opt-in
#                                     at runtime via XNAT_LOG_CONSOLE (see XNAT-8782).
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
        curl \
        ca-certificates \
        netcat-traditional \
        iputils-ping \
        net-tools \
        traceroute \
        dcmtk \
    && rm -rf /var/lib/apt/lists/*

# -----------------------------------------------------------------------------
# OpenCV native for dcm4che's image codecs.
#
# dcm4che 5 resolves every JPEG-family transfer syntax to the OpenCV-backed ImageIO plugins in
# org.dcm4che:dcm4che-imageio-opencv, which is Java glue over this binary. Without it, anything
# decoding compressed pixel data -- snapshots, thumbnails, montages, and redaction by the
# alterPixels anonymization function -- fails with "No Reader for format: jpeg2000-cv registered".
#
# It is fetched rather than copied from the build context because the context is assembled by the
# reusable CI workflow and carries only the WAR. /usr/java/packages/lib is already on the JVM's
# default java.library.path on Linux, so nothing needs -Djava.library.path.
#
# Set INSTALL_OPENCV=false for 1.9.x builds, which are on dcm4che 2 and would carry ~19 MB unused.
# See docs/mirroring-opencv-codecs.md.
# -----------------------------------------------------------------------------
ARG INSTALL_OPENCV=true
# No default: BuildKit populates TARGETARCH with the architecture actually being built for, and a
# default here would mask it -- an arm64 build would silently install the amd64 native. Empty (a
# pre-BuildKit builder) falls through to the error case, which is the right answer.
ARG TARGETARCH
ARG OPENCV_VERSION=4.9.0-dcm
ARG OPENCV_BASE=https://nrgxnat.jfrog.io/nrgxnat/libs-release/org/weasis/thirdparty/org/opencv/libopencv_java/${OPENCV_VERSION}
RUN set -eu; \
    if [ "${INSTALL_OPENCV}" != "true" ]; then \
        echo "INSTALL_OPENCV=${INSTALL_OPENCV}, skipping the OpenCV native"; \
    else \
        case "${TARGETARCH}" in \
            amd64) classifier=linux-x86-64;  sha256=41d81dfe284b448f38a1420f711c30b53f3a42035067059fcc9449d1b2cadca3 ;; \
            arm64) classifier=linux-aarch64; sha256=5dd0d8fd94d97504cad8d6eeef26c23cc1bbb975732467bce793f6b5959077b3 ;; \
            *) echo "No OpenCV native published for TARGETARCH=${TARGETARCH}" >&2; exit 1 ;; \
        esac; \
        mkdir -p /usr/java/packages/lib; \
        curl -fsSL --retry 3 -o /usr/java/packages/lib/libopencv_java.so \
             "${OPENCV_BASE}/libopencv_java-${OPENCV_VERSION}-${classifier}.so"; \
        echo "${sha256}  /usr/java/packages/lib/libopencv_java.so" | sha256sum -c -; \
    fi

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

# Expand the WAR into webapps/ROOT/. The WAR ships stock (no logback surgery):
# console logging is opt-in at runtime via XNAT_LOG_CONSOLE (XNAT-8782), so no
# log-config patching happens here or at build time.
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
