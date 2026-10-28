/*
 * DicomEdit: EmptyType2ElementTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.dicomedit;

import org.dcm4che3.data.Tag;
import org.junit.Test;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSeverity;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * XNAT-8499 follow-up: show that the null-variable check added to
 * {@link DicomEditParseTreeVisitor#extractVariableValue(String)} does not misfire on a
 * DICOM type 2 element that is present but zero-length.
 *
 * <p>Type 2 elements are <em>required to be present</em> but are permitted to be empty, and
 * real scanners emit them that way routinely. The new check throws on a null {@code Value},
 * not an empty one, so an empty type 2 element must still anonymize cleanly — if it did not,
 * this change would start failing imports on ordinary, conformant data.
 *
 * <p>The object is crafted rather than loaded from a fixture: a real test object with
 * {@code PatientName (0010,0010)} overwritten with a zero-length value.
 * {@link #craftedObjectIsAValidEmptyType2()} asserts that precondition so the rest of the
 * class cannot pass vacuously against a mis-crafted object.
 *
 * <p>{@link #aGenuinelyNullVariableStillThrows()} is a positive control: it proves this
 * harness can in fact observe the null-variable failure, so the passing assertions above it
 * mean something rather than reflecting a blind test.
 *
 * <p>Note these are guard tests, not regression tests: the empty-element cases also pass
 * against the pre-fix code, where no such check existed. What they pin is that the check
 * added here did not narrow what counts as valid input.
 */
public class EmptyType2ElementTests {

    private static final ResourceManager RESOURCES = ResourceManager.getInstance();

    private static final File DICOM_FILE = RESOURCES.getTestResourceFile(
            "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    /** Original SeriesDescription of the test object, used to detect a skipped assignment. */
    private static final String ORIGINAL_SERIES_DESCRIPTION = "t1_mpr_1mm_p2_pos50";

    private static final String READ_VIA_VARIABLE =
            "version \"6.1\"\npn := (0010,0010)\n(0008,103e) := pn\n";
    private static final String READ_DIRECTLY =
            "version \"6.1\"\n(0008,103e) := (0010,0010)\n";
    private static final String READ_INSIDE_FUNCTION =
            "version \"6.1\"\n(0008,103e) := format[\"x{0}y\", (0010,0010)]\n";

    /** PatientName present with a zero-length value: a conformant type 2 element. */
    private DicomObjectI withEmptyPatientName() throws Exception {
        final DicomObjectI object = DicomObjectFactory.newInstance(DICOM_FILE);
        object.putString(Tag.PatientName, "");
        return object;
    }

    /** PatientName removed entirely — NOT valid for a type 2 element; contrast case only. */
    private DicomObjectI withAbsentPatientName() throws Exception {
        final DicomObjectI object = DicomObjectFactory.newInstance(DICOM_FILE);
        object.delete(Tag.PatientName);
        return object;
    }

    private AnonymizationResult apply(final String script, final DicomObjectI object) throws Exception {
        return BaseScriptApplicator
                .getInstance(new ByteArrayInputStream(script.getBytes()))
                .apply(object);
    }

    private static void assertSucceeded(final AnonymizationResult result) {
        assertEquals("expected clean anonymization, got: " + result.getMessage(),
                AnonymizationResultSeverity.SUCCESS, result.getSeverity());
        assertFalse("empty type 2 element must not trip the null-variable check, but got: "
                    + result.getMessage(),
                result.getMessage().contains("is null"));
    }

    @Test
    public void craftedObjectIsAValidEmptyType2() throws Exception {
        final DicomObjectI object = withEmptyPatientName();
        assertTrue("PatientName must be present for a type 2 element", object.contains(Tag.PatientName));
        assertTrue("PatientName must be zero-length", object.isEmpty(Tag.PatientName));
        assertEquals("", object.getString(Tag.PatientName));
    }

    @Test
    public void emptyType2ReadIntoAVariableAnonymizesCleanly() throws Exception {
        final AnonymizationResult result = apply(READ_VIA_VARIABLE, withEmptyPatientName());
        assertSucceeded(result);
        assertEquals("the empty value should propagate as empty",
                "", result.getDicomObject().getString(Tag.SeriesDescription));
    }

    @Test
    public void emptyType2AssignedDirectlyAnonymizesCleanly() throws Exception {
        final AnonymizationResult result = apply(READ_DIRECTLY, withEmptyPatientName());
        assertSucceeded(result);
        assertEquals("", result.getDicomObject().getString(Tag.SeriesDescription));
    }

    @Test
    public void emptyType2InsideAFunctionInterpolatesAsEmpty() throws Exception {
        final AnonymizationResult result = apply(READ_INSIDE_FUNCTION, withEmptyPatientName());
        assertSucceeded(result);
        // "xy", not "xnully" — the empty value must not stringify as "null".
        assertEquals("xy", result.getDicomObject().getString(Tag.SeriesDescription));
    }

    @Test
    public void absentElementSkipsTheAssignmentRatherThanFailing() throws Exception {
        // Contrast case, documenting today's behavior: a missing source element makes the
        // assignment a no-op and still reports SUCCESS, leaving the target as it was.
        final AnonymizationResult result = apply(READ_DIRECTLY, withAbsentPatientName());
        assertSucceeded(result);
        assertEquals(ORIGINAL_SERIES_DESCRIPTION,
                result.getDicomObject().getString(Tag.SeriesDescription));
    }

    @Test
    public void aGenuinelyNullVariableStillThrows() throws Exception {
        // Positive control: proves this harness can observe the null-variable failure, so the
        // SUCCESS assertions above are not vacuous.
        final AnonymizationResult result = apply(
                "version \"6.1\"\nPatientName := \"Anonymized\"\n(0010,0010) := patientName\n",
                withEmptyPatientName());
        assertEquals(AnonymizationResultSeverity.ERROR, result.getSeverity());
        assertTrue("control should report the null variable by name, got: " + result.getMessage(),
                result.getMessage().contains("Variable 'patientName' is null."));
    }
}
