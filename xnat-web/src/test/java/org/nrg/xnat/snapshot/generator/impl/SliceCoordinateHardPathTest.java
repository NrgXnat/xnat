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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * The branch of {@link SliceCoordinateCalculator} taken when a scan mixes multi-frame and
 * single-frame objects, so neither "one frame per file" nor "one file" holds. It reads the frame
 * count out of each object and walks it, which needs real files -- {@code SliceCoordinateCalculator}
 * tests reach the two arithmetic branches with placeholder names and never come here.
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

        assertEquals("every slice placed, including slice 0",
                     Arrays.asList(new SliceCoordinate(0, 0), new SliceCoordinate(0, 1), new SliceCoordinate(0, 2),
                                   new SliceCoordinate(1, 0), new SliceCoordinate(2, 0)),
                     coordinates);
    }

    /** An object with no image is not a slice, so the montage must never select it. */
    @Test
    public void neverSelectsAnObjectWithNoImage() throws Exception {
        final List<String> files = Arrays.asList(object("first.dcm", null), report("report.dcm"), object("last.dcm", null));

        final List<SliceCoordinate> coordinates = calculator.getSliceCoordinates(2, 2, files);

        assertEquals("the report at index 1 is not a slice",
                     Arrays.asList(new SliceCoordinate(0, 0), new SliceCoordinate(2, 0)), coordinates);
    }

    /** Frames after the last requested slice must not walk off the end of the slice list. */
    @Test
    public void stopsOnceEverySliceHasBeenPlaced() throws Exception {
        final List<String> files = Arrays.asList(object("one.dcm", 3), object("two.dcm", 3));

        final List<SliceCoordinate> coordinates = calculator.getSliceCoordinates(1, 6, files);

        assertEquals(Arrays.asList(new SliceCoordinate(1, 0)), coordinates);
    }

    /**
     * Objects past the last requested slice are never read. On shared storage each one is a round
     * trip, and a single-panel montage selects the midpoint, so half the scan is skipped. The
     * unreadable object stands in for that: reaching it at all fails the montage.
     */
    @Test
    public void doesNotReadObjectsPastTheLastSelectedSlice() throws Exception {
        // Four slices across three files, so neither shortcut applies and the hard path is taken.
        final List<String> files = Arrays.asList(object("multi.dcm", 3), object("single.dcm", null),
                                                 unreadable("never-read.dcm"));

        // One panel of four slices selects slice 2, the last frame of multi.dcm.
        final List<SliceCoordinate> coordinates = calculator.getSliceCoordinates(1, 4, files);

        assertEquals(Collections.singletonList(new SliceCoordinate(0, 2)), coordinates);
    }

    /**
     * Counting frames as the walk reaches each object is a restructure, not a change of result: for
     * every layout it must place exactly the coordinates the eager version placed.
     * <p>
     * Checked against a reference copy of that eager walk over a grid of layouts and panel counts,
     * rather than against coordinates written out by hand, so it covers combinations nobody thought
     * to write a case for.
     */
    @Test
    public void placesTheSameCoordinatesAsTheEagerWalk() throws Exception {
        final int[][] layouts = {
                {3, 1, 1}, {1, 3, 1}, {1, 1, 3}, {2, 2, 1}, {5, 1, 1, 1},
                {1, 1, 1, 4, 1}, {3, 3}, {1, 2, 3, 4}, {4, 0, 4}, {1, 0, 1, 7},
        };
        int compared = 0;
        for (int layout = 0; layout < layouts.length; layout++) {
            final int[]        frames  = layouts[layout];
            final List<String> files   = objects(layout, frames);
            int                nSlices = 0;
            for (final int f : frames) {
                nSlices += f;
            }
            // Guard against the grid drifting into a branch that never reads a file at all.
            assertNotEquals("layout " + layout + " does not reach the hard path", nSlices, files.size());

            for (final int nPanels : new int[]{1, 2, 3, 4, 5, 9, 16}) {
                assertEquals("layout " + Arrays.toString(frames) + " into " + nPanels + " panels",
                             eagerWalk(nPanels, nSlices, files),
                             calculator.getSliceCoordinates(nPanels, nSlices, files));
                compared++;
            }
        }
        assertEquals("every layout and panel count should have been compared", 70, compared);
    }

    /**
     * The walk as it read before, counting every object up front. Kept here deliberately: it is the
     * thing the implementation is being held equal to.
     */
    private List<SliceCoordinate> eagerWalk(final int nPanels, final int nSlices, final List<String> files) throws Exception {
        final List<Integer> sliceNumbers      = calculator.selecctSliceIndices(nPanels, nSlices);
        final List<Integer> frameCountPerFile = new ArrayList<>();
        for (final String f : files) {
            frameCountPerFile.add(FrameCounter.framesIn(new File(f)));
        }
        final List<SliceCoordinate> coordinates = new ArrayList<>();
        int iSlice = 0, iSliceNumber = 0;
        for (int iFile = 0; iFile < files.size() && iSliceNumber < sliceNumbers.size(); iFile++) {
            for (int iFrame = 0; iFrame < frameCountPerFile.get(iFile) && iSliceNumber < sliceNumbers.size(); iFrame++) {
                if (iSlice == sliceNumbers.get(iSliceNumber)) {
                    coordinates.add(new SliceCoordinate(iFile, iFrame));
                    iSliceNumber++;
                }
                iSlice++;
            }
        }
        return coordinates;
    }

    /** One object per entry, of that many frames; zero means an object carrying no image. */
    private List<String> objects(final int layout, final int[] frames) throws Exception {
        final List<String> files = new ArrayList<>();
        for (int i = 0; i < frames.length; i++) {
            final String name = "L" + layout + "-" + i + ".dcm";
            files.add(frames[i] == 0 ? report(name) : object(name, frames[i] == 1 ? null : frames[i]));
        }
        return files;
    }

    /** Not a DICOM object at all, so counting frames in it throws. */
    private String unreadable(final String name) throws Exception {
        final File file = temporaryFolder.newFile(name);
        Files.write(file.toPath(), "not a DICOM object".getBytes(StandardCharsets.UTF_8));
        return file.getAbsolutePath();
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
