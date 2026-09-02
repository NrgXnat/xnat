/*
 * DicomDB: org.nrg.dcm.FixedDicomAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.util.TagUtils;
import org.nrg.dicomtools.utilities.DicomUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Index for a simple attribute that always gets its value from the same location.
 */
public final class FixedDicomAttributeIndex extends AbstractDicomAttributeIndex implements DicomAttributeIndex {
    private final int[] path;
    private final ItemPointer[] itemPointer;
    private final int lastTag;
    private final String column;

    public FixedDicomAttributeIndex(final String columnName, final int... tagPath) {
        if (tagPath.length % 2 != 1) {
            throw new IllegalArgumentException(columnName + ": tag path must have odd number of components");
        }
        path = new int[tagPath.length];
        System.arraycopy(tagPath, 0, path, 0, tagPath.length);

        itemPointer = new ItemPointer[tagPath.length/2];
        for (int i = 0; i < tagPath.length-1; i += 2) {
            itemPointer[i/2] = new ItemPointer(tagPath[i], tagPath[i+1]);
        }
        lastTag = tagPath[tagPath.length-1];

        column = columnName;
    }

    public FixedDicomAttributeIndex(final int... tagPath) {
        this(makeColumnName(tagPath), tagPath);
    }

    private static String makeColumnName(final int[] tagPath) {
        final StringBuilder sb = new StringBuilder("ft");
        sb.append("%1$08x".formatted(tagPath[0]));
        for (int i = 1; i < tagPath.length; i++) {
            sb.append("_").append("%1$08x".formatted(tagPath[i]));
        }
        return sb.toString();
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public String getColumnName() {
        return column;
    }

    private Integer[] getPath() {
        final Integer[] path = new Integer[this.path.length];
        for (int i = 0; i < path.length; i++) {
            path[i] = this.path[i];
        }
        return path;
    }

    public Integer[] getPath(final Attributes context) {
        return getPath();
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public int getMaxTag() {
        return maxTagIn(path);
    }

    /**
     * ${@inheritDoc}
     */
    private Optional<Attributes> containingDataset(final Attributes root) {
        return itemPointer.length > 0 ? Optional.ofNullable(root.getNestedDataset(itemPointer)) : Optional.of(root);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public String getString(final Attributes attributes) {
        return containingDataset(attributes)
                .flatMap(attrs -> Optional.ofNullable(attrs.getStrings(lastTag)))
                .map(DicomUtils::combineValues)
                .orElse(null);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public String getString(final Attributes attributes, String defaultValue) {
        return containingDataset(attributes)
                .flatMap(attrs -> Optional.ofNullable(attrs.getStrings(lastTag)))
                .filter(values -> values.length > 0)
                .map(DicomUtils::combineValues)
                .orElse(defaultValue);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public String[] getStrings(final Attributes attributes) {
       return containingDataset(attributes)
               .map(attrs -> attrs.getStrings(lastTag))
               .orElse(null);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public String toString() {
        return Arrays.stream(path).mapToObj(TagUtils::toString)
                .reduce(new StringJoiner(":", "Fixed[", "]"), StringJoiner::add, StringJoiner::merge)
                .toString();
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(path);
    }

    /**
     * ${@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof FixedDicomAttributeIndex &&
                Arrays.equals(path, ((FixedDicomAttributeIndex) o).path);
    }
}
