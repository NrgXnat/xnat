package org.nrg.xnat.snapshot.generator.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide the ability to intelligently select a subset of slices from an assumed-to-be-sorted list to fill montage panels.
 * Select m evenly spaced indices from a list of n.
 */
public class SliceCoordinateCalculator {

    /**
     * Return the list of slice numbers chosen to fill the number of panels.
     *
     * Slice index counts from 0.
     *
     * @param nSlices
     * @param nPanels
     * @return
     */
    public List<Integer> selecctSliceIndices(int nPanels, int nSlices) {
        List<Integer> sliceNumber = new ArrayList<>();
        if( nSlices > 0 && nPanels > 0) {
            nPanels = Math.min( nSlices, nPanels);
            int m = nSlices / nPanels;
            int b = nSlices / ( 2 * nPanels);
            for( int i = 0; i < nPanels; i++) {
                sliceNumber.add( m * i + b );
            }
        }
        return sliceNumber;
    }

    /**
     * Return the list of slice coordinates of the slice numbers chosen for the panels.
     * @param nSlices
     * @param nPanels
     * @return
     */
    public List<SliceCoordinate> getSliceCoordinates(int nPanels, int nSlices, List<String> files) throws MizerException {
        List<Integer> sliceNumbers = selecctSliceIndices( nPanels, nSlices);
        List<SliceCoordinate> coordinates = new ArrayList<>();
        // All files are single framed.
        if( nSlices == files.size()) {
            for( int sliceNumber: sliceNumbers) {
                coordinates.add( new SliceCoordinate( sliceNumber, 0));
            }
        }
        // All slices are in a single multi-frame file.
        else if( files.size() == 1) {
            for( int sliceNumber: sliceNumbers) {
                coordinates.add( new SliceCoordinate( 0, sliceNumber));
            }
        }
        // All bets are off. Go figure it out.
        else {
            coordinates.addAll( getSliceCoordinatesTheHardWay( sliceNumbers, files));
        }
        return coordinates;
    }

    /**
     * Yeah well, the scan catalog doesn't record the number of frames per instance so we read them all.
     * TODO: Ideally we fix the scan catalog but it is supposed to be going away soon.....
     *
     * Assumes the files are ordered by instance number and the frames stack up in order. Ideally we sort the frames
     * by their z coordinates.
     *
     * @param sliceNumbers
     * @return
     * @throws MizerException
     */
    private List<SliceCoordinate> getSliceCoordinatesTheHardWay( List<Integer> sliceNumbers, List<String> files) throws MizerException {
        final List<Integer> frameCountPerFile = new ArrayList<>();
        for (final String f : files) {
            frameCountPerFile.add(framesIn(new File(f)));
        }
        final List<SliceCoordinate> sliceCoordinates = new ArrayList<>();
        int iSlice = 0;
        int iSliceNumber = 0;
        for (int iFile = 0; iFile < files.size() && iSliceNumber < sliceNumbers.size(); iFile++) {
            for (int iFrame = 0; iFrame < frameCountPerFile.get(iFile) && iSliceNumber < sliceNumbers.size(); iFrame++) {
                // Compared before the increment: selecctSliceIndices counts slices from 0, so
                // incrementing first made slice 0 unmatchable, which stalled iSliceNumber and
                // silently dropped every panel after it.
                if (iSlice == sliceNumbers.get(iSliceNumber)) {
                    sliceCoordinates.add(new SliceCoordinate(iFile, iFrame));
                    iSliceNumber++;
                }
                iSlice++;
            }
        }
        return sliceCoordinates;
    }

    /**
     * The frames one object contributes to the montage: its Number of Frames, or one if it does not
     * say, or none at all if it carries no image.
     * <p>
     * The single definition of that: {@code SnapshotResourceGeneratorImpl.countFrames} sums this to
     * decide how many slices a scan has, and the walk below uses it to locate them. Keeping it in
     * one place is what stops a count and a selection that disagree, which sends the montage after
     * a slice the count says is absent, or at an object with no pixels -- the "Missing Pixel Data"
     * failure this replaced.
     */
    static int framesIn(final File file) throws MizerException {
        try (final DicomInputStream input = new DicomInputStream(file)) {
            input.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            // Through Columns: Rows and Columns follow NumberOfFrames in the group, and under
            // IncludeBulkData.NO the pixel data element is skipped rather than added, so the
            // dimensions are the only way to tell an image from a report.
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
