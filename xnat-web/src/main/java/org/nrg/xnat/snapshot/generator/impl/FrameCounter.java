package org.nrg.xnat.snapshot.generator.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.File;
import java.io.IOException;

/**
 * How many montage slices a DICOM object contributes.
 * <p>
 * {@link SnapshotResourceGeneratorImpl} sums this to decide how many slices a scan has and
 * {@link SliceCoordinateCalculator} walks it to locate them, so the two share one definition rather
 * than each forming its own opinion.
 */
final class FrameCounter {

    private FrameCounter() {}

    /**
     * What one object contributes to a montage: how many slices, and where it belongs in the series.
     *
     * @param frames         montage slices this object holds, zero if it carries no image.
     * @param instanceNumber its Instance Number, or null if it does not say.
     */
    record ObjectHeader(int frames, Integer instanceNumber) {}

    /**
     * The object's Number of Frames, one if it does not say, or none if it carries no image.
     *
     * @throws MizerException if the object cannot be read, since a scan cannot be rendered from a
     *                        count that silently omits part of itself.
     */
    static int framesIn(final File file) throws MizerException {
        return headerOf(file).frames();
    }

    /**
     * The object's frame count and Instance Number, from one read.
     * <p>
     * Instance Number is (0020,0013) and Columns is (0028,0011), so it already falls inside the window
     * this reads for the dimensions: ordering the series costs nothing beyond counting it.
     *
     * @throws MizerException if the object cannot be read.
     */
    static ObjectHeader headerOf(final File file) throws MizerException {
        try (final DicomInputStream input = new DicomInputStream(file)) {
            input.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            // Dimensions rather than the presence of Pixel Data: IncludeBulkData.NO skips that
            // element instead of adding it, so it is absent for every object. Reading through
            // Columns picks up Rows and Columns, which follow Number of Frames in the group.
            final Attributes attributes = input.readDataset(Tag.Columns + 1);
            final Integer instanceNumber = attributes.containsValue(Tag.InstanceNumber)
                                           ? attributes.getInt(Tag.InstanceNumber, 0) : null;
            if (attributes.getInt(Tag.Rows, 0) <= 0 || attributes.getInt(Tag.Columns, 0) <= 0) {
                return new ObjectHeader(0, instanceNumber);
            }
            return new ObjectHeader(Math.max(1, attributes.getInt(Tag.NumberOfFrames, 1)), instanceNumber);
        } catch (IOException e) {
            throw new MizerException("Unable to read a frame count from " + file.getAbsolutePath(), e);
        }
    }
}
