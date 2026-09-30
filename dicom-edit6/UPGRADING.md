# Upgrading DicomEdit 6

The major version of this library is the major version of the DicomEdit script language: every 6.x
release runs DicomEdit 6 scripts. Changes to the Java API, the dependencies or the runtime
requirements do not move it, so they are listed here, release by release.

From 6.10 on, each release also accepts its own major.minor as a script version, alongside every
earlier one: 6.10.0 accepts `version "6.10"`, and 6.11.0 will accept `version "6.11"`. A script
version means the same as any other; declaring a newer one makes older engines reject the script as
unsupported rather than run it without whatever changed. A patch release adds no script version.

## 6.9.1 to 6.10.0

### Scripts

Existing scripts keep working: every version header from `6.0` to `6.7` is still accepted.

`version "6.10"` is new. It means exactly what `6.7` means; a script that declares it will not run on
an engine before 6.10.0, which would apply the old `alterPixels` behaviour described below. There are
no `6.8` or `6.9` script versions; those numbers were library releases only.

`alterPixels` writes different output for the same script:

- **The transfer syntax is kept.** An uncompressed object stays in its own syntax; previously every
  redacted object came back Explicit VR Little Endian. Lossless JPEG, JPEG-LS and JPEG 2000 objects
  are re-encoded losslessly in their own syntax, and the pixels outside the rectangle are unchanged.
  If no encoder for the syntax is available, or encoding fails, the object comes back uncompressed
  instead and a warning is logged.
- **Lossy objects come back uncompressed.** JPEG Baseline, lossy JPEG 2000 and the other lossy
  syntaxes are not re-encoded, since that would degrade the whole image to redact one rectangle.
  Lossy Image Compression (0028,2110) is set to `01`, and this compression step is added to the
  history in Lossy Image Compression Method (0028,2114) and Ratio (0028,2112). Those pair up in
  order, one value per step, and values already present are never changed. The method is added
  unless the last one recorded is already this step's, and the ratio only when every earlier step has
  one. The method uses the defined term for the source syntax, including HTJ2K (`ISO_15444_15`),
  JPEG XL (`ISO_18181_1`) and the video syntaxes. RLE objects also come back uncompressed, because
  nothing can encode RLE.
- **The fill value is used.** The region is filled with the value the script asks for; previously it
  was always filled with 0.
- **The redaction is recorded once.** As before, Burned In Annotation (0028,0301) is set to `NO`, and
  "Burned in text blacked out" and code 113101 "Clean Pixel Data Option" are added to
  De-identification Method (0012,0063) and De-identification Method Code Sequence (0012,0064). They
  are no longer added a second time when the object already carries them. When the rectangle misses
  the image, nothing is recorded and Burned In Annotation is left as it was; 6.9.1 removed it even
  then.
- **Floating point pixel data is redacted.** Float Pixel Data and Double Float Pixel Data, as in a
  parametric map, used to pass through unchanged.
- **Some objects are refused.** `alterPixels` fails on objects it cannot redact exactly, including
  integer samples whose Bits Allocated is not a multiple of 8, a 4:2:2 layout without 3 samples per
  pixel or with samples wider than 8 bits, YBR_PARTIAL_420 (which shares chroma samples between
  rows), floating point pixel data in a compressed syntax, and a compressed object whose decoded
  pixel data would be too long for a pixel data element.

### Running

Redacting compressed pixel data needs the OpenCV native library (`libopencv_java`) on
`java.library.path`. Without it, `alterPixels` fails on every JPEG-family object with "Unable to
transcode pixel data from ... A codec for the transfer syntax is required to redact pixels in a
compressed object.", caused by an `UnsatisfiedLinkError`. Uncompressed and RLE objects do not need
it. The XNAT image installs the native; [mirroring-opencv-codecs.md](../docs/mirroring-opencv-codecs.md)
covers installing it anywhere else.

Redaction stages pixel data in scratch files under `java.io.tmpdir`, or under the directory named by
the `dicom.pixeledit.scratch.dir` system property. Allow the size of the object for an uncompressed
syntax, and more than that for a compressed one.

### Java API and dependencies

- **dcm4che 5.35.0 or later.** Earlier releases of `dcm4che-imageio-opencv` fail on Java 17 and later
  with `NullPointerException: ... because "seg" is null` for every compressed object.
- **Add the codecs to the runtime classpath.** `org.dcm4che:dcm4che-imageio-opencv` and
  `org.dcm4che:dcm4che-imageio-rle`, at the dcm4che version above, are declared by this library for
  its own tests only.
- **Removed classes.** `PixelmedPixelEditHandler`, `DicomImageBlackout` (with its
  `BurnedInAnnotationFlagAction`) and `Compressor` are gone from `org.nrg.dicom.dicomedit.pixels.impl`.
  The handler registered in `META-INF/services` is now `StreamingRectanglePixelEditHandler`. The
  `PixelEditHandler` interface is unchanged.
- **pixelmed is no longer a dependency.** Code that uses it has to declare `com.dclunie:pixelmed`
  itself. The XNAT WAR still ships pixelmed for plugins built against it, until 1.11.
- **Released dependencies only.** The published 6.9.1 POM depends on mizer and framework
  `1.10.1-SNAPSHOT`. 6.10.0 is published only by the build that releases those modules.
- **7.0.0 snapshots are withdrawn.** Builds published as `7.0.0-SNAPSHOT` or `7.0.0-RC-SNAPSHOT` are
  replaced by 6.10.0; use `6.10.0-SNAPSHOT` until 6.10.0 is released.
