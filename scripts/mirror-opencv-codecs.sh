#!/usr/bin/env bash
#
# Mirror the Weasis OpenCV codecs into nrgxnat/libs-release-local.
#
# dcm4che 5 resolves compressed DICOM to the OpenCV-backed ImageIO plugins, which reach XNAT through
# org.dcm4che:dcm4che-imageio-opencv. That artifact is already in Artifactory; the Weasis library
# behind it and the per-platform native binaries are not. See docs/mirroring-opencv-codecs.md.
#
# Dry run (default) downloads every artifact, verifies it against the checksum published upstream,
# and reports what would be deployed. Nothing leaves the machine.
#
#     scripts/mirror-opencv-codecs.sh
#
# Publishing additionally uploads, and needs deploy rights on libs-release-local:
#
#     ARTIFACTORY_USER=you scripts/mirror-opencv-codecs.sh --publish
#
# The token is prompted for, silently, if it is not already in the environment. Prefer that to
# passing it inline: an argument on a command line is visible to `ps` while the script runs, and is
# kept in shell history afterwards.
#
# libs-release-local rejects redeploys with 409 rather than overwriting, so re-running is safe: an
# artifact already present is reported and skipped.

set -euo pipefail

SRC="https://raw.githubusercontent.com/nroduit/mvn-repo/master"
DST="https://nrgxnat.jfrog.io/nrgxnat/libs-release-local"
READ="https://nrgxnat.jfrog.io/nrgxnat/libs-release"

WEASIS="org/weasis/core/weasis-core-img/4.9.0.1"
OPENCV="org/weasis/thirdparty/org/opencv/libopencv_java/4.9.0-dcm"

# weasis-core-img-bom is deliberately absent: it already resolves from Artifactory.
#
# These are every platform upstream actually publishes, less linux-armv7a. Note that Weasis's POM
# declares windows-x86-64 and windows-x86 too, but no Windows binary exists in the repository --
# a Windows workstation cannot get this native from here at all.
#
# Upstream also carries "-dyn" variants of each Linux native, dynamically linked and smaller. The
# POM names the statically linked ones, so those are what resolve; do not substitute them.
ARTIFACTS=(
  "$WEASIS/weasis-core-img-4.9.0.1.pom"
  "$WEASIS/weasis-core-img-4.9.0.1.jar"
  "$OPENCV/libopencv_java-4.9.0-dcm.pom"
  "$OPENCV/libopencv_java-4.9.0-dcm-linux-x86-64.so"      # what ships in the image
  "$OPENCV/libopencv_java-4.9.0-dcm-linux-aarch64.so"     # arm64 nodes and CI
  "$OPENCV/libopencv_java-4.9.0-dcm-macosx-aarch64.dylib" # Apple silicon development
  "$OPENCV/libopencv_java-4.9.0-dcm-macosx-x86-64.dylib"  # Intel Mac development
)

publish=false
[ "${1:-}" = "--publish" ] && publish=true

if $publish; then
  : "${ARTIFACTORY_USER:?--publish needs ARTIFACTORY_USER}"
  if [ -z "${ARTIFACTORY_TOKEN:-}" ]; then
    if [ -t 0 ]; then
      read -rs -p "Artifactory token for $ARTIFACTORY_USER: " ARTIFACTORY_TOKEN
      echo
    else
      echo "No ARTIFACTORY_TOKEN, and no terminal to prompt on." >&2
      echo "Run this from an interactive shell, or export the token first." >&2
      exit 1
    fi
  fi
  # curl reads it from the environment rather than from argv, keeping it out of the process table.
  export ARTIFACTORY_TOKEN
else
  echo "DRY RUN -- downloading and verifying only. Pass --publish to upload."
fi
echo

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
failed=0

for path in "${ARTIFACTS[@]}"; do
  name="$(basename "$path")"

  # Already mirrored? Nothing to do, and a PUT would only earn a 409. A 404 here is the normal
  # case before the first run, so this must not be noisy about it.
  #
  # -I -L rather than a bare GET: Artifactory answers a binary download with a 302 to storage, so
  # without following redirects every already-mirrored jar and native reads as absent, while the
  # small POMs served inline read as present.
  if [ "$(curl -sI -L -o /dev/null -w '%{http_code}' "$READ/$path")" = "200" ]; then
    printf '  %-46s present already\n' "$name"
    continue
  fi

  file="$work/$name"
  if ! curl -fsSL --retry 3 -o "$file" "$SRC/$path"; then
    printf '  %-46s DOWNLOAD FAILED\n' "$name"; failed=1; continue
  fi

  # The checksum published beside the artifact is the only provenance on offer -- upstream
  # publishes no signatures.
  want="$(curl -fsSL "$SRC/$path.sha1" | tr -d '[:space:]')"
  got="$(shasum -a 1 "$file" | cut -d' ' -f1)"
  if [ "$want" != "$got" ]; then
    printf '  %-46s SHA-1 MISMATCH want=%s got=%s\n' "$name" "$want" "$got"; failed=1; continue
  fi
  sha256="$(shasum -a 256 "$file" | cut -d' ' -f1)"
  bytes="$(wc -c < "$file" | tr -d ' ')"

  if ! $publish; then
    printf '  %-46s ok  %10s bytes  sha256=%s\n' "$name" "$bytes" "$sha256"
    continue
  fi

  # Credentials go in on stdin as a curl config rather than as -u, which would put the token in
  # argv where `ps` can read it. printf is a shell builtin, so it is not a process either.
  code="$(printf 'user = "%s:%s"\n' "$ARTIFACTORY_USER" "$ARTIFACTORY_TOKEN" \
        | curl -sS -o /dev/null -w '%{http_code}' --config - \
               -H "X-Checksum-Sha1: $got" -H "X-Checksum-Sha256: $sha256" \
               -X PUT -T "$file" "$DST/$path")"
  case "$code" in
    200|201) printf '  %-46s deployed  sha256=%s\n' "$name" "$sha256" ;;
    409)     printf '  %-46s present already\n' "$name" ;;
    401|403) printf '  %-46s DENIED (HTTP %s) -- check deploy rights\n' "$name" "$code"; failed=1 ;;
    *)       printf '  %-46s FAILED (HTTP %s)\n' "$name" "$code"; failed=1 ;;
  esac
done

echo
if [ "$failed" -ne 0 ]; then
  echo "One or more artifacts failed. Nothing partial was left behind that a re-run cannot fix:"
  echo "libs-release-local rejects redeploys, so re-running skips whatever already landed."
  exit 1
fi
$publish && echo "Mirror complete. Verify with: scripts/mirror-opencv-codecs.sh" \
         || echo "All artifacts verified. Re-run with --publish to upload."
