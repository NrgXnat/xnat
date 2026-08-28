#!/bin/sh
# Generate a default xnat-conf.properties at image build time.
#
# Build-arg defaults from Dockerfile (XNAT_DATASOURCE_*, XNAT_ACTIVEMQ)
# are folded into the file. At run-time, these can be overridden by
# the deploying orchestrator either by:
#   (a) mounting a replacement xnat-conf.properties over the one we
#       seed here, or
#   (b) re-running this script with different env vars before catalina
#       starts (the entrypoint does not do this today — would need a
#       small wrapper if production wants that flow).
#
# Ported from build_scripts_v2/xnat-docker-build/make-xnat-config.sh.

set -e

if [ ! -f "$XNAT_HOME/config/xnat-conf.properties" ]; then
    cat > "$XNAT_HOME/config/xnat-conf.properties" << EOF
datasource.driver=$XNAT_DATASOURCE_DRIVER
datasource.url=$XNAT_DATASOURCE_URL
datasource.username=$XNAT_DATASOURCE_USERNAME
datasource.password=$XNAT_DATASOURCE_PASSWORD

hibernate.dialect=org.hibernate.dialect.PostgreSQL9Dialect
hibernate.hbm2ddl.auto=update
hibernate.show_sql=false
hibernate.cache.use_second_level_cache=true
hibernate.cache.use_query_cache=true

spring.activemq.broker-url=tcp://$XNAT_ACTIVEMQ:61616?wireFormat.maxInactivityDuration=0
spring.activemq.user=admin
spring.activemq.password=admin
xnat.is_primary_node=true

spring.http.multipart.max-file-size=1073741824
spring.http.multipart.max-request-size=1073741824
EOF
fi

# Stash a copy of the config dir contents under /usr/local/share/xnat so
# diagnostic tooling has a known location to read the defaults from.
mkdir -p /usr/local/share/xnat
find "$XNAT_HOME/config" -mindepth 1 -maxdepth 1 -type f -exec cp {} /usr/local/share/xnat \;
