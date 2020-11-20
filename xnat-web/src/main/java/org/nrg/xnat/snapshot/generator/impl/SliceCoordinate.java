package org.nrg.xnat.snapshot.generator.impl;

import java.util.Objects;

/**
 * Account for the fact that the slices of a scan can (theoretically) be divided amongst multiple multi-frame files.
 * Count from 0.
 *
 * TODO: Save the URI of the file instead of an index into an array that could be god knows where.
 */
public class SliceCoordinate {
    // index into a list of files containing the file of interest.
    private final int fileNumber;
    // index of the frame within the file.
    private final int frameNumber;

    public SliceCoordinate( int fileNumber, int frameNumber) {
        this.fileNumber = fileNumber;
        this.frameNumber = frameNumber;
    }

    public int getFileNumber() { return fileNumber;}
    public int getFrameNumber() { return frameNumber;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SliceCoordinate that = (SliceCoordinate) o;
        return fileNumber == that.fileNumber &&
                frameNumber == that.frameNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileNumber, frameNumber);
    }
}
