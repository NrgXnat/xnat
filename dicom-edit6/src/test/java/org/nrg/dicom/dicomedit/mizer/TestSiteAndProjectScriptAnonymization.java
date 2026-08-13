package org.nrg.dicom.dicomedit.mizer;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.test.workers.resources.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;

/**
 * A site script and a project script applied together through the mizer service.
 * <p>
 * Between them they rewrite header elements and redact a rectangle of pixels, so this covers the
 * whole of what a real anonymization does and the order the contexts are applied in: both scripts
 * assign Manufacturer, and the project script's value has to win.
 * <p>
 * The object is loaded with bulk data on the heap, so the pixel edit runs over a {@code byte[]}
 * rather than a {@link BulkData} reference. That is the other half of
 * {@code StreamingRectanglePixelEditHandlerTest}, which exercises the reference path throughout.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class TestSiteAndProjectScriptAnonymization {

    /** The rectangle SCRIPT_PROJ redacts, and the value it fills with. */
    private static final int RECT_LEFT = 100, RECT_TOP = 100, RECT_RIGHT = 200, RECT_BOTTOM = 200;
    private static final int FILL_VALUE = 100;

    @Test
    public void appliesSiteAndProjectScripts() throws MizerException, IOException {
        final DicomObjectI dicomObject = DicomObjectFactory.newInstance(DICOM_TEST);

        assertEquals("head^DHead", dicomObject.getString(Tag.StudyDescription));
        assertEquals("Sample Patient", dicomObject.getString(Tag.PatientName));
        assertEquals("Sample ID", dicomObject.getString(Tag.PatientID));
        assertEquals("SIEMENS", dicomObject.getString(Tag.Manufacturer));
        assertEquals("Hospital", dicomObject.getString(Tag.InstitutionName));

        final int columns = dicomObject.getAttributes().getInt(Tag.Columns, 0);
        final byte[] before = readPixelData(dicomObject);
        // Something has to be there to redact, or the pixel assertions below prove nothing.
        assertNotEquals("fixture already holds the fill value inside the redacted region",
                        FILL_VALUE, valueAt(before, columns, 150, 150));

        final Map<String, Object> elements = new HashMap<>();
        elements.put("project", "XNAT_01");
        elements.put("subject", "XNAT_01_01");
        elements.put("modalityLabel", "MR");
        final List<MizerContext> contexts  = Arrays.asList(new MizerContextWithScript(0L, SCRIPT_SITE, elements), new MizerContextWithScript(0L, SCRIPT_PROJ, elements));
        final Set<Variable> variables = _service.getReferencedVariables(contexts);
        assertNotNull(variables);
        assertEquals(4, variables.size());

        try {
            _service.anonymize(dicomObject, contexts);

            // Header elements each script assigns.
            assertEquals("site script should have set StudyDescription from the project variable",
                         "XNAT_01", dicomObject.getString(Tag.StudyDescription));
            assertEquals("site script should have set PatientName from the subject variable",
                         "XNAT_01_01", dicomObject.getString(Tag.PatientName));
            assertEquals("project script should have set InstitutionName",
                         PROJ_TAG, dicomObject.getString(Tag.InstitutionName));
            assertEquals("project script should have set PatientID from its session variable",
                         "XNAT_01_01_MR1", dicomObject.getString(Tag.PatientID));
            // Both scripts assign Manufacturer. The project script runs second, so it wins -- this
            // is what pins the order the contexts are applied in.
            assertEquals("the project script's Manufacturer should have overwritten the site script's",
                         "MR", dicomObject.getString(Tag.Manufacturer));

            // The redaction the project script asks for.
            final byte[] after = readPixelData(dicomObject);
            assertEquals("pixel data changed length", before.length, after.length);
            for (int y = 0; y < dicomObject.getAttributes().getInt(Tag.Rows, 0); y++) {
                for (int x = 0; x < columns; x++) {
                    final boolean inside = x >= RECT_LEFT && x < RECT_RIGHT && y >= RECT_TOP && y < RECT_BOTTOM;
                    if (inside) {
                        assertEquals(String.format("pixel %d,%d is inside the redacted rectangle", x, y),
                                     FILL_VALUE, valueAt(after, columns, x, y));
                    } else {
                        assertEquals(String.format("pixel %d,%d is outside it and must not change", x, y),
                                     valueAt(before, columns, x, y), valueAt(after, columns, x, y));
                    }
                }
            }
        } finally {
            dicomObject.releaseScratchFiles();
        }
    }

    /** Stored value of one 16-bit little endian pixel. */
    private static int valueAt(final byte[] pixels, final int columns, final int x, final int y) {
        final int offset = (y * columns + x) * 2;
        return (pixels[offset] & 0xff) | ((pixels[offset + 1] & 0xff) << 8);
    }

    /**
     * Pixel data as bytes, however it is currently held: on the heap as read, or as a reference
     * into the file a pixel edit staged it in.
     */
    private static byte[] readPixelData(final DicomObjectI dicomObject) throws IOException {
        final Object value = dicomObject.getAttributes().getValue(Tag.PixelData);
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        if (value instanceof BulkData) {
            try (InputStream in = ((BulkData) value).openStream()) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final byte[] buffer = new byte[1 << 16];
                for (int read; (read = in.read(buffer)) > 0; ) {
                    out.write(buffer, 0, read);
                }
                return out.toByteArray();
            }
        }
        throw new IOException("no readable pixel data, found " + value);
    }

    private static final String SITE_TAG        = "DicomEdit 6 site anonymization";
    private static final String PROJ_TAG        = "DicomEdit 6 XNAT 01 project anonymization";
    private static final String SCRIPT_SITE     = "version \"6.1\"\n" +
            "(0008,0070) := \"" + SITE_TAG + "\"\n" +
            "project != \"Unassigned\" ? (0008,1030) := project\n" +
            "(0008,1030) := project\n" +
            "(0010,0010) := subject";
    private static final String SCRIPT_PROJ     = "version \"6.1\"\n" +
            "(0008,0080) := \"" + PROJ_TAG + "\"\n" +
            "session := \"XNAT_01_01_MR1\"\n" +
            "(0008,0070) := modalityLabel\n" +
            "alterPixels[\"rectangle\", \"l=100, t=100, r=200, b=200\", \"solid\", \"v=100\"] \n" +
            "(0010,0020) := session";

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File DICOM_TEST       = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    @Autowired
    private MizerService _service;
}
