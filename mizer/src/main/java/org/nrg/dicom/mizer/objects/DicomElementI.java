/*
 * DicomEdit: DicomElementI
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.objects;

/**
 * Interface representing elements within DICOM Ojbects.
 *
 * Essential for dealing with sequence elements. This interface provides a boundary between DicomEdit and underlying
 * DICOM libraries, e.g. dcm4che2.
 *
 */
public interface DicomElementI {

    int tag();

    /**
     * @return  hex string representation of this element
     */
    String tagString();

    boolean hasItems();

    int countItems();

    DicomObjectI getDicomObject( int i);

    void removeItem( int i);

    default boolean isPixelData() {
        return 0x7FE00010 == tag();
    }

    boolean isUID();

    String getVRAsString();

}
