package org.nrg.xnat.snapshot.generator.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * The branch of {@link SliceCoordinateCalculator} taken when a scan mixes multi-frame and
 * single-frame objects, so neither "one frame per file" nor "one file" holds.
 * <p>
 * The existing tests pass placeholder names like {@code "foo"}, which only reach the two arithmetic
 * branches, so this path went untested and accumulated four faults: a frame count read through
 * {@code Integer.getInteger}, which resolves a system property rather than parsing a number; a walk
 * that incremented before comparing, making slice 0 unmatchable; an unguarded read past the end of
 * the requested slice list; and objects with no image counted as a frame apiece.
 */
public class SliceCoordinateHardPathTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final SliceCoordinateCalculator calculator = new SliceCoordinateCalculator();

    /** Three frames in one object then two single-frame objects: five slices across three files. */
    @Test
    public void mapsEverySliceAcrossMixedObjects() throws Exception {
        final List<String> files = Arrays.asList(object("multi.dcm", 3), object("a.dcm", null), object("b.dcm", null));

        final List<SliceCoordinate> coordinates = calculator.getSliceCoordinates(5, 5, files);

        assertEquals("slice 0 has to be reachable; incrementing before the compare lost it",
                     Arrays.asList(new SliceCoordinate(0, 0), new SliceCoordinate(0, 1), new SliceCoordinate(0, 2),
                                   new SliceCoordinate(1, 0), new SliceCoordinate(2, 0)),
                     coordinates);
    }

    /** An object with no image is not a slice, so the montage must never select it. */
    @Test
    public void neverSelectsAnObjectWithNoImage() throws Exception {
        final List<String> files = Arrays.asList(object("first.dcm", null), report("report.dcm"), object("last.dcm", null));

        final List<SliceCoordinate> coordinates = calculator.getSliceCoordinates(2, 2, files);

        assertEquals("the report sits at index 1 and has no pixels to render",
                     Arrays.asList(new SliceCoordinate(0, 0), new SliceCoordinate(2, 0)), coordinates);
    }

    /** Frames after the last requested slice must not walk off the end of the slice list. */
    @Test
    public void stopsOnceEverySliceHasBeenPlaced() throws Exception {
        final List<String> files = Arrays.asList(object("one.dcm", 3), object("two.dcm", 3));

        final List<SliceCoordinate> coordinates = calculator.getSliceCoordinates(1, 6, files);

        assertEquals(Arrays.asList(new SliceCoordinate(1, 0)), coordinates);
    }

    private String object(final String name, final Integer frames) throws Exception {
        final Attributes dataset = base(name, UID.SecondaryCaptureImageStorage);
        dataset.setInt(Tag.Rows, VR.US, 4);
        dataset.setInt(Tag.Columns, VR.US, 4);
        dataset.setInt(Tag.BitsAllocated, VR.US, 8);
        if (frames != null) {
            dataset.setInt(Tag.NumberOfFrames, VR.IS, frames);
        }
        return write(name, dataset);
    }

    /** No Rows, Columns or pixel data, as a structured report has. */
    private String report(final String name) throws Exception {
        return write(name, base(name, UID.ComprehensiveSRStorage));
    }

    private static Attributes base(final String name, final String sopClass) {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, sopClass);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498." + Math.abs(name.hashCode()));
        return dataset;
    }

    private String write(final String name, final Attributes dataset) throws Exception {
        final File file = temporaryFolder.newFile(name);
        try (final DicomOutputStream out = new DicomOutputStream(file)) {
            out.writeDataset(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset);
        }
        return file.getAbsolutePath();
    }
}
