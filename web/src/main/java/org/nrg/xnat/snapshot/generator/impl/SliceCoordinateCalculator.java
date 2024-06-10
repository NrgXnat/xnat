package org.nrg.xnat.snapshot.generator.impl;

import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;

import java.io.File;
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
        List<Integer> frameCountPerFile = new ArrayList<>();
        for( String f: files) {
            String s = DicomObjectFactory.newInstance(new File(f)).getString(0x00280008);
            int nFrames = (s != null) ? Integer.getInteger(s) : 1;
            frameCountPerFile.add(nFrames);
        }
        List<SliceCoordinate> sliceCoordinates = new ArrayList<>();
        int iSlice = 0;
        int iSliceNumber = 0;
        for( int iFile = 0; iFile < files.size(); iFile++) {
            for( int iFrame = 0; iFrame < frameCountPerFile.get(iFile); iFrame++) {
                iSlice++;
                if( iSlice == sliceNumbers.get( iSliceNumber)) {
                    sliceCoordinates.add( new SliceCoordinate( iFile, iFrame));
                    iSliceNumber++;
                }
            }
        }
        return sliceCoordinates;
    }

}
