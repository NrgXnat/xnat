# Installing the OpenCV native library for XNAT 1.10.2

Starting with 1.10.2, XNAT decodes and encodes JPEG-family DICOM pixel data (JPEG Baseline and Extended, JPEG Lossless, JPEG-LS and JPEG 2000) with OpenCV. The WAR includes the Java side of OpenCV, but not the native library it calls.

If you run XNAT via the **XNAT container image**, no action is needed; The container image already includes the library.
If you deploy XNAT by dropping the **WAR into your own Tomcat, or your own container image**, you should install the library yourself, as described below.

_You can install it before you upgrade. XNAT 1.10.1 never loads it._

Without the library, XNAT starts and runs normally, and uncompressed and RLE data behave as before. However:

* JPEG-family scans get no snapshots or thumbnails.
* If an anonymization script applies `alterPixels` to a JPEG-family object, the import, archive or re-anonymization of that object fails.
* Native DICOM Pre-Compression, if you turned it on, is skipped.

## Getting the library

Use this exact build (5.0.0-dcm), because it must match the Java code in the WAR. An OpenCV package from your operating system won't work.

Linux and macOS builds are at:
`https://nrgxnat.jfrog.io/nrgxnat/libs-release/org/weasis/thirdparty/org/opencv/libopencv_java/5.0.0-dcm/`

The Windows build is at:
`https://nrgxnat.jfrog.io/nrgxnat/libs-release/org/weasis/thirdparty/org/opencv/opencv_java/5.0.0-dcm/`

| Platform | File | SHA-256 | Notes |
|---|---|---|---|
| Linux x86-64 | `libopencv_java-5.0.0-dcm-linux-x86-64.so` | `3552c806744192c734f6cf492bf95a139cbfd6f800ff662688d18b6a199f92f1` | glibc 2.17 or later (not Alpine/musl) |
| Linux aarch64 | `libopencv_java-5.0.0-dcm-linux-aarch64.so` | `b07190e6ef6e233c2b7dc7f945c5b470be44450a49f9915aeaaa86f8799ef6a0` | glibc 2.27 or later (not Alpine/musl) |
| macOS Apple silicon | `libopencv_java-5.0.0-dcm-macosx-aarch64.dylib` | `f447743478afd7b8bf1fb66d5df22a46f50b0b811d490638aec83992cbfe423e` | |
| macOS Intel | `libopencv_java-5.0.0-dcm-macosx-x86-64.dylib` | `902e3993685d3909d1952944943bd56a62d6010f6d1fcc3c6a98dc178fbe930e` | |
| Windows x86-64 | `opencv_java-5.0.0-dcm-windows-x86-64.dll` | `c6ada67c315202471934f67f52ae4d0d16e7d8c0f2318bf53267be621b703324` | No Visual C++ runtime needed |

There are no builds for 32-bit or ARM Windows.

Java looks the library up by a fixed file name, so rename the download: `libopencv_java.so` on Linux, `libopencv_java.dylib` on macOS, `opencv_java.dll` on Windows.

## Linux

`/usr/java/packages/lib` is on Java's default library path, so no Tomcat configuration is needed. For aarch64, use the aarch64 file name and checksum from the table.

```bash
sudo mkdir -p /usr/java/packages/lib
sudo curl -fsSL -o /usr/java/packages/lib/libopencv_java.so \
  https://nrgxnat.jfrog.io/nrgxnat/libs-release/org/weasis/thirdparty/org/opencv/libopencv_java/5.0.0-dcm/libopencv_java-5.0.0-dcm-linux-x86-64.so
echo "3552c806744192c734f6cf492bf95a139cbfd6f800ff662688d18b6a199f92f1  /usr/java/packages/lib/libopencv_java.so" | sha256sum -c -
sudo chmod 644 /usr/java/packages/lib/libopencv_java.so
```

Then restart Tomcat.

**If your Tomcat already sets `-Djava.library.path`** (for example, for the Tomcat Native library), Java no longer searches the default path. Look for it in `setenv.sh` or your service's JVM options. Then either add `/usr/java/packages/lib` to that value, separated by `:`, or put the file in a directory it already lists.

