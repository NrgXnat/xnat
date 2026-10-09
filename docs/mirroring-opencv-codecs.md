# Mirroring the OpenCV image codecs into Artifactory

dcm4che 5 decodes and encodes JPEG-family pixel data (JPEG, JPEG-LS and JPEG 2000) through OpenCV.
Its `ImageReaderFactory` maps those transfer syntaxes to the readers in `dcm4che-imageio-opencv`:

```
# org/dcm4che3/imageio/codec/ImageReaderFactory.properties, in dcm4che-imageio-5.35.0.jar
1.2.840.10008.1.2.4.70 : jpeg-cv     : org.dcm4che3.opencv.NativeImageReader
1.2.840.10008.1.2.4.90 : jpeg2000-cv : org.dcm4che3.opencv.NativeImageReader
```

`dcm4che-imageio-opencv` is a Java layer over a native OpenCV library built by Weasis. If the jar
is missing, decoding fails with `No Reader for format: jpeg2000-cv registered`. If the native
library is missing, it fails with
`UnsatisfiedLinkError: 'long org.opencv.core.Mat.n_Mat(int, int, int)'`.

Weasis's POM declares the native libraries with `provided` scope, so they are never resolved
transitively, and the `weasis-core-img` jar contains no native code. Anything that needs a native
library has to declare it for each platform, and the repository it resolves from has to have it.

## Versions

The versions have to match. `dcm4che-imageio-opencv` uses the dcm4che version, 5.35.0.
`dcm4che-parent-5.35.0.pom` pins `weasis-core-img` 5.0.0, which pins the native library at
5.0.0-dcm. A native library that doesn't match the Java code fails to load or misbehaves. When you
upgrade dcm4che, check the new pins and re-run the mirror script with the new coordinates.

Java 17 and later need dcm4che 5.35.0 or later. Earlier versions of `dcm4che-imageio-opencv`
reflect into `java.desktop` internals, which Java 17 denies. dcm4che logs the denial, and decoding
then fails with a `NullPointerException` ending `because "seg" is null` (dcm4che issue 1403). Don't
work around it with `--add-opens java.desktop/javax.imageio.stream=ALL-UNNAMED`: the reflection then
succeeds, and the native library crashes the JVM.

## What is in Artifactory

| Artifact | |
| --- | --- |
| `org.dcm4che:dcm4che-imageio-opencv:5.35.0` | already present, through dcm4che's parent POM |
| `org.weasis.core:weasis-core-img-bom:5.0.0` | already present, through dcm4che's parent POM |
| `org.weasis.core:weasis-core-img:5.0.0` | mirrored |
| `org.weasis.thirdparty.org.opencv:libopencv_java:5.0.0-dcm` | mirrored, four classifiers |
| `org.weasis.thirdparty.org.opencv:opencv_java:5.0.0-dcm` | mirrored, `windows-x86-64` |

Upstream is `https://raw.githubusercontent.com/nroduit/mvn-repo/master`, which the build also
declares as a repository, in
`buildSrc/src/main/groovy/buildlogic.java-common-conventions.gradle`. Mirroring gives the build and
the image a fixed copy we control instead of a file served from a GitHub branch. That matters most
for the native libraries, which run inside XNAT and are unsigned: upstream publishes only `.sha1`
and `.md5` checksums for them.

## Mirroring

`scripts/mirror-opencv-codecs.sh` downloads each artifact from upstream and checks it against the
`.sha1` published next to it. It then uploads it to `libs-release-local` with its SHA-1 and SHA-256,
which Artifactory checks on receipt. Artifacts already in Artifactory are skipped.

By default the script is a dry run and uploads nothing:

```
$ scripts/mirror-opencv-codecs.sh
DRY RUN -- downloading and verifying only. Pass --publish to upload.

  weasis-core-img-5.0.0.pom                      ok        3007 bytes  sha256=4dbcf650...
  libopencv_java-5.0.0-dcm-linux-x86-64.so       ok    34664360 bytes  sha256=3552c806...
  ...
All artifacts verified. Re-run with --publish to upload.
```

