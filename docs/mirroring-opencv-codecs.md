# Mirroring the OpenCV image codecs into Artifactory

XNAT resolves compressed DICOM pixel data through dcm4che 5's `ImageReaderFactory`, whose stock
configuration names the OpenCV-backed plugins for every JPEG-family transfer syntax:

```
# org/dcm4che3/imageio/codec/ImageReaderFactory.properties, in dcm4che-imageio-5.35.0.jar
1.2.840.10008.1.2.4.70 : jpeg-cv     : org.dcm4che3.opencv.NativeImageReader
1.2.840.10008.1.2.4.90 : jpeg2000-cv : org.dcm4che3.opencv.NativeImageReader
```

Those classes live in `dcm4che-imageio-opencv`, which is Java glue over a native OpenCV build. Without
them, anything routed through the factory fails with `No Reader for format: jpeg2000-cv registered`.

The native binaries are declared `<scope>provided</scope>` in Weasis's POM, so they are **never
resolved transitively** — the 238 KB `weasis-core-img` jar contains no binaries at all. They have to
be declared explicitly, per platform, which means whichever repository serves them must hold them.

## What is in Artifactory

All of it, as of the mirror run recorded below. `dcm4che-imageio-opencv` and the Weasis BOM were
already there — they arrive transitively through dcm4che's parent POM. `weasis-core-img` and the
per-platform natives were not, and are what `scripts/mirror-opencv-codecs.sh` deployed.

| Artifact | |
| --- | --- |
| `org.dcm4che:dcm4che-imageio-opencv:5.35.0` | was already present |
| `org.weasis.core:weasis-core-img-bom:5.0.0` | was already present |
| `org.weasis.core:weasis-core-img:5.0.0` | mirrored |
| `org.weasis.thirdparty.org.opencv:libopencv_java:5.0.0-dcm` | mirrored, four classifiers |

The versions are not free choices. `dcm4che-imageio-opencv` must match the dcm4che version, and the
Weasis library and its native must match what that dcm4che release pins: XNAT is on dcm4che 5.35.0,
`dcm4che-parent-5.35.0.pom` pins `weasis-core-img` 5.0.0, and that pins `libopencv_java`
5.0.0-dcm. The three move together or not at all; a mismatched native fails at load or, worse,
subtly.

5.35.0 specifically, not 5.33.1: everything before it reflects into `java.desktop` internals to
resolve a stream segment, which JDK 17+ denies. dcm4che logs the denial and returns null, and the
caller dereferences it -- so snapshot generation fails with "Cannot invoke
StreamSegment.getImageDescriptor() because seg is null" for every compressed study (XNAT-6581,
XNAT-6743, and dcm4che issue 1403). The documented workaround, `--add-opens
java.desktop/javax.imageio.stream=ALL-UNNAMED`, is worse than the bug: reflection then succeeds and
the native segfaults, taking the JVM with it. 5.35.0 rewrote the method to ask the stream for its
file directly, and needs no flag. Bumping dcm4che means re-running the
mirror script with the new coordinates.

Upstream is
`https://raw.githubusercontent.com/nroduit/mvn-repo/master`, which is already a declared repository in
`buildSrc/src/main/groovy/buildlogic.java-common-conventions.gradle`. Mirroring pins these to an
immutable coordinate we control rather than a branch-served path, which matters most for the native
library: it is unsigned (no `.asc` is published, only `.sha1`/`.md5`) and it executes inside the
production container.

## Mirroring

Requires `ARTIFACTORY_USER` and `ARTIFACTORY_TOKEN` with deploy rights on `libs-release-local`. The
script verifies every download against the checksum published beside it before uploading anything,
and sends both checksums with the `PUT` so Artifactory rejects a corrupted transfer.

`scripts/mirror-opencv-codecs.sh` does this. It runs as a dry run by default, downloading every
artifact and verifying it against the checksum published upstream without uploading anything:

```
$ scripts/mirror-opencv-codecs.sh
DRY RUN -- downloading and verifying only. Pass --publish to upload.

  weasis-core-img-5.0.0.pom                      ok        3007 bytes  sha256=4dbcf650...
  libopencv_java-5.0.0-dcm-linux-x86-64.so       ok    34664360 bytes  sha256=3552c806...
  ...
All artifacts verified. Re-run with --publish to upload.
```