To use a different directory, add it to `CATALINA_OPTS` in `$CATALINA_BASE/bin/setenv.sh`:

```bash
CATALINA_OPTS="$CATALINA_OPTS -Djava.library.path=/opt/opencv/lib"
```

## Windows

`%SystemRoot%\Sun\Java\bin` is on Java's default library path, so no Tomcat configuration is needed. It doesn't exist until you create it. Run these commands in PowerShell as Administrator:

```powershell
$dir = "$env:SystemRoot\Sun\Java\bin"
New-Item -ItemType Directory -Force $dir | Out-Null
curl.exe -fsSL -o "$dir\opencv_java.dll" https://nrgxnat.jfrog.io/nrgxnat/libs-release/org/weasis/thirdparty/org/opencv/opencv_java/5.0.0-dcm/opencv_java-5.0.0-dcm-windows-x86-64.dll
(Get-FileHash "$dir\opencv_java.dll" -Algorithm SHA256).Hash
# expect C6ADA67C315202471934F67F52AE4D0D16E7D8C0F2318BF53267BE621B703324
```

Then restart the Tomcat service.

To use a different directory, add it to `-Djava.library.path` instead. For the Tomcat service, that's under Java Options on the Java tab of `tomcat9w.exe`. If a value is already set, add your directory to it, separated by `;`.

## macOS (development)

`/Library/Java/Extensions` is on Java's default library path. Pick the build that matches your JDK, not your Mac: an Intel JDK running under Rosetta needs the Intel build.

```bash
sudo mkdir -p /Library/Java/Extensions
sudo curl -fsSL -o /Library/Java/Extensions/libopencv_java.dylib \
  https://nrgxnat.jfrog.io/nrgxnat/libs-release/org/weasis/thirdparty/org/opencv/libopencv_java/5.0.0-dcm/libopencv_java-5.0.0-dcm-macosx-aarch64.dylib
shasum -a 256 /Library/Java/Extensions/libopencv_java.dylib
```

## Your own container image

Copy the OpenCV `RUN` step from the XNAT [`Dockerfile`](../Dockerfile). It picks the right Linux build for the image's architecture and checks it against the checksum. The base image must use glibc.

## Checking the install

**Before you upgrade**, you can check that Java loads the library. You need a JDK (Java 11 or later) for this. Save this file as `OpenCvCheck.java`:

```java
public class OpenCvCheck {
    public static void main(String[] args) {
        System.loadLibrary("opencv_java");
        System.out.println("OpenCV native library loaded");
    }
}
```

Run it as the Tomcat user, with Tomcat's `java`:

```bash
sudo -u tomcat java OpenCvCheck.java
```

If Tomcat sets `-Djava.library.path`, pass the same option. On success, it prints `OpenCV native library loaded`.

**After you upgrade**, open the snapshot of a JPEG or JPEG 2000 compressed scan.

## Troubleshooting

When the library doesn't load, XNAT writes `Cannot load OpenCV native library: <reason>` to Tomcat's standard error. On Linux, that's `catalina.out` or the systemd journal. On Windows, it's the `-stderr` log in Tomcat's `logs` folder. XNAT's own logs then show `UnsatisfiedLinkError: 'long org.opencv.core.Mat.n_Mat(int, int, int)'`.

| Message | Cause |
|---|---|
| `no opencv_java in java.library.path: ...` | The file isn't in any listed directory, or it has the wrong name. The message lists the directories searched. |
| `... (Possible cause: can't load AMD 64 .so on a AARCH64 platform)` | The build doesn't match the JVM's architecture. |
| `version 'GLIBC_2.xx' not found` | The operating system is too old for this build. |
| `No Reader for format: jpeg-cv registered` (or `jpeg2000-cv`) | This isn't the native library. The `dcm4che-imageio-opencv` jar is missing from the deployed `WEB-INF/lib`. Redeploy the WAR. |
| `NullPointerException ... because "seg" is null` | An older `dcm4che-imageio-opencv` jar is loaded instead of the WAR's, usually one added to `${xnat.home}/plugins`. Remove it. |
