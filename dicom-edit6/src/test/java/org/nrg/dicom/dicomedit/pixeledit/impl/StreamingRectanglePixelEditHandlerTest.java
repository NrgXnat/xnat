package org.nrg.dicom.dicomedit.pixeledit.impl;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.dicomedit.BaseScriptApplicator;
import org.nrg.dicom.dicomedit.pixels.impl.StreamingRectanglePixelEditHandler;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that redaction writes the value the script asked for, in the right place, leaves
 * everything else alone, and preserves the transfer syntax.
 * <p>
 * Assertions are made against stored pixel values across the whole image, so a redaction that lands
 * in the wrong byte range -- the risk with big-endian samples, planar colour, or multi-frame
 * offsets -- fails rather than passing on a sampled pixel.
 */
public class StreamingRectanglePixelEditHandlerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final StreamingRectanglePixelEditHandler handler = new StreamingRectanglePixelEditHandler();

    // ------------------------------------------------------------------ native transfer syntaxes

    @Test
    public void redactsImplicitVrLittleEndian16Bit() throws Exception {
        // 512x512 MONOCHROME2, BitsAllocated 16, BitsStored 12. Not the only guard for anything,
        // but the only case on Implicit VR Little Endian, which is what most scanners send.
        redactAndVerify("dicom/single-frame/CT-ivle-mono2-12bits.dcm",
                        new Rectangle2D.Float(200, 200, 100, 100), new Color(200, 200, 200),
                        UID.ImplicitVRLittleEndian);
    }

    @Test
    public void redactsInterleavedColour() throws Exception {
        // RGB, PlanarConfiguration 0: the three samples of a pixel are adjacent.
        redactAndVerify("dicom/single-frame/US-evle-rgb-8bits.dcm",
                        new Rectangle2D.Float(100, 10, 50, 50), new Color(10, 20, 30),
                        UID.ExplicitVRLittleEndian);
    }

    @Test
    public void redactsPlanarColour() throws Exception {
        // The only guard against planar colour being treated as interleaved.
        // RGB with PlanarConfiguration 1: each sample lives in its own plane rather than beside its
        // neighbours, so a rectangle is one byte run per plane. This fixture is Explicit VR Big
        // Endian too, but at 8 bits per sample byte order cannot show up -- see
        // redactsBigEndianSixteenBit for that.
        redactAndVerify("dicom/single-frame/US-evbe-rgb-8bits.dcm",
                        new Rectangle2D.Float(200, 200, 100, 100), new Color(10, 20, 30),
                        UID.ExplicitVRBigEndian);
    }

    @Test
    public void redactsBigEndianSixteenBit() throws Exception {
        // The only guard against byte order being ignored.
        // Explicit VR Big Endian at 16 bits, so the fill value has to be written most significant
        // byte first. Every other fixture is either little endian or 8 bits per sample, where the
        // byte order branch cannot be wrong.
        redactAndVerify("dicom/single-frame/CT-evbe-mono2-16bits.dcm",
                        new Rectangle2D.Float(150, 150, 120, 120), new Color(200, 200, 200),
                        UID.ExplicitVRBigEndian);
    }

    // ------------------------------------------------------------------------- lossless encapsulated

    @Test
    public void preservesLosslessCompressionAndRedacts() throws Exception {
        // JPEG 2000 lossless: decode, redact, re-encode. Transfer syntax must survive, because
        // decompressing a lossless object inflates it for no gain.
        redactAndVerify("dicom/DE44/horos_jpg2k.dcm",
                        new Rectangle2D.Float(100, 100, 80, 80), new Color(0, 0, 0),
                        UID.JPEG2000Lossless);
    }

    @Test
    public void redactsEveryFrameOfMultiFrameRle() throws Exception {
        // RLE Lossless is lossless, but its transfer syntax cannot be preserved: dcm4che ships an
        // RLE reader and no RLE writer (dcm4che-imageio-rle contains only an ImageReaderSpi, and
        // ImageWriterFactory.properties has no entry for 1.2.840.10008.1.2.5), and the OpenCV
        // codecs do not cover RLE either. With nothing able to re-encode it, the redacted object is
        // stored uncompressed. Every frame must still be redacted, which is what this pins down.
        redactAndVerify("dicom/multi-frame/us-rle-8bit.dcm",
                        new Rectangle2D.Float(50, 50, 100, 100), new Color(7, 7, 7),
                        UID.ExplicitVRLittleEndian);
    }

    // ----------------------------------------------------------------------------- lossy encapsulated

    @Test
    public void writesLossySourceUncompressedWithHistory() throws Exception {
        // The only guard against the lossy compression history not being recorded.
        // Re-encoding lossy input would cost a second generation of loss over the whole image, so
        // the redacted object is stored uncompressed and the history is recorded instead.
        // A non-zero fill on purpose: this object's corner is solid black, so a fill of zero would
        // have been indistinguishable from no redaction at all.
        final File output = redact("dicom/multi-frame/xa-jpeg1.dcm",
                                   new Rectangle2D.Float(20, 20, 60, 60), new Color(180, 180, 180));
        final RawPixels result = RawPixels.of(output);
        assertEquals("lossy input should be stored uncompressed",
                     UID.ExplicitVRLittleEndian, result.transferSyntax);
        assertEquals("lossy compression must be recorded",
                     "01", result.dataset.getString(Tag.LossyImageCompression));
        assertTrue("a lossy compression method should be recorded",
                   result.dataset.getString(Tag.LossyImageCompressionMethod) != null);
        assertRedacted(RawPixels.of(resource("dicom/multi-frame/xa-jpeg1.dcm")), result,
                       new Rectangle2D.Float(20, 20, 60, 60), new Color(180, 180, 180), false);
    }

    // ------------------------------------------------------------------------------------ edge cases

    @Test
    public void leavesImageAloneWhenRectangleMissesIt() throws Exception {
        final String source = "dicom/single-frame/US-evle-rgb-8bits.dcm";
        final File   output = redact(source, new Rectangle2D.Float(5000, 5000, 10, 10), new Color(255, 0, 0));

        final RawPixels before = RawPixels.of(resource(source));
        final RawPixels after  = RawPixels.of(output);
        assertEquals(before.length(), after.length());
        for (int y = 0; y < before.rows; y++) {
            for (int x = 0; x < before.columns; x++) {
                for (int sample = 0; sample < before.samplesPerPixel; sample++) {
                    assertEquals("pixel " + x + "," + y + " sample " + sample + " should be untouched",
                                 before.sample(0, x, y, sample), after.sample(0, x, y, sample));
                }
            }
        }
    }

    @Test
    public void clipsRectangleOverhangingTheImage() throws Exception {
        // The only guard against the rectangle not being clipped to the image.
        // Overhanging the right and bottom edges must clip, not wrap onto the next row or frame.
        redactAndVerify("dicom/single-frame/US-evle-rgb-8bits.dcm",
                        new Rectangle2D.Float(200, 90, 500, 500), new Color(1, 2, 3),
                        UID.ExplicitVRLittleEndian);
    }

    // The only guard against staged pixel files being left behind.
    @Test
    public void deletesScratchFilesOnRelease() throws Exception {
        final DicomObjectI dobj = DicomObjectFactory.newInstance(
                resource("dicom/single-frame/US-evle-mono2-8bits.dcm"), DicomInputStream.IncludeBulkData.URI);
        handler.process(new Rectangle2D.Float(10, 10, 20, 20), new Color(0, 0, 0), dobj);

        final File scratch = scratchFileOf(dobj);
        assertTrue("redaction should have staged pixels on disk", scratch.exists());
        dobj.releaseScratchFiles();
        assertTrue("scratch pixels should be deleted on release", !scratch.exists());
    }

    // ----------------------------------------------------------------------------------- machinery

    private void redactAndVerify(String resource, Rectangle2D rect, Color fill, String expectedTs)
            throws Exception {
        final RawPixels before = RawPixels.of(resource(resource));
        final File      output = redact(resource, rect, fill);
        final RawPixels after  = RawPixels.of(output);

        assertEquals("transfer syntax should be preserved", expectedTs, after.transferSyntax);
        assertRedacted(before, after, rect, fill, true);
    }

    /**
     * @param exactOutside false for a lossy source, whose pixels outside the rectangle came back
     *                     through a decoder and are only expected to be close, not identical.
     */
    private void assertRedacted(RawPixels before, RawPixels after, Rectangle2D rect, Color fill,
                                boolean exactOutside) {
        final int x0 = (int) Math.max(0, rect.getMinX());
        final int x1 = (int) Math.min(after.columns, rect.getMaxX());
        final int y0 = (int) Math.max(0, rect.getMinY());
        final int y1 = (int) Math.min(after.rows, rect.getMaxY());
        final int[] expectedFill = after.samplesPerPixel == 3
                ? new int[]{fill.getRed(), fill.getGreen(), fill.getBlue()}
                : new int[]{fill.getRed()};

        int redacted = 0;
        int differedBefore = 0;
        for (int frame = 0; frame < after.frames; frame++) {
            for (int y = 0; y < after.rows; y++) {
                for (int x = 0; x < after.columns; x++) {
                    final boolean inside = x >= x0 && x < x1 && y >= y0 && y < y1;
                    for (int sample = 0; sample < after.samplesPerPixel; sample++) {
                        final int actual = after.sample(frame, x, y, sample);
                        if (inside) {
                            assertEquals("frame " + frame + " pixel " + x + "," + y + " sample " + sample
                                         + " should carry the requested fill",
                                         expectedFill[sample], actual);
                        } else if (exactOutside) {
                            assertEquals("frame " + frame + " pixel " + x + "," + y + " sample " + sample
                                         + " is outside the redacted region and must not change",
                                         before.sample(frame, x, y, sample), actual);
                        }
                    }
                    if (inside) {
                        redacted++;
                        for (int sample = 0; sample < after.samplesPerPixel; sample++) {
                            if (before.sample(frame, x, y, sample) != expectedFill[sample]) {
                                differedBefore++;
                            }
                        }
                    }
                }
            }
        }
        assertTrue("the test should have redacted something", redacted > 0);
        // Without this, a rectangle over a region that already held the fill value would satisfy
        // every assertion above whether or not any redaction happened.
        assertTrue("the source already held the fill value throughout the region, so this case "
                   + "would pass without any redaction taking place", differedBefore > 0);
    }

    private File redact(String resource, Rectangle2D rect, Color fill) throws Exception {
        final DicomObjectI dobj = DicomObjectFactory.newInstance(resource(resource),
                                                                 DicomInputStream.IncludeBulkData.URI);
        handler.process(rect, fill, dobj);
        final File output = temporaryFolder.newFile();
        try (OutputStream out = new FileOutputStream(output)) {
            dobj.write(out);
        }
        dobj.releaseScratchFiles();
        return output;
    }

    private static File resource(String name) throws Exception {
        return new File(StreamingRectanglePixelEditHandlerTest.class.getClassLoader().getResource(name).toURI());
    }

    /** The staged pixel file a redaction produced, found through the dataset's bulk data reference. */
    private static File scratchFileOf(DicomObjectI dobj) {
        return ((org.dcm4che3.data.BulkData) dobj.getAttributes().getValue(Tag.PixelData)).getFile();
    }
}
