#!/bin/bash
# Container entrypoint. The container runs as a non-root uid, so timezone is set
# via the standard TZ env var (read directly by glibc and the JVM) instead of the
# old root-only /etc/localtime symlink. Override with `docker run -e TZ=...`; the
# default (Etc/UTC) is set in the Dockerfile. The legacy lowercase `timezone` var
# is still honored for backward compatibility.
#
# XNAT boots UNINITIALIZED on an empty DB (XnatInitCheckFilter gates every non-setup
# request behind the setup wizard), the same as the develop branch — the entrypoint
# does NOT auto-init. Complete first-install setup + config with docker/init-xnat.sh,
# then populate the data fixture with docker/build-known-state.sh.
set -e

if [ -n "${timezone:-}" ]; then
    export TZ="${timezone}"
fi

exec "$@"
