package org.nrg.xnat.snapshot.generator.impl;

import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        return getSliceCoordinates(nPanels, nSlices, files, Collections.emptyMap());
    }

    /**
     * As above, given frame counts the caller has already read.
     *
     * @param knownFrameCounts frames per file path. A file missing from it is read here; an empty map means
     *                         reading every file this reaches, which is what happens when the catalog carried
     *                         a frame count and so nothing had to be counted off the objects.
     */
    public List<SliceCoordinate> getSliceCoordinates(int nPanels, int nSlices, List<String> files,
                                                     Map<String, Integer> knownFrameCounts) throws MizerException {
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
            coordinates.addAll(getSliceCoordinatesTheHardWay(sliceNumbers, files, knownFrameCounts));
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
    private List<SliceCoordinate> getSliceCoordinatesTheHardWay(List<Integer> sliceNumbers, List<String> files,
                                                               Map<String, Integer> knownFrameCounts) throws MizerException {
        final List<SliceCoordinate> sliceCoordinates = new ArrayList<>();
        int iSlice = 0;
        int iSliceNumber = 0;
        for (int iFile = 0; iFile < files.size() && iSliceNumber < sliceNumbers.size(); iFile++) {
            // Counted as we reach each object rather than for all of them up front: the loop ends
            // once the last requested slice is placed, and on shared storage every object it does
            // not reach is a round trip saved. A single panel selects the midpoint, so that is
            // half of them.
            //
            // And only when the caller does not already know: a scan whose catalog records no frame count
            // has had every header read to total the slices, so reading them again here was the same work
            // twice.
            final String file = files.get(iFile);
            final int frames = knownFrameCounts.containsKey(file)
                               ? knownFrameCounts.get(file)
                               : FrameCounter.framesIn(new File(file));
            for (int iFrame = 0; iFrame < frames && iSliceNumber < sliceNumbers.size(); iFrame++) {
                // Slice indices count from 0, so compare before advancing.
                if (iSlice == sliceNumbers.get(iSliceNumber)) {
                    sliceCoordinates.add(new SliceCoordinate(iFile, iFrame));
                    iSliceNumber++;
                }
                iSlice++;
            }
        }
        return sliceCoordinates;
    }

}
