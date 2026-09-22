package org.nrg.xnat.archive;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

/**
 * The parity matrix for the two off-heap bulk data paths: whatever the transfer syntax, reading an
 * object through the file path ({@code DicomObjectFactory.newInstance(file, IncludeBulkData.URI)},
 * how file anonymization reads) or through the stream path ({@link ReceivedDicomObject}, how the
 * importer reads) and writing it back must reproduce the pixel data exactly -- and the file path
 * must never damage its source. Both paths share one bulk data creator and one serializer; this
 * matrix is what proves the sharing preserves every syntax, and what catches the next
 * fast-path-with-a-precondition bug before it ships.
 */
public class BulkDataPathParityTest {

    private static final File FIXTURE = Paths.get("src", "test", "resources", "dicom",
            "1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm").toFile();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void explicitLittleEndianReadsTheSameThroughBothPaths() throws Exception {
        assertPixelParityBothPaths(transcode(UID.ExplicitVRLittleEndian));
    }

    @Test
    public void implicitLittleEndianReadsTheSameThroughBothPaths() throws Exception {
        assertPixelParityBothPaths(transcode(UID.ImplicitVRLittleEndian));
    }

    @Test
    public void explicitBigEndianReadsTheSameThroughBothPaths() throws Exception {
        assertPixelParityBothPaths(transcode(UID.ExplicitVRBigEndian));
    }

    @Test
    public void deflatedReadsTheSameThroughBothPaths() throws Exception {
        assertPixelParityBothPaths(transcode(UID.DeflatedExplicitVRLittleEndian));
    }

    /**
     * Encapsulated pixel data reads as fragments, each referenced separately, so the strongest
     * check is byte identity of the whole round-tripped object.
     */
    @Test
    public void encapsulatedRoundTripsByteForByteThroughBothPaths() throws Exception {
        final File   source = encapsulatedFixture();
        final byte[] bytes  = Files.readAllBytes(source.toPath());

        assertArrayEquals("the file path must reproduce an encapsulated object exactly",
                          bytes, writeThroughFilePath(source));
        assertArrayEquals("the file path must not change its source",
                          bytes, Files.readAllBytes(source.toPath()));
        assertArrayEquals("the stream path must reproduce an encapsulated object exactly",
                          bytes, writeThroughStreamPath(source));
    }

    /** A gzipped source has no honest file offsets, so the file path must spool -- and still agree. */
    @Test
    public void gzippedFileReadsTheSameThroughTheFilePath() throws Exception {
        final File plain = transcode(UID.ExplicitVRLittleEndian);
        final File gzipped = folder.newFile("fixture.dcm.gz");
        try (final GZIPOutputStream out = new GZIPOutputStream(Files.newOutputStream(gzipped.toPath()))) {
            Files.copy(plain.toPath(), out);
        }
        final byte[] reference;
        try (final InputStream in = new GZIPInputStream(new FileInputStream(gzipped))) {
            reference = pixels(readWhole(in));
        }
        assertArrayEquals("pixels must survive the file path's read of a gzipped source",
                          reference, pixels(readWhole(new ByteArrayInputStream(writeThroughFilePath(gzipped)))));
    }

    private void assertPixelParityBothPaths(final File source) throws Exception {
        final byte[] sourceBytes = Files.readAllBytes(source.toPath());
        final byte[] reference   = pixels(readWhole(new ByteArrayInputStream(sourceBytes)));

        assertArrayEquals("pixels must survive the file path (how file anonymization reads)",
                          reference, pixels(readWhole(new ByteArrayInputStream(writeThroughFilePath(source)))));
        assertArrayEquals("the file path must not change its source",
                          sourceBytes, Files.readAllBytes(source.toPath()));
        assertArrayEquals("pixels must survive the stream path (how the importer reads)",
                          reference, pixels(readWhole(new ByteArrayInputStream(writeThroughStreamPath(source)))));
    }

    /** Reads through {@code newInstance(file, URI)} and writes back, as file anonymization does. */
    private static byte[] writeThroughFilePath(final File source) throws Exception {
        final ByteArrayOutputStream written = new ByteArrayOutputStream();
        final DicomObjectI read = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        try {
            read.write(written);
        } finally {
            read.releaseScratchFiles();
        }
        return written.toByteArray();
    }

    /** Reads through {@link ReceivedDicomObject} whole and writes back, as an anonymizing import does. */
    private byte[] writeThroughStreamPath(final File source) throws Exception {
        final File output = folder.newFile(source.getName() + ".out");
        try (final ReceivedDicomObject received = ReceivedDicomObject.read(
                new FileInputStream(source), null, Tag.SeriesDescription, true)) {
            received.write(received.getDataset(), null, output, "parity-test");
        }
        return Files.readAllBytes(output.toPath());
    }

    /** The fixture rewritten in the given transfer syntax. */
    private File transcode(final String transferSyntax) throws Exception {
        final Attributes dataset = readWhole(new FileInputStream(FIXTURE));
        final File transcoded = folder.newFile("fixture-" + transferSyntax + ".dcm");
        try (final DicomOutputStream out = new DicomOutputStream(transcoded)) {
            out.writeDataset(dataset.createFileMetaInformation(transferSyntax), dataset);
        }
        return transcoded;
    }

    /** An object with three fragments of (not actually compressed) encapsulated pixel data. No codec needed. */
    private File encapsulatedFixture() throws IOException {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.411");
        dataset.setString(Tag.StudyInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.412");
        dataset.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.413");
        dataset.setInt(Tag.Rows, VR.US, 8);
        dataset.setInt(Tag.Columns, VR.US, 8);
        dataset.setInt(Tag.BitsAllocated, VR.US, 8);
        dataset.setInt(Tag.SamplesPerPixel, VR.US, 1);
        dataset.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        dataset.setInt(Tag.NumberOfFrames, VR.IS, 3);
        final Fragments fragments = dataset.newFragments(Tag.PixelData, VR.OB, 4);
        fragments.add(new byte[0]);
        for (int frame = 1; frame <= 3; frame++) {
            final byte[] fragment = new byte[1000 + 2 * frame];
            for (int i = 0; i < fragment.length; i++) {
                fragment[i] = (byte) (frame * 41 + i);
            }
            fragments.add(fragment);
        }
        final File file = folder.newFile("encapsulated.dcm");
        try (final DicomOutputStream out = new DicomOutputStream(file)) {
            out.writeDataset(dataset.createFileMetaInformation(UID.JPEGBaseline8Bit), dataset);
        }
        return file;
    }

    private static Attributes readWhole(final InputStream in) throws IOException {
        try (final DicomInputStream dis = new DicomInputStream(in)) {
            dis.readFileMetaInformation();
            return dis.readDataset();
        }
    }

    private static byte[] pixels(final Attributes dataset) throws IOException {
        final byte[] pixels = dataset.getBytes(Tag.PixelData);
        assertNotNull("the fixture must carry pixel data for this comparison to mean anything", pixels);
        return pixels;
    }
}
