/*
 * DicomDB: org.nrg.dcm.DataSetAttrs
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.nrg.attr.ConversionFailureException;
import org.nrg.dicomtools.utilities.DicomUtils;
import org.nrg.util.Opener;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * Manages reading DICOM files and retrieving attribute values.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class DataSetAttrs implements Iterable<DicomAttributeIndex> {
    private static final Comparator<DicomAttributeIndex> comparator = new DicomAttributeIndex.Comparator();
    private final Attributes attributes;
    private final SortedSet<DicomAttributeIndex> tagSet;

    DataSetAttrs(final Attributes attributes, final Collection<DicomAttributeIndex> indices) {
        this.attributes = attributes;
        this.tagSet = Sets.newTreeSet(comparator);
        tagSet.addAll(indices);
    }

    public static <T> DataSetAttrs create(final T resource,
                                          final Collection<DicomAttributeIndex> tags,
                                          Opener<T> opener) throws IOException {
        return new DataSetAttrs(DicomUtils.read(opener.open(resource), stopTagFor(tags)), tags);
    }

    /**
     * The largest bound this can return. A caller turns the bound into an exclusive stop tag by adding one, and
     * one more than 0xFFFFFFFF is zero -- which dcm4che reads as "stop at the first element", so every file would
     * come back empty. Only a DICOM mapping can name a tag that high, since parseDicomTag accepts the whole
     * unsigned range, and (FFFF,FFFF) is not a legal tag in any case. Capping here reads to the end of the
     * object instead.
     */
    private static final int HIGHEST_STOP_TAG = 0xFFFFFFFE;

    /**
     * How far to read for these indices: the last tag any of them may ask for.
     * <p>
     * A fixed stop tag cannot serve both ends of this. An index may point at a tag sorting after the pixel data --
     * a private group &gt;= 0x8000, for instance -- and the read has to reach it or the attribute comes back empty
     * with no error; a store whose indices all stop early should not read past them for nothing. So it is the
     * highest bound the indices report, and an index that cannot report one asks for
     * {@link DicomAttributeIndex#getMaxTag() the tag before the pixel data}, which holds the window open for the
     * rest of the set.
     * <p>
     * For a session builder store the bound is (5200,9230): DicomAttributes.chain reaches into the per-frame
     * functional groups, and every modality's attributes are registered on every store, so those sequences are
     * what holds the window open.
     *
     * @param tags the indices the store will query.
     *
     * @return the last tag to read, compared unsigned.
     */
    static int stopTagFor(final Collection<DicomAttributeIndex> tags) {
        int stopTag = 0;
        for (final DicomAttributeIndex index : tags) {
            final int maxTag = index.getMaxTag();
            if (Integer.compareUnsigned(maxTag, stopTag) > 0) {
                stopTag = maxTag;
            }
        }
        if (0 == stopTag) {
            // Nothing was asked for. Read to the pixel data rather than not at all, so a caller that queries an
            // index it never declared still finds it.
            return Tag.PixelData - 1;
        }
        return Integer.compareUnsigned(stopTag, HIGHEST_STOP_TAG) > 0 ? HIGHEST_STOP_TAG : stopTag;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString() + Iterables.transform(tagSet,
                dai -> dai.getAttributeName(attributes));
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
        return index.getString(attributes);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<DicomAttributeIndex> iterator() {
        return Iterables.unmodifiableIterable(tagSet).iterator();
    }
}
