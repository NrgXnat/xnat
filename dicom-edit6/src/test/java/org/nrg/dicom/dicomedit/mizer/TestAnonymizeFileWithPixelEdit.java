package org.nrg.dicom.dicomedit.mizer;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.test.workers.resources.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * Anonymization of a file whose pixels are redacted, which is how the importer arrives.
 * <p>
 * {@link TestSiteAndProjectScriptAnonymization} applies the same kind of script to an object
 * already on the heap. This one goes through the file overload, and so through
 * {@code AnonymizeCallOnFileWithPixels}, which reads bulk data as a reference rather than onto the
 * heap: the redaction is staged in a scratch file that the object still has to read from when it
 * is written back out.
 * <p>
 * That leaves two steps whose order matters and whose failures are both silent. Releasing the
 * scratch file before the write pulls the pixel data out from under it. Never releasing it leaves
 * a file the size of the pixel data behind, once per object. The assertions below stand one on
 * each side of that: pixels read back out of the written file, and an empty scratch directory.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class TestAnonymizeFileWithPixelEdit {

    /** The rectangle SCRIPT redacts, and the value it fills with. */
    private static final int RECT_LEFT = 100, RECT_TOP = 100, RECT_RIGHT = 200, RECT_BOTTOM = 200;
    private static final int FILL_VALUE = 100;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void writesRedactedPixelsBeforeReleasingTheScratchFile() throws Exception {
        final File scratchDirectory = temporaryFolder.newFolder("pixel-edit-scratch");
        final File testFile         = temporaryFolder.newFile("anonymize-me.dcm");
        Files.copy(DICOM_TEST.toPath(), testFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        final Attributes before  = datasetOf(testFile);
        final int        rows    = before.getInt(Tag.Rows, 0);
        final int        columns = before.getInt(Tag.Columns, 0);
        final byte[]     original = before.getBytes(Tag.PixelData);
        // Something has to be there to redact, or the pixel assertions below prove nothing.
        assertNotEquals("fixture already holds the fill value inside the redacted region",
                        FILL_VALUE, valueAt(original, columns, 150, 150));

        final Map<String, Object> elements = new HashMap<>();
        elements.put("subject", "XNAT_01_01");
        final List<MizerContext> contexts = Collections.singletonList(new MizerContextWithScript(0L, SCRIPT, elements));

        final String previous = System.setProperty(SCRATCH_DIR_PROPERTY, scratchDirectory.getAbsolutePath());
        try {
            _service.anonymize(testFile, contexts);
        } finally {
            if (previous == null) {
                System.clearProperty(SCRATCH_DIR_PROPERTY);
            } else {
                System.setProperty(SCRATCH_DIR_PROPERTY, previous);
            }
        }

        final Attributes after     = datasetOf(testFile);
        final byte[]     redacted  = after.getBytes(Tag.PixelData);
        assertEquals("script should have set PatientName from the subject variable",
                     "XNAT_01_01", after.getString(Tag.PatientName));

        // Read back out of the file the service wrote. A release that happened before that write
        // would show up here as pixel data that is short, empty, or still carrying the original.
        assertEquals("pixel data changed length", original.length, redacted.length);
        String firstWrong = null;
        int    wrong      = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                final boolean inside   = x >= RECT_LEFT && x < RECT_RIGHT && y >= RECT_TOP && y < RECT_BOTTOM;
                final int     expected = inside ? FILL_VALUE : valueAt(original, columns, x, y);
                final int     actual   = valueAt(redacted, columns, x, y);
                if (actual != expected) {
                    wrong++;
                    if (firstWrong == null) {
                        firstWrong = String.format("pixel %d,%d is %s the redacted rectangle: expected %d, found %d",
                                                   x, y, inside ? "inside" : "outside", expected, actual);
                    }
                }
            }
        }
        assertNull(wrong + " pixels wrong, first at " + firstWrong, firstWrong);

        // The redaction above could only have been written from a staged file, so this is not
        // vacuous: it is that same file, gone.
        assertEquals("pixel edit scratch files left behind",
                     Collections.emptyList(), Arrays.asList(scratchDirectory.list(STAGED_PIXELS)));
    }

    /** Stored value of one 8-bit pixel. */
    private static int valueAt(final byte[] pixels, final int columns, final int x, final int y) {
        return pixels[y * columns + x] & 0xff;
    }

    private static Attributes datasetOf(final File file) throws IOException {
        try (final DicomInputStream in = new DicomInputStream(file)) {
            in.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES);
            return in.readDataset();
        }
    }

    /** What {@code StreamingRectanglePixelEditHandler.createScratchFile} names the files it stages. */
    private static final FilenameFilter STAGED_PIXELS = (directory, name) -> name.startsWith("pixeledit") && name.endsWith(".pixels");

    /** Read by {@code StreamingRectanglePixelEditHandler}, where it is not visible from this package. */
    private static final String SCRATCH_DIR_PROPERTY = "dicom.pixeledit.scratch.dir";

    private static final String SCRIPT = "version \"6.1\"\n" +
            "(0010,0010) := subject\n" +
            "alterPixels[\"rectangle\", \"l=100, t=100, r=200, b=200\", \"solid\", \"v=100\"]";

    // Uncompressed and 8-bit, so the value the script asks for is the byte that lands in the file,
    // and 970x1552, so the rectangle is well inside it.
    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File            DICOM_TEST       = _resourceManager.getTestResourceFile("dicom/single-frame/US-evle-mono2-8bits.dcm");

    @Autowired
    private MizerService _service;
}
