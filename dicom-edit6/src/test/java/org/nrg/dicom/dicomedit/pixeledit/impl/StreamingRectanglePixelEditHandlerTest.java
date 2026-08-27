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
import org.nrg.dicom.dicomedit.BaseScriptApplicator;
import org.nrg.dicom.dicomedit.pixels.impl.StreamingRectanglePixelEditHandler;
import org.nrg.dicom.mizer.exceptions.MizerException;
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
import static org.junit.Assert.fail;

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

    @Test
    public void redactsSubSampledLossyJpeg() throws Exception {
        // The only guard against the sub-sampled refusal reaching compressed input.
        // YBR_FULL_422 JPEG baseline. Two luma samples share a chroma pair in the compressed form,
        // which the byte arithmetic could not address, but decoding rewrites
        // PhotometricInterpretation to RGB and expands the samples, so what reaches the redaction is
        // three whole samples to a pixel. The refusal in refusesSubSampledNativePixelData must not
        // fire here.
        final Rectangle2D rect   = new Rectangle2D.Float(100, 100, 60, 60);
        final Color       fill   = new Color(180, 90, 30);
        final String      source = "dicom/single-frame/US-jpg-ybr-8bits.dcm";
        final RawPixels   result = RawPixels.of(redact(source, rect, fill));

        assertEquals("lossy input should be stored uncompressed",
                     UID.ExplicitVRLittleEndian, result.transferSyntax);
        assertEquals("the decoded object should carry whole samples per pixel",
                     "RGB", result.dataset.getString(Tag.PhotometricInterpretation));
        assertRedacted(RawPixels.of(resource(source)), result, rect, fill, false);
    }

    // ------------------------------------------------------------------------------------ edge cases

    @Test
    public void leavesImageAloneWhenRectangleMissesIt() throws Exception {
        final String source = "dicom/single-frame/US-evle-rgb-8bits.dcm";
        // Clear of the image in both axes, then in one axis at a time. The one-axis cases are not
        // decoration: PixelGeometry.clip clamps width and height with separate expressions, and a
        // rectangle that still overlaps in the other axis has an in-range origin to write from, so
        // a clip that produced a positive width would corrupt real pixels rather than land
        // harmlessly off the end of the frame.
        for (final Rectangle2D rect : new Rectangle2D[]{new Rectangle2D.Float(5000, 5000, 10, 10),
                                                        new Rectangle2D.Float(5000, 10, 10, 10),
                                                        new Rectangle2D.Float(10, 5000, 10, 10)}) {
            final File      output = redact(source, rect, new Color(255, 0, 0));
            final RawPixels before = RawPixels.of(resource(source));
            final RawPixels after  = RawPixels.of(output);
            assertEquals(before.length(), after.length());
            for (int y = 0; y < before.rows; y++) {
                for (int x = 0; x < before.columns; x++) {
                    for (int sample = 0; sample < before.samplesPerPixel; sample++) {
                        assertEquals(rect + ": pixel " + x + "," + y + " sample " + sample
                                     + " should be untouched",
                                     before.sample(0, x, y, sample), after.sample(0, x, y, sample));
                    }
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

    /**
     * Pixel data that runs past the frame count the object declares must still be redacted.
     * <p>
     * Those bytes used to be copied through untouched, on the reasoning that anything beyond
     * NumberOfFrames was not ours to interpret. That is right for an odd-length value's pad byte and
     * wrong for an object carrying more frames than it admits to: they reached the archive with
     * whatever was burned into them. Failing to redact is the one outcome this code should never
     * choose quietly.
     */
    @Test
    public void redactsFramesBeyondTheDeclaredCount() throws Exception {
        final int side = 8, declared = 2, actual = 3;
        final byte[] pixels = new byte[side * side * actual];
        java.util.Arrays.fill(pixels, (byte) 0xAB);

        final File source = temporaryFolder.newFile("undeclared-frames.dcm");
        final Attributes ds = new Attributes();
        ds.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        ds.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.7001");
        ds.setInt(Tag.Rows, VR.US, side);
        ds.setInt(Tag.Columns, VR.US, side);
        ds.setInt(Tag.SamplesPerPixel, VR.US, 1);
        ds.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        ds.setInt(Tag.BitsAllocated, VR.US, 8);
        ds.setInt(Tag.BitsStored, VR.US, 8);
        ds.setInt(Tag.HighBit, VR.US, 7);
        ds.setInt(Tag.PixelRepresentation, VR.US, 0);
        ds.setInt(Tag.NumberOfFrames, VR.IS, declared);   // understates what follows
        ds.setBytes(Tag.PixelData, VR.OB, pixels);
        try (DicomOutputStream out = new DicomOutputStream(source)) {
            out.writeDataset(ds.createFileMetaInformation(UID.ExplicitVRLittleEndian), ds);
        }

        final DicomObjectI dobj = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        handler.process(new Rectangle2D.Float(0, 0, side, side), new Color(7, 7, 7), dobj);
        final File output = temporaryFolder.newFile("undeclared-frames-out.dcm");
        try (OutputStream out = new FileOutputStream(output)) {
            dobj.write(out);
        }
        dobj.releaseScratchFiles();

        final byte[] after;
        try (DicomInputStream in = new DicomInputStream(output)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES);
            after = in.readDataset().getBytes(Tag.PixelData);
        }
        assertEquals("every byte of pixel data should still be there", pixels.length, after.length);
        for (int at = 0; at < after.length; at++) {
            assertEquals("byte " + at + " (frame " + (at / (side * side)) + ") should be redacted, "
                         + "including the frame past NumberOfFrames", 7, after[at] & 0xFF);
        }
    }

    /**
     * A frame far too large to hold in memory is redacted rather than refused.
     * <p>
     * What a redaction buffers is a line, not a frame, so the size of a frame does not bound what
     * can be edited: only one line has to fit in memory, and since Rows and Columns are US that is
     * a few hundred kilobytes at worst however large the image. This object declares a 65535x65535
     * image -- a frame of nearly 4 GiB -- and carries only its first few rows of pixel data, which
     * is what makes the case cheap enough to have a test. Buffering a frame refused this outright.
     */
    @Test
    public void redactsAnImageWhoseFrameIsTooLargeToBuffer() throws Exception {
        final int    side = 65535, storedLines = 4, width = 100, height = 2;
        final byte[] pixels = new byte[side * storedLines];
        java.util.Arrays.fill(pixels, (byte) 0xAB);

        final File       source = temporaryFolder.newFile("huge-frame.dcm");
        final Attributes ds     = new Attributes();
        ds.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        ds.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.7003");
        ds.setInt(Tag.Rows, VR.US, side);
        ds.setInt(Tag.Columns, VR.US, side);
        ds.setInt(Tag.SamplesPerPixel, VR.US, 1);
        ds.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        ds.setInt(Tag.BitsAllocated, VR.US, 8);
        ds.setInt(Tag.BitsStored, VR.US, 8);
        ds.setInt(Tag.HighBit, VR.US, 7);
        ds.setInt(Tag.PixelRepresentation, VR.US, 0);
        ds.setBytes(Tag.PixelData, VR.OB, pixels);
        try (DicomOutputStream out = new DicomOutputStream(source)) {
            out.writeDataset(ds.createFileMetaInformation(UID.ExplicitVRLittleEndian), ds);
        }

        final DicomObjectI dobj = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        handler.process(new Rectangle2D.Float(0, 0, width, height), new Color(7, 7, 7), dobj);
        final File output = temporaryFolder.newFile("huge-frame-out.dcm");
        try (OutputStream out = new FileOutputStream(output)) {
            dobj.write(out);
        }
        dobj.releaseScratchFiles();

        final byte[] after;
        try (DicomInputStream in = new DicomInputStream(output)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES);
            after = in.readDataset().getBytes(Tag.PixelData);
        }
        assertEquals("every byte of pixel data should still be there", pixels.length, after.length);
        for (int at = 0; at < after.length; at++) {
            final int row      = at / side;
            final int column   = at % side;
            final int expected = row < height && column < width ? 7 : 0xAB;
            assertEquals("byte " + at + " (row " + row + ", column " + column + ")",
                         expected, after[at] & 0xFF);
        }
    }

    /**
     * Colour-by-plane pixel data spanning more than one frame is redacted in every plane of every
     * frame.
     * <p>
     * Buffering a line rather than a frame means the redactor is handed a line at a time and works
     * out the plane and the row from which line of the frame it is, so the point where that count
     * wraps from the last plane of one frame to the first plane of the next is the one place a
     * rectangle can now land in the wrong frame. Nothing else covers it: the multi-frame fixtures
     * are PALETTE COLOR, one sample to a pixel, and the planar fixture has a single frame, so this
     * object is built here.
     */
    @Test
    public void redactsEveryPlaneOfEveryFrameWhenColourIsByPlane() throws Exception {
        final int    side = 4, samples = 3, frameCount = 2;
        final byte[] pixels = new byte[side * side * samples * frameCount];
        for (int at = 0; at < pixels.length; at++) {
            // Varied deliberately, and clear of the fill values: a run written to the wrong plane,
            // row or frame then shows up as a changed byte instead of coinciding with what was
            // already there.
            pixels[at] = (byte) (0x80 + at % 0x40);
        }

        final File       source = temporaryFolder.newFile("planar-multi-frame.dcm");
        final Attributes ds     = new Attributes();
        ds.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        ds.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.7004");
        ds.setInt(Tag.Rows, VR.US, side);
        ds.setInt(Tag.Columns, VR.US, side);
        ds.setInt(Tag.SamplesPerPixel, VR.US, samples);
        ds.setString(Tag.PhotometricInterpretation, VR.CS, "RGB");
        ds.setInt(Tag.PlanarConfiguration, VR.US, 1);
        ds.setInt(Tag.BitsAllocated, VR.US, 8);
        ds.setInt(Tag.BitsStored, VR.US, 8);
        ds.setInt(Tag.HighBit, VR.US, 7);
        ds.setInt(Tag.PixelRepresentation, VR.US, 0);
        ds.setInt(Tag.NumberOfFrames, VR.IS, frameCount);
        ds.setBytes(Tag.PixelData, VR.OB, pixels);
        try (DicomOutputStream out = new DicomOutputStream(source)) {
            out.writeDataset(ds.createFileMetaInformation(UID.ExplicitVRLittleEndian), ds);
        }

        final Rectangle2D  rect = new Rectangle2D.Float(1, 1, 2, 2);
        final Color        fill = new Color(10, 20, 30);
        final DicomObjectI dobj = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        handler.process(rect, fill, dobj);
        final File output = temporaryFolder.newFile("planar-multi-frame-out.dcm");
        try (OutputStream out = new FileOutputStream(output)) {
            dobj.write(out);
        }
        dobj.releaseScratchFiles();

        assertRedacted(RawPixels.of(source), RawPixels.of(output), rect, fill, true);
    }

    /**
     * Sub-sampled pixel data that was never compressed is redacted where it was asked for.
     * <p>
     * YBR_FULL_422 carries one chroma pair for every two luma samples, stored Y Y Cb Cr, so a row is
     * {@code columns * 2} bytes rather than the {@code columns * 3} that whole pixels would take, and
     * a pixel is not a fixed run of bytes. Addressing it as though it were puts the rectangle about
     * a third of the way down from where it was asked for and leaves the value length untouched, so
     * the object would reach the archive reporting a redaction that did not happen.
     * <p>
     * The layout is dcm4che's: {@code ColorSubsampling.YBR_XXX_422.indexOfY(x, y, columns)} is
     * {@code ((columns * y + x) * 2) - x % 2} and {@code indexOfBR} lands on the third byte of the
     * pair, which is what this pins down. The rectangle here starts on an odd column on purpose:
     * half a chroma pair cannot be given a new colour on its own, so the fill has to round outwards
     * onto column 2, and over-redacting is the safe direction to err in.
     */
    @Test
    public void redactsSubSampledNativePixelData() throws Exception {
        final int    columns = 8, rows = 8, lineLength = columns * 2;
        final byte[] pixels  = pattern(lineLength * rows);
        final File   source  = nativeObject("ybr-422.dcm", pixels, "YBR_FULL_422", rows, columns, 3, 0);

        // rgb(180,90,30) is Y=110 Cb=82 Cr=177 in full-range YBR, which is dcm4che's own conversion.
        final byte[] after = redactBytes(source, new Rectangle2D.Float(3, 2, 3, 2), new Color(180, 90, 30));
        final int[]  group = {110, 110, 82, 177};

        assertEquals("the value length must not change", pixels.length, after.length);
        for (int at = 0; at < after.length; at++) {
            final int row    = at / lineLength;
            final int within = at % lineLength;
            // Columns 3 to 5 were asked for; groups 1 and 2 cover columns 2 to 5.
            final int expected = row >= 2 && row < 4 && within >= 4 && within < 12
                                 ? group[within % 4]
                                 : pixels[at] & 0xFF;
            assertEquals("byte " + at + " (row " + row + ", byte " + within + " of the row)",
                         expected, after[at] & 0xFF);
        }
    }

    /**
     * A black fill on YBR pixel data is stored as black, not as green.
     * <p>
     * Y, Cb and Cr are not R, G and B: writing {@code v=0} into them unconverted stores Y=0 Cb=0
     * Cr=0, which renders as a medium green rather than the black the script asked for. The pixelmed
     * handler this replaces decoded to RGB and wrote real black, so this is the guard against
     * regressing on that. Compressed YBR objects come back RGB from the decoder and are unaffected,
     * which is why this case has to be built natively.
     */
    @Test
    public void writesABlackFillAsBlackOnYbrPixelData() throws Exception {
        final int    columns = 4, rows = 4, lineLength = columns * 3;
        final byte[] pixels  = pattern(lineLength * rows);
        final File   source  = nativeObject("ybr-full.dcm", pixels, "YBR_FULL", rows, columns, 3, 0);

        final byte[] after = redactBytes(source, new Rectangle2D.Float(0, 0, 2, 1), new Color(0, 0, 0));
        // Black is Y=0, Cb=128, Cr=128. Cb and Cr sit at the middle of their range, not at zero.
        final int[] blackPixel = {0, 128, 128};

        for (int at = 0; at < after.length; at++) {
            final int expected = at < 6 ? blackPixel[at % 3] : pixels[at] & 0xFF;
            assertEquals("byte " + at, expected, after[at] & 0xFF);
        }
    }

    /**
     * Pixel data whose chroma is shared between rows is refused.
     * <p>
     * YBR_PARTIAL_420 sub-samples vertically as well as horizontally, so a row cannot be recoloured
     * without touching its neighbour and redacting a line at a time cannot address it. The standard
     * permits it only with the MPEG transfer syntaxes, so an object carrying it uncompressed is
     * malformed; refusing beats guessing at a layout, and unlike 422 there is no rounding that makes
     * it safe.
     */
    @Test
    public void refusesPixelDataWhoseChromaIsSharedBetweenRows() throws Exception {
        final int  columns = 4, rows = 4;
        final File source  = nativeObject("ybr-420.dcm", pattern(columns * rows / 2 * 3),
                                          "YBR_PARTIAL_420", rows, columns, 3, 0);
        final DicomObjectI dobj = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        try {
            handler.process(new Rectangle2D.Float(0, 0, 2, 2), new Color(0, 0, 0), dobj);
            fail("pixel data with chroma shared between rows should be refused");
        } catch (MizerException expected) {
            assertTrue("the failure should say why, was: " + expected.getMessage(),
                       expected.getMessage().contains("shares chroma samples between rows"));
        }
    }

    /** Varied source bytes, clear of every fill value these tests use, so a stray write shows up. */
    private static byte[] pattern(int length) {
        final byte[] pixels = new byte[length];
        for (int at = 0; at < length; at++) {
            pixels[at] = (byte) (0x10 + at % 0x40);
        }
        return pixels;
    }

    /** An eight-bit Explicit VR Little Endian object with the given pixel module. */
    private File nativeObject(String name, byte[] pixels, String photometricInterpretation,
                              int rows, int columns, int samplesPerPixel, int planarConfiguration)
            throws Exception {
        final File       source = temporaryFolder.newFile(name);
        final Attributes ds     = new Attributes();
        ds.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        ds.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.7005");
        ds.setInt(Tag.Rows, VR.US, rows);
        ds.setInt(Tag.Columns, VR.US, columns);
        ds.setInt(Tag.SamplesPerPixel, VR.US, samplesPerPixel);
        ds.setString(Tag.PhotometricInterpretation, VR.CS, photometricInterpretation);
        ds.setInt(Tag.PlanarConfiguration, VR.US, planarConfiguration);
        ds.setInt(Tag.BitsAllocated, VR.US, 8);
        ds.setInt(Tag.BitsStored, VR.US, 8);
        ds.setInt(Tag.HighBit, VR.US, 7);
        ds.setInt(Tag.PixelRepresentation, VR.US, 0);
        ds.setBytes(Tag.PixelData, VR.OB, pixels);
        try (DicomOutputStream out = new DicomOutputStream(source)) {
            out.writeDataset(ds.createFileMetaInformation(UID.ExplicitVRLittleEndian), ds);
        }
        return source;
    }

    /** Redacts a file and returns the pixel data it wrote. */
    private byte[] redactBytes(File source, Rectangle2D rect, Color fill) throws Exception {
        final DicomObjectI dobj = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        handler.process(rect, fill, dobj);
        final File output = temporaryFolder.newFile(source.getName() + "-out.dcm");
        try (OutputStream out = new FileOutputStream(output)) {
            dobj.write(out);
        }
        dobj.releaseScratchFiles();
        try (DicomInputStream in = new DicomInputStream(output)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES);
            return in.readDataset().getBytes(Tag.PixelData);
        }
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