Publishing needs deploy rights on `libs-release-local`:

```bash
ARTIFACTORY_USER=... ARTIFACTORY_TOKEN=... scripts/mirror-opencv-codecs.sh --publish
```

Artifacts already present are reported and skipped, and `libs-release-local` rejects redeploys with
409 rather than overwriting, so the script is safe to re-run after a partial failure.

Upstream publishes no Windows binary, despite Weasis's POM declaring `windows-x86-64` and
`windows-x86` dependencies. A Windows workstation cannot obtain this native from here.

For the record, SHA-256 of the artifacts that matter most, verified against the upstream `.sha1`:

```
3552c806744192c734f6cf492bf95a139cbfd6f800ff662688d18b6a199f92f1  libopencv_java-5.0.0-dcm-linux-x86-64.so
b07190e6ef6e233c2b7dc7f945c5b470be44450a49f9915aeaaa86f8799ef6a0  libopencv_java-5.0.0-dcm-linux-aarch64.so
f447743478afd7b8bf1fb66d5df22a46f50b0b811d490638aec83992cbfe423e  libopencv_java-5.0.0-dcm-macosx-aarch64.dylib
902e3993685d3909d1952944943bd56a62d6010f6d1fcc3c6a98dc178fbe930e  libopencv_java-5.0.0-dcm-macosx-x86-64.dylib
```

## Verifying the mirror

Re-run the script with no arguments. Everything should report `present already`.

Checking by hand needs one subtlety: Artifactory answers a **binary** download with a 302 redirect to
storage, so `curl` without `-L` reports a mirrored jar or native as absent while the small POMs,
served inline, look fine. Follow redirects:

```bash
curl -sI -L -o /dev/null -w '%{http_code}\n' \
  https://nrgxnat.jfrog.io/nrgxnat/libs-release/org/weasis/thirdparty/org/opencv/libopencv_java/5.0.0-dcm/libopencv_java-5.0.0-dcm-linux-x86-64.so
```

`libs-release` is the virtual repository the build reads from and it already includes
`libs-release-local`, so no repository declaration changes are needed. It also serves anonymously,
which is why the image build needs no credentials.

## Coverage

OpenCV supplies the JPEG family only:

| Transfer syntax | Decode | Encode |
| --- | --- | --- |
| JPEG Baseline / Extended / Lossless / Progressive | OpenCV | OpenCV |
| JPEG-LS | OpenCV | OpenCV |
| JPEG 2000, lossless and lossy | OpenCV | OpenCV |
| RLE Lossless | `dcm4che-imageio-rle` | **nothing** |

`dcm4che-imageio-rle` contains only an `ImageReaderSpi`, and `ImageWriterFactory.properties` has no
entry for `1.2.840.10008.1.2.5`. Nothing in the stack can produce RLE. A pixel redaction on an RLE
object therefore decodes and redacts correctly but is stored uncompressed, since preserving the
transfer syntax would require an encoder that does not exist.

## Using it from the build

Version catalogs cannot express classifiers, so the glue goes in `libs.versions.toml` and the natives
are declared inline. `dicom-edit6` already does this for its tests — see its `build.gradle` for a
working example of the platform-classifier selection and the staging task described below.

```toml
# gradle/libs.versions.toml
weasis-opencv = "5.0.0-dcm"

dcm4che5-dcm4che-imageio-opencv = { group = "org.dcm4che", name = "dcm4che-imageio-opencv", version.ref = "dcm4che5" }
```

```groovy
// xnat-web/build.gradle -- the Java glue, on the runtime classpath and so inside the WAR
implementation libs.dcm4che5.dcm4che.imageio.opencv
```

The native is not a classpath entry and cannot travel in the WAR, so the image fetches it. It is not
copied from the build context either: that context is assembled by the reusable CI workflow
(`NrgXnat/xnat-ci-workflows`) and carries only `xnat.war`. `libs-release` serves anonymously, so the
image build needs no Artifactory credentials.

`/usr/java/packages/lib` is already on the JVM's default `java.library.path` on Linux, so nothing
needs `-Djava.library.path` and **no Helm chart change is required**.

