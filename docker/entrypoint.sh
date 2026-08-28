#!/bin/bash
# Container entrypoint. The container runs as a non-root uid, so timezone is set
# via the standard TZ env var (read directly by glibc and the JVM) instead of the
# old root-only /etc/localtime symlink. Override with `docker run -e TZ=...`; the
# default (Etc/UTC) is set in the Dockerfile. The legacy lowercase `timezone` var
# is still honored for backward compatibility.
set -e

if [ -n "${timezone:-}" ]; then
    export TZ="${timezone}"
fi

exec "$@"
