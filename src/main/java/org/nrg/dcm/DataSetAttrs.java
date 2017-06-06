/*
 * DicomDB: org.nrg.dcm.DataSetAttrs
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.attr.ConversionFailureException;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.util.Opener;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Manages reading DICOM files and retrieving attribute values.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class DataSetAttrs implements Iterable<DicomAttributeIndex> {
    private static final Comparator<DicomAttributeIndex> comparator = new DicomAttributeIndex.Comparator();
    private final DicomObject                    o;
    private final SortedSet<DicomAttributeIndex> tagSet;

    DataSetAttrs(final DicomObject o, final Collection<DicomAttributeIndex> indices) {
        this.o = o;
        this.tagSet = Sets.newTreeSet(comparator);
        tagSet.addAll(indices);
    }

    public static <T> DataSetAttrs create(final T resource,
                                          final Collection<DicomAttributeIndex> tags,
                                          Opener<T> opener) throws IOException {
        return new DataSetAttrs(DicomUtils.read(opener.open(resource), Tag.PixelData - 1), tags);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString() + Iterables.transform(tagSet,
                                                      new Function<DicomAttributeIndex, String>() {
                                                          public String apply(final DicomAttributeIndex i) {
                                                              return i.getAttributeName(o);
                                                          }
                                                      });
    }

    /**
     * Retrieves the value of the indicated attribute from this data set. Performs
     * conversions for a few selected types: VR TM to xs:time, VR DA to xs:date
     *
     * @param index The index of the attribute for which value is requested
     *
     * @return The value of the specified attribute.
     *
     * @throws ConversionFailureException If the value of the attribute can't be converted.
     */
    public String get(final DicomAttributeIndex index) throws ConversionFailureException {
        return index.getString(o);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<DicomAttributeIndex> iterator() {
        return Iterables.unmodifiableIterable(tagSet).iterator();
    }
}
