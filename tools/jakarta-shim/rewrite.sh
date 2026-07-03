#!/usr/bin/env bash
#
# Regenerates the jakarta-shimmed JARs in libs/ from the original javax artifacts using the Apache Tomcat
# jakartaee-migration tool. The shimmed JARs are byte-code rewrites (javax.* EE namespaces -> jakarta.*) of
# legacy libraries that have no upstream Jakarta release: Restlet 1.1.10 (x4), Turbine 2.3.3, Velocity 1.7,
# velocity-tools 2.0.
#
# Normally you never need to run this: libs/ is committed. Re-run only to bump the migration tool or to
# regenerate from scratch, then run verify.sh.
#
# Usage: ./rewrite.sh [work-dir]      (default: ./work)

set -euo pipefail
cd "$(dirname "$0")"

MIGRATION_VERSION="${MIGRATION_VERSION:-1.0.8}"
WORK="${1:-./work}"
IN="$WORK/in"
TOOL="$WORK/jakartaee-migration-${MIGRATION_VERSION}-shaded.jar"
OUT="./libs"

mkdir -p "$IN"

# Original artifacts (coordinates match gradle/libs.versions.toml).
ARTIFACTS=(
    "org/restlet/org.restlet/1.1.10/org.restlet-1.1.10.jar"
    "com/noelios/restlet/com.noelios.restlet/1.1.10/com.noelios.restlet-1.1.10.jar"
    "com/noelios/restlet/com.noelios.restlet.ext.servlet/1.1.10/com.noelios.restlet.ext.servlet-1.1.10.jar"
    "org/restlet/org.restlet.ext.fileupload/1.1.10/org.restlet.ext.fileupload-1.1.10.jar"
    "turbine/turbine/2.3.3/turbine-2.3.3.jar"
    "org/apache/velocity/velocity/1.7/velocity-1.7.jar"
    "org/apache/velocity/velocity-tools/2.0/velocity-tools-2.0.jar"
)

echo "==> Fetching jakartaee-migration ${MIGRATION_VERSION}"
[ -f "$TOOL" ] || curl -fSL -o "$TOOL" \
    "https://repo1.maven.org/maven2/org/apache/tomcat/jakartaee-migration/${MIGRATION_VERSION}/jakartaee-migration-${MIGRATION_VERSION}-shaded.jar"

echo "==> Fetching original javax artifacts"
for path in "${ARTIFACTS[@]}"; do
    jar="$(basename "$path")"
    if [ ! -f "$IN/$jar" ]; then
        # Prefer the local Gradle cache; fall back to Maven Central / XNAT Artifactory.
        cached="$(find ~/.gradle/caches/modules-2/files-2.1 -name "$jar" 2>/dev/null | head -1 || true)"
        if [ -n "$cached" ]; then
            cp "$cached" "$IN/$jar"
        else
            curl -fSL -o "$IN/$jar" "https://repo1.maven.org/maven2/$path" \
                || curl -fSL -o "$IN/$jar" "https://nrgxnat.jfrog.io/nrgxnat/libs-release/$path"
        fi
    fi
done

echo "==> Rewriting (profile=EE)"
mkdir -p "$OUT"
for jar in "$IN"/*.jar; do
    name="$(basename "$jar" .jar)"
    java -jar "$TOOL" -profile=EE "$jar" "$OUT/${name}-jakarta.jar"
    echo "    $name -> ${name}-jakarta.jar"
done

echo "==> Done. Now run ./verify.sh"
