/*
 * DicomDB: org.nrg.dcm.FixedDicomAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.util.Arrays;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.util.TagUtils;


/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class FixedDicomAttributeIndex extends AbstractDicomAttributeIndex
implements DicomAttributeIndex {
    private final int[] path;
    private final String column;

    public FixedDicomAttributeIndex(final String columnName, final int...tagPath) {
        if (0 == tagPath.length) {
            throw new IllegalArgumentException("must provide at least one tag");
        }
        path = new int[tagPath.length];
        System.arraycopy(tagPath, 0, path, 0, tagPath.length);

        column = columnName;
    }

    public FixedDicomAttributeIndex(final int...tagPath) {
        this(makeColumnName(tagPath), tagPath);
    }

    private static String makeColumnName(final int[] tagPath) {
        final StringBuilder sb = new StringBuilder("ft");
        sb.append(String.format("%1$08x", tagPath[0]));
        for (int i = 1; i < tagPath.length; i++) {
            sb.append("_").append(String.format("%1$08x", tagPath[i]));
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.DicomAttributeIndex#getColumnName()
     */
    public String getColumnName() { return column; }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomAttributeIndex#getElement(org.dcm4che2.data.DicomObject)
     */
    public DicomElement getElement(final DicomObject o) {
        return o.get(path);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.DicomAttributeIndex#getPath(org.dcm4che2.data.DicomObject)
     */
    public Integer[] getPath(final DicomObject context) {
        final Integer[] path = new Integer[this.path.length];
        for (int i = 0; i < path.length; i++) {
            path[i] = this.path[i];
        }
        return path;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(TagUtils.toString(path[0]));
        for (int i = 1; i < path.length; i++) {
            sb.append("/").append(TagUtils.toString(path[i]));
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(path);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof FixedDicomAttributeIndex &&
        Arrays.equals(path, ((FixedDicomAttributeIndex)o).path);
    }


    public static DicomAttributeIndex[] makeIndices(int...tags) {
        final DicomAttributeIndex[] indices = new DicomAttributeIndex[tags.length];
        for (int i = 0; i < tags.length; i++) {
            indices[i] = new FixedDicomAttributeIndex(tags[i]);
        }
        return indices;
    }

    public static DicomAttributeIndex[] makeIndices(Integer...tags) {
        final DicomAttributeIndex[] indices = new DicomAttributeIndex[tags.length];
        for (int i = 0; i < tags.length; i++) {
            indices[i] = new FixedDicomAttributeIndex(tags[i]);
        }
        return indices;
    }
}
