# Mirroring the OpenCV image codecs into Artifactory

XNAT resolves compressed DICOM pixel data through dcm4che 5's `ImageReaderFactory`, whose stock
configuration names the OpenCV-backed plugins for every JPEG-family transfer syntax:

```
# org/dcm4che3/imageio/codec/ImageReaderFactory.properties, in dcm4che-imageio-5.33.1.jar
1.2.840.10008.1.2.4.70 : jpeg-cv     : org.dcm4che3.opencv.NativeImageReader
1.2.840.10008.1.2.4.90 : jpeg2000-cv : org.dcm4che3.opencv.NativeImageReader
```

Those classes live in `dcm4che-imageio-opencv`, which is Java glue over a native OpenCV build. Without
them, anything routed through the factory fails with `No Reader for format: jpeg2000-cv registered`.

The native binaries are declared `<scope>provided</scope>` in Weasis's POM, so they are **never
resolved transitively** — the 238 KB `weasis-core-img` jar contains no binaries at all. They have to
be declared explicitly, per platform, which means whichever repository serves them must hold them.

## What is already available, and what is not

Probed against `https://nrgxnat.jfrog.io/nrgxnat/libs-release`:

| Artifact | Status |
| --- | --- |
| `org.dcm4che:dcm4che-imageio-opencv:5.33.1` | present |
| `org.weasis.core:weasis-core-img-bom:4.9.0.1` | present |
| `org.weasis.core:weasis-core-img:4.9.0.1` | **missing** |
| `org.weasis.thirdparty.org.opencv:libopencv_java:4.9.0-dcm` (all classifiers) | **missing** |

So only the bottom two rows need mirroring. Upstream is
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

  weasis-core-img-4.9.0.1.pom                    ok        3441 bytes  sha256=884566e5...
  libopencv_java-4.9.0-dcm-linux-x86-64.so       ok    18841808 bytes  sha256=41d81dfe...
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


`libs-release-local` returns 409 on redeploy rather than overwriting, so the script is safe to re-run;
it reports what was already there instead of failing.

For the record, SHA-256 of the artifacts that matter most, verified against the upstream `.sha1`:

```
50ef4d80ab6d95e83f7bad82b99f9792520566eb77ca6bdd0bbc3ec3cbbb84e7  weasis-core-img-4.9.0.1.jar
41d81dfe284b448f38a1420f711c30b53f3a42035067059fcc9449d1b2cadca3  libopencv_java-4.9.0-dcm-linux-x86-64.so
5dd0d8fd94d97504cad8d6eeef26c23cc1bbb975732467bce793f6b5959077b3  libopencv_java-4.9.0-dcm-linux-aarch64.so
0201f684e7e85881624fe2d91ca3d8fc36ae16b53143c876bef7754c6de8457a  libopencv_java-4.9.0-dcm-macosx-aarch64.dylib
```

## Verifying the mirror

```bash
for p in org/weasis/core/weasis-core-img/4.9.0.1/weasis-core-img-4.9.0.1.jar \
         org/weasis/thirdparty/org/opencv/libopencv_java/4.9.0-dcm/libopencv_java-4.9.0-dcm-linux-x86-64.so; do
  echo "$(curl -s -o /dev/null -w '%{http_code}' "https://nrgxnat.jfrog.io/nrgxnat/libs-release/$p")  $p"
done
```

Both should report 200. `libs-release` is the virtual repository the build reads from and it already
includes `libs-release-local`, so no repository declaration changes are needed.

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
weasis-opencv = "4.9.0-dcm"

dcm4che5-dcm4che-imageio-opencv = { group = "org.dcm4che", name = "dcm4che-imageio-opencv", version.ref = "dcm4che5" }
```

```groovy
// xnat-web/build.gradle
implementation libs.dcm4che5.dcm4che.imageio.opencv

// The native is not a classpath entry -- it is staged into the image build context. A separate
// configuration keeps it off the runtime classpath while still resolving through Artifactory.
configurations { opencvNative }
dependencies {
    opencvNative "org.weasis.thirdparty.org.opencv:libopencv_java:${libs.versions.weasis.opencv.get()}:linux-x86-64@so"
}
tasks.register('stageOpenCvNative', Copy) {
    from configurations.opencvNative
    into layout.buildDirectory.dir('docker-context')
    rename '.*', 'libopencv_java.so'
}
```

The image then places it where the JVM already looks. On Linux `/usr/java/packages/lib` is on the
default `java.library.path`, so no `-Djava.library.path` and no Helm chart change is required:

```dockerfile
# Dockerfile
COPY docker-context/libopencv_java.so /usr/java/packages/lib/
```

Tests that decode compressed pixel data need the native too. Resolve the classifier matching the
build host and point the test JVM at it:

```groovy
// dicom-edit6/build.gradle
test {
    systemProperty 'java.library.path', configurations.opencvNative.singleFile.parent
}
```

## Checking that it worked

With the native loadable, these format names resolve; without it they are absent:

```java
ImageIO.getImageReadersByFormatName("jpeg2000-cv")   // -> NativeImageReader
```

The encapsulated cases in `StreamingRectanglePixelEditHandlerTest` are ignored pending exactly this;
un-ignore them once the mirror and the native staging are in place.

Note that OpenCV does **not** cover RLE Lossless — that reader is pure Java in
`org.dcm4che:dcm4che-imageio-rle`, which `xnat-web` declares separately.
