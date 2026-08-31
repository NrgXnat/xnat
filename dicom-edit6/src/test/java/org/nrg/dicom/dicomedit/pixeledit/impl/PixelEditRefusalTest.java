package org.nrg.dicom.dicomedit.pixeledit.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.dicomedit.pixels.impl.StreamingRectanglePixelEditHandler;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Pixel depths the redactor refuses, and the fact that refusing leaves the object alone.
 * <p>
 * A rectangle is located as a byte range within a line, which only works where a sample occupies a
 * whole number of bytes. Where it does not, redacting anyway would write the fill across
 * neighbouring pixels and leave the ones that were asked for partly intact -- a de-identification
 * that reports success having not done the job. So the redactor stops, and the object fails the
 * archive rather than entering it.
 * <p>
 * That is a deliberate choice with a real cost: {@code BitsAllocated} of 1 is how BINARY
 * segmentation is stored, so a site running {@code alterPixels} site-wide cannot ingest those
 * objects at all. The workaround is to exempt them in the script, where the intent lives. These
 * tests exist so the refusal cannot be softened into a silent skip without someone deciding to.
 */
public class PixelEditRefusalTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final StreamingRectanglePixelEditHandler handler = new StreamingRectanglePixelEditHandler();

    /** BitsAllocated of 1: eight pixels to a byte, as BINARY segmentation stores its mask. */
    @Test
    public void refusesBitPackedPixelsRatherThanRedactingAcrossThem() throws Exception {
        assertRefused(1, "BitsAllocated of 1");
    }

    /**
     * Any depth that is not a whole number of bytes, not only the packed case. Twelve bits is the
     * one that turns up, usually as BitsStored on a 16-bit object but occasionally as BitsAllocated.
     */
    @Test
    public void refusesADepthThatIsNotAWholeNumberOfBytes() throws Exception {
        assertRefused(12, "BitsAllocated of 12");
    }

    /**
     * A 422 group is laid out Y Y Cb Cr, so it needs three samples to fill. An object declaring a
     * 422 photometric interpretation with any other sample count disagrees with itself; the fill
     * would run off the end of the samples it was given.
     */
    @Test
    public void refusesSubsampledColourThatDoesNotCarryThreeSamples() throws Exception {
        final File   source = subsampled(1);
        final byte[] before = Files.readAllBytes(source.toPath());

        final DicomObjectI dobj = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        try {
            handler.process(new Rectangle2D.Float(0, 0, 8, 8), new Color(0, 0, 0), dobj);
            fail("expected a refusal for YBR_FULL_422 with 1 sample per pixel");
        } catch (MizerException e) {
            final String message = String.valueOf(e.getMessage());
            assertTrue("the message should name the layout, was: " + message, message.contains("YBR_FULL_422"));
            assertTrue("the message should name the sample count, was: " + message, message.contains("1 samples per pixel"));
        } finally {
            dobj.releaseScratchFiles();
        }
        assertArrayEquals("a refused object must be left exactly as it was",
                          before, Files.readAllBytes(source.toPath()));
    }

    /** A 422 object declaring the given sample count, which for 422 may only legally be three. */
    private File subsampled(final int samplesPerPixel) throws Exception {
        final int        side    = 16;
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.8" + samplesPerPixel);
        dataset.setInt(Tag.Rows, VR.US, side);
        dataset.setInt(Tag.Columns, VR.US, side);
        dataset.setInt(Tag.SamplesPerPixel, VR.US, samplesPerPixel);
        dataset.setString(Tag.PhotometricInterpretation, VR.CS, "YBR_FULL_422");
        dataset.setInt(Tag.PlanarConfiguration, VR.US, 0);
        dataset.setInt(Tag.BitsAllocated, VR.US, 8);
        dataset.setInt(Tag.BitsStored, VR.US, 8);
        dataset.setInt(Tag.HighBit, VR.US, 7);
        dataset.setInt(Tag.PixelRepresentation, VR.US, 0);
        dataset.setBytes(Tag.PixelData, VR.OB, new byte[side * side * 2]);

        final File file = temporaryFolder.newFile("subsampled-" + samplesPerPixel + ".dcm");
        try (final DicomOutputStream out = new DicomOutputStream(file)) {
            out.writeDataset(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset);
        }
        return file;
    }

    /**
     * Refusing has to leave the object as it was: the importer reports the failure, and an object
     * that had been partly rewritten first would be neither redacted nor original.
     */
    private void assertRefused(final int bitsAllocated, final String expected) throws Exception {
        final File   source = object(bitsAllocated);
        final byte[] before = Files.readAllBytes(source.toPath());

        final DicomObjectI dobj = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        try {
            handler.process(new Rectangle2D.Float(0, 0, 8, 8), new Color(0, 0, 0), dobj);
            fail("expected a refusal for BitsAllocated of " + bitsAllocated);
        } catch (MizerException e) {
            final String message = String.valueOf(e.getMessage());
            assertTrue("the message should name the depth, was: " + message, message.contains(expected));
            assertTrue("the message should say why, was: " + message, message.contains("byte-aligned"));
        } finally {
            dobj.releaseScratchFiles();
        }

        assertArrayEquals("a refused object must be left exactly as it was",
                          before, Files.readAllBytes(source.toPath()));
        assertEquals("a refusal should stage nothing to clean up later",
                     0, temporaryFolder.getRoot().listFiles((d, n) -> n.startsWith("pixeledit")).length);
    }

    /** A 16x16 single-frame image at the given depth, its pixel data sized to match. */
    private File object(final int bitsAllocated) throws Exception {
        final int        side    = 16;
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.7" + bitsAllocated);
        dataset.setInt(Tag.Rows, VR.US, side);
        dataset.setInt(Tag.Columns, VR.US, side);
        dataset.setInt(Tag.SamplesPerPixel, VR.US, 1);
        dataset.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        dataset.setInt(Tag.BitsAllocated, VR.US, bitsAllocated);
        dataset.setInt(Tag.BitsStored, VR.US, bitsAllocated);
        dataset.setInt(Tag.HighBit, VR.US, bitsAllocated - 1);
        dataset.setInt(Tag.PixelRepresentation, VR.US, 0);

        final byte[] pixels = new byte[(side * side * bitsAllocated + 7) / 8];
        java.util.Arrays.fill(pixels, (byte) 0xAB);
        dataset.setBytes(Tag.PixelData, VR.OB, pixels);

        final File file = temporaryFolder.newFile("depth-" + bitsAllocated + ".dcm");
        try (final DicomOutputStream out = new DicomOutputStream(file)) {
            out.writeDataset(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset);
        }
        return file;
    }
}