To upload, you need deploy rights on `libs-release-local`. The script prompts for your token if
`ARTIFACTORY_TOKEN` isn't set:

```bash
ARTIFACTORY_USER=you scripts/mirror-opencv-codecs.sh --publish
```

It's safe to re-run after a partial failure, because `libs-release-local` rejects redeploys with 409
instead of overwriting.

The Windows native library has a different artifactId, `opencv_java`, without the `lib` prefix.
Weasis's POM declares it the same way. There is no 32-bit Windows build.

SHA-256 of the native libraries, verified against upstream's `.sha1`:

```
3552c806744192c734f6cf492bf95a139cbfd6f800ff662688d18b6a199f92f1  libopencv_java-5.0.0-dcm-linux-x86-64.so
b07190e6ef6e233c2b7dc7f945c5b470be44450a49f9915aeaaa86f8799ef6a0  libopencv_java-5.0.0-dcm-linux-aarch64.so
f447743478afd7b8bf1fb66d5df22a46f50b0b811d490638aec83992cbfe423e  libopencv_java-5.0.0-dcm-macosx-aarch64.dylib
902e3993685d3909d1952944943bd56a62d6010f6d1fcc3c6a98dc178fbe930e  libopencv_java-5.0.0-dcm-macosx-x86-64.dylib
c6ada67c315202471934f67f52ae4d0d16e7d8c0f2318bf53267be621b703324  opencv_java-5.0.0-dcm-windows-x86-64.dll
```

## Checking the mirror

Run the script with no arguments. Every artifact should show `present already`.

To check one by hand, use `curl -L`. Artifactory answers a binary download with a 302 redirect to
storage, so without `-L` a mirrored jar or native library looks missing. POMs are served directly
and look fine either way.

```bash
curl -sI -L -o /dev/null -w '%{http_code}\n' \
  https://nrgxnat.jfrog.io/nrgxnat/libs-release/org/weasis/thirdparty/org/opencv/libopencv_java/5.0.0-dcm/libopencv_java-5.0.0-dcm-linux-x86-64.so
```

The build reads from `libs-release`, a virtual repository that includes `libs-release-local` and
serves anonymously. Neither the build nor the image build needs credentials or a repository change.

## What OpenCV covers

OpenCV handles the JPEG family only:

| Transfer syntax | Decode | Encode |
| --- | --- | --- |
| JPEG Baseline / Extended / Lossless / Progressive | OpenCV | OpenCV |
| JPEG-LS | OpenCV | OpenCV |
| JPEG 2000, lossless and lossy | OpenCV | OpenCV |
| RLE Lossless | `dcm4che-imageio-rle` | none |

RLE Lossless can be decoded but not encoded. `dcm4che-imageio-rle` has only a reader, and
`ImageWriterFactory.properties` has no entry for `1.2.840.10008.1.2.5`. A redacted RLE object is
stored uncompressed.

## Using it from the build

Version catalogs can't express classifiers, so `dcm4che-imageio-opencv` goes in `libs.versions.toml`
and the native libraries are declared inline. `dicom-edit6/build.gradle` has a working example of
choosing the platform and staging the native library for tests.

```toml
# gradle/libs.versions.toml
weasis-opencv = "5.0.0-dcm"

dcm4che5-dcm4che-imageio-opencv = { group = "org.dcm4che", name = "dcm4che-imageio-opencv", version.ref = "dcm4che5" }
```

```groovy
// xnat-web/build.gradle: on the runtime classpath, so it is in the WAR
implementation libs.dcm4che5.dcm4che.imageio.opencv
```

A native library can't go in the WAR, so the image downloads it. It isn't copied from the build
context, because the reusable CI workflow (`NrgXnat/xnat-ci-workflows`) puts only `xnat.war` in that
context. The image installs it in `/usr/java/packages/lib`, which is on Java's default
`java.library.path` on Linux, so neither `-Djava.library.path` nor a Helm chart change is needed.

