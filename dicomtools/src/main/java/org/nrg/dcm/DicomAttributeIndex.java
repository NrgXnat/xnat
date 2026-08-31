/*
 * DicomDB: org.nrg.dcm.DicomAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.nrg.attr.ConversionFailureException;

public interface DicomAttributeIndex {
    String getAttributeName(Attributes attributes);

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
    Integer[] getPath(Attributes context);

    /**
     * Returns an upper bound, compared unsigned, on the tags this index may read.
     * <p>
     * {@link #getPath(Attributes)} needs the object to resolve, which is no use to a reader deciding how far to
     * read in the first place, so this reports the highest tag the index could ask for without seeing anything.
     * A conservative over-estimate is fine; an under-estimate means the reader stops short and the attribute
     * comes back empty.
     * <p>
     * The default is the tag before the pixel data, which is how far every read went before this existed. An
     * implementation that knows its tags should override it and say so -- the reader stops at the highest
     * bound it is given, so one index that does not know keeps the window open for all of them. An
     * implementation that can reference a tag sorting <i>after</i> the pixel data must override it, or the
     * store will not read far enough to find the value.
     *
     * @return the highest tag this index may read, as an unsigned value.
     */
    default int getMaxTag() {
        return Tag.PixelData - 1;
    }

    /**
     * Returns the value of the target attributes in the provided dataset.
     * @param attributes DICOM dataset
     * @return value of the provided attribute: joined by \\ if multiple values, null if undefined
     * @throws ConversionFailureException
     * Note: no instances actually throw this ConversionFailureException? may be deprecated
     */
    String getString(Attributes attributes) throws ConversionFailureException;

    /**
     * Returns the value of the target attributes in the provided dataset.
     * @param attributes DICOM dataset
     * @return value of the provided attribute: joined by \\ if multiple values, provided default if undefined
     * @throws ConversionFailureException
     * Note: no instances actually throw this ConversionFailureException? may be deprecated
     */
    String getString(Attributes attributes, String defaultValue) throws ConversionFailureException;

    /**
     * Get all values for this attributes on this dataset.
     * @param attributes dataset from which the attribute is to be extracted
     * @return zero or more values, or null if the attribute can't be evaluated on this dataset
     */
    String[] getStrings(Attributes attributes);


    class Comparator implements java.util.Comparator<DicomAttributeIndex> {
        public int compare(final DicomAttributeIndex i0, final DicomAttributeIndex i1) {
            return i0.getColumnName().compareTo(i1.getColumnName());
        }
    }
}
