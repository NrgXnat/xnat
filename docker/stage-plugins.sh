#!/usr/bin/env bash
# Stage a curated set of XNAT plugins into docker/plugins/, which docker-compose bind-mounts
# read-only into the container's ${xnat.home}/plugins (/data/xnat/home/plugins). XNAT scans that
# dir once at boot, so after staging run `docker compose restart xnat` to load new/updated plugins.
#
# The set is defined in docker/plugins.manifest (see that file for the entry formats). Built jars
# are NOT committed (docker/plugins/ is git-ignored); this script (re)generates them, clearing any
# previously-staged jars first so a removed manifest entry drops out.
#
# Usage:
#   ./docker/stage-plugins.sh                # build `source` entries, then stage everything
#   ./docker/stage-plugins.sh --skip-build   # stage existing build/libs/*-xpl.jar without rebuilding
set -euo pipefail
cd "$(dirname "$0")/.."

REPO_ROOT="$(pwd)"
MANIFEST="docker/plugins.manifest"
DEST="docker/plugins"
SKIP_BUILD="${1:-}"

[ -f "$MANIFEST" ] || { echo "!! Missing $MANIFEST" >&2; exit 1; }

mkdir -p "$DEST"
rm -f "$DEST"/*.jar
echo ">> Staging plugins from $MANIFEST -> $DEST/"

abspath() { case "$1" in /*) printf '%s' "$1" ;; *) printf '%s/%s' "$REPO_ROOT" "$1" ;; esac; }

staged=0
while read -r kind arg ref || [ -n "${kind:-}" ]; do
    [[ -z "${kind// }" || "$kind" == \#* ]] && continue
    case "$kind" in
        source)
            dir="$(abspath "$arg")"
            [ -d "$dir" ] || { echo "!! source dir not found: $dir" >&2; exit 1; }
            if [ -n "${ref:-}" ] && \
               [ "$(git -C "$dir" rev-parse HEAD 2>/dev/null)" != "$(git -C "$dir" rev-parse "$ref" 2>/dev/null)" ]; then
                [ -z "$(git -C "$dir" status --porcelain)" ] || {
                    echo "!! $arg has uncommitted changes; cannot switch to pinned ref '$ref'. Commit/stash first." >&2; exit 1; }
                echo "   checking out pinned ref '$ref' in $arg ..."
                git -C "$dir" checkout -q "$ref"
            fi
            if [ "$SKIP_BUILD" != "--skip-build" ]; then
                echo "   building $arg${ref:+ @ $ref}  (gradlew xnatPluginJar) ..."
                ( cd "$dir" && ./gradlew xnatPluginJar -q )
            fi
            jar="$(ls -t "$dir"/build/libs/*-xpl.jar 2>/dev/null | head -1 || true)"
            [ -n "$jar" ] || { echo "!! no *-xpl.jar in $dir/build/libs (build it, or drop --skip-build)" >&2; exit 1; }
            ;;
        file)
            jar="$(abspath "$arg")"
            [ -f "$jar" ] || { echo "!! file not found: $jar" >&2; exit 1; }
            ;;
        coord)
            # group:artifact:version[:classifier] resolved from Maven Local. Remote download from a
            # published repo (GitHub Packages / Artifactory, with auth) is a future extension.
            IFS=':' read -r g a v c <<<"$arg"
            jar="$HOME/.m2/repository/${g//.//}/$a/$v/$a-$v${c:+-$c}.jar"
            [ -f "$jar" ] || { echo "!! not in Maven Local: $arg  ($jar)" >&2; exit 1; }
            ;;
        *)
            echo "!! unknown manifest entry: '$kind $arg'" >&2; exit 1 ;;
    esac
    cp "$jar" "$DEST/"
    echo "   staged $(basename "$jar")"
    staged=$((staged + 1))
done < "$MANIFEST"

echo ">> Staged $staged plugin(s):"
ls -1 "$DEST"/*.jar 2>/dev/null | sed 's#^#     #' || echo "     (none)"
echo ">> Next: docker compose restart xnat   (XNAT loads plugins at boot)"