```dockerfile
# Dockerfile: pinned by checksum for each architecture
ARG TARGETARCH
RUN set -eu; \
    case "${TARGETARCH}" in \
        amd64) classifier=linux-x86-64;  sha256=3552c806... ;; \
        arm64) classifier=linux-aarch64; sha256=b07190e6... ;; \
        *) echo "No OpenCV native published for TARGETARCH=${TARGETARCH}" >&2; exit 1 ;; \
    esac; \
    mkdir -p /usr/java/packages/lib; \
    curl -fsSL -o /usr/java/packages/lib/libopencv_java.so "${OPENCV_BASE}/...-${classifier}.so"; \
    echo "${sha256}  /usr/java/packages/lib/libopencv_java.so" | sha256sum -c -
```

Pass `--build-arg INSTALL_OPENCV=false` for 1.9.x images. They use dcm4che 2 and would carry about
19 MB they never load.

Tests that decode JPEG-family data need the native library for the machine running the build:

```groovy
// dicom-edit6/build.gradle
tasks.register('stageOpenCvNative', Copy) {
    from configurations.opencvNative
    into layout.buildDirectory.dir('opencv-native')
    rename '.*', openCv.fileName   // System.loadLibrary needs libopencv_java.so, not the
}                                  // resolved file name with its version and classifier

test {
    dependsOn tasks.named('stageOpenCvNative')
    systemProperty 'java.library.path', layout.buildDirectory.dir('opencv-native').get().asFile.absolutePath
}
```

## Deploying the WAR without the image

The WAR includes `dcm4che-imageio-opencv` but not the native library. Sites that deploy the WAR into
their own Tomcat have to install the native library themselves. See
[installing-opencv-native.md](installing-opencv-native.md).

Without the native library, these fail with
`UnsatisfiedLinkError: 'long org.opencv.core.Mat.n_Mat(int, int, int)'`:

- Snapshots and thumbnails of JPEG-family scans.
- `alterPixels` on a JPEG-family object. The import, archive or re-anonymization of that object
  fails.

Native DICOM Pre-Compression is skipped. Uncompressed and RLE data don't need the native library.

### Compared with 1.10.1

XNAT 1.10.0 and 1.10.1 rendered snapshots with dcm4che 5.33.1, which maps every JPEG-family transfer
syntax, including JPEG Baseline, to the OpenCV readers:

```
# org/dcm4che3/imageio/codec/ImageReaderFactory.properties, in dcm4che-imageio-5.33.1.jar
1.2.840.10008.1.2.4.50:jpeg-cv:org.dcm4che3.opencv.NativeImageReader::
1.2.840.10008.1.2.4.90:jpeg2000-cv:org.dcm4che3.opencv.NativeImageReader::
```

Neither WAR included `dcm4che-imageio-opencv`, so these snapshots failed with
`No Reader for format: jpeg-cv registered` (XNAT-6581). Without the native library they still fail,
with the error above.

DicomEdit 6.9.1 redacted JPEG-family objects without OpenCV, and pre-compression fell back to the
pure-Java JPEG 2000 writer in `jai_imageio`. Both now need the native library.

## Checking that it worked

`ImageIO.getImageReadersByFormatName("jpeg2000-cv")` returns `NativeImageReader` whenever
`dcm4che-imageio-opencv` is on the classpath, with or without the native library, so it only checks
the jar. To check the native library, load it with `System.loadLibrary("opencv_java")`.
[installing-opencv-native.md](installing-opencv-native.md) has a short program that does this.

`StreamingRectanglePixelEditHandlerTest`, in `:dicom-edit6:test`, decodes, redacts and re-encodes a
JPEG 2000 Lossless object, keeping its transfer syntax. It also stores a redacted lossy JPEG object
uncompressed, with its compression history. These tests fail with
`UnsatisfiedLinkError: 'long org.opencv.core.Mat.n_Mat(int, int, int)'` if the staged native library
can't be loaded, and with `No Reader for format: jpeg2000-cv registered` if `dcm4che-imageio-opencv`
is missing.