```dockerfile
# Dockerfile -- pinned by checksum, per architecture, failing the build if either is wrong
ARG TARGETARCH
RUN set -eu; \
    case "${TARGETARCH}" in \
        amd64) classifier=linux-x86-64;  sha256=3552c806... ;; \
        arm64) classifier=linux-aarch64; sha256=b07190e6... ;; \
        *) echo "No OpenCV native published for TARGETARCH=${TARGETARCH}" >&2; exit 1 ;; \
    esac; \
    curl -fsSL -o /usr/java/packages/lib/libopencv_java.so "${OPENCV_BASE}/...-${classifier}.so"; \
    echo "${sha256}  /usr/java/packages/lib/libopencv_java.so" | sha256sum -c -
```

Pass `--build-arg INSTALL_OPENCV=false` for 1.9.x images, which are on dcm4che 2 and would otherwise
carry ~19 MB they never load.

Tests that decode compressed pixel data need the native too, resolved for whichever machine runs the
build rather than for the image:

```groovy
// dicom-edit6/build.gradle
tasks.register('stageOpenCvNative', Copy) {
    from configurations.opencvNative
    into layout.buildDirectory.dir('opencv-native')
    rename '.*', openCv.fileName   // System.loadLibrary wants libopencv_java.so, not the
}                                  // resolved name, which carries version and classifier

test {
    dependsOn tasks.named('stageOpenCvNative')
    systemProperty 'java.library.path', layout.buildDirectory.dir('opencv-native').get().asFile.absolutePath
}
```

## Deploying the WAR without the image

Sites that drop `xnat.war` into their own Tomcat get the Java glue, because it travels in the WAR,
but not the native, because the Dockerfile is what installs that. Install it by hand or compressed
pixel data cannot be decoded at all:

```bash
curl -fsSLo /usr/java/packages/lib/libopencv_java.so \
  https://nrgxnat.jfrog.io/nrgxnat/libs-release/org/weasis/thirdparty/org/opencv/libopencv_java/5.0.0-dcm/libopencv_java-5.0.0-dcm-linux-x86-64.so
# verify against the SHA-256 recorded above, then restart Tomcat
```

`/usr/java/packages/lib` is on the JVM's default `java.library.path` on Linux. Somewhere else works
too, as long as `CATALINA_OPTS` carries `-Djava.library.path=/that/directory`.

Without it, anything that has to decode compressed pixel data fails: snapshots and thumbnails of
compressed studies, and `alterPixels` over a compressed object, which fails the import rather than
letting an unredacted object through. Uncompressed and RLE Lossless objects need no native and are
unaffected, which is most archives.

**This is not a new class of requirement, but it does move.** Before this change, compressed
snapshots were served by dcm4che 2's DICOM reader, and its codec table names JAI's native-backed
readers:

```
1.2.840.10008.1.2.4.90=jpeg2000,com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLib
1.2.840.10008.1.2.4.57=jpeg,com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
```

Those need `libclib_jiio` and its `clibwrapper_jiio.jar`, neither of which XNAT has ever shipped, so
a JPEG 2000 snapshot failed with `No Image Reader of class ...J2KImageReaderCodecLib available for
format:jpeg2000` -- which is XNAT-6581 and XNAT-6743. The requirement changes from a native nobody
could satisfy to one the image installs.

The exception is **JPEG Baseline**, which dcm4che 2 served through the JDK's own reader with no
native at all. On a deployment without `libopencv_java.so`, baseline JPEG goes from working to
failing. That is the one case where installing the native is a genuine new obligation rather than a
replacement for an unmet one.

## Checking that it worked

With the native loadable, these format names resolve; without it they are absent:

```java
ImageIO.getImageReadersByFormatName("jpeg2000-cv")   // -> NativeImageReader
```

End to end, `:dicom-edit6:test` exercises the codecs: `StreamingRectanglePixelEditHandlerTest`
round-trips a JPEG 2000 Lossless object through decode, redaction and re-encode with its transfer
syntax intact, and stores a lossy JPEG object uncompressed with its compression history recorded.
Those cases fail with `No Reader for format: jpeg2000-cv registered` if the native is not loadable,
which makes them a serviceable check that the staging works.
