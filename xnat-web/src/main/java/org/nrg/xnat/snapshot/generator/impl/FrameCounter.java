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
     * The object's Number of Frames, one if it does not say, or none if it carries no image.
     *
     * @throws MizerException if the object cannot be read, since a scan cannot be rendered from a
     *                        count that silently omits part of itself.
     */
    static int framesIn(final File file) throws MizerException {
        try (final DicomInputStream input = new DicomInputStream(file)) {
            input.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            // Dimensions rather than the presence of Pixel Data: IncludeBulkData.NO skips that
            // element instead of adding it, so it is absent for every object. Reading through
            // Columns picks up Rows and Columns, which follow Number of Frames in the group.
            final Attributes attributes = input.readDataset(Tag.Columns + 1);
            if (attributes.getInt(Tag.Rows, 0) <= 0 || attributes.getInt(Tag.Columns, 0) <= 0) {
                return 0;
            }
            return Math.max(1, attributes.getInt(Tag.NumberOfFrames, 1));
        } catch (IOException e) {
            throw new MizerException("Unable to read a frame count from " + file.getAbsolutePath(), e);
        }
    }
}
