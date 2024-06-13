/*
 * DicomDB: org.nrg.dcm.DicomAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.nrg.attr.ConversionFailureException;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface DicomAttributeIndex {
    String getAttributeName(DicomObject o);

    /**
     * Returns the name of the column for the target attribute in the SQL database.
     *
     * @return The name of the column corresponding to the submitted index.
     */
    String getColumnName();

    /**
     * Returns the tag path for this attribute index. Requires a context argument
     * because private tags are relocatable. Note that this path is not quite equivalent
     * to the dcm4che int[] tagpath, because we can use null values as a sequence index
     * meaning use all of the sequence items.
     *
     * @param context    The DICOM object.
     *
     * @return The tag path for the indicated object.
     */
    Integer[] getPath(DicomObject context);

    DicomElement getElement(DicomObject o);

    String getString(DicomObject o) throws ConversionFailureException;

    String getString(DicomObject o, String defaultValue) throws ConversionFailureException;

    String[] getStrings(DicomObject o);

    class Comparator implements java.util.Comparator<DicomAttributeIndex> {
        public int compare(final DicomAttributeIndex i0, final DicomAttributeIndex i1) {
            return i0.getColumnName().compareTo(i1.getColumnName());
        }
    }
}
