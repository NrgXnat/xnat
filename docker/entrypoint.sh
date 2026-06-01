#!/bin/bash
# Container entrypoint: optional timezone setup, then exec the actual
# Tomcat startup command passed in by Dockerfile ENTRYPOINT/CMD.
#
# Set timezone with:  docker run -e timezone=America/Chicago ...
#
# Ported from build_scripts_v2/xnat-docker-build/entrypoint.sh.

set -x

if [ -n "${timezone}" ]; then
    unlink /etc/localtime 2>/dev/null || rm -f /etc/localtime
    ln -s "/usr/share/zoneinfo/${timezone}" /etc/localtime
else
    echo "timezone environment variable is not set"
fi

exec "$@"
