/*
 * DicomDB: org.nrg.dcm.AbstractDicomAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.nrg.attr.ConversionFailureException;
import org.nrg.dicomtools.utilities.DicomUtils;

import java.util.*;

public abstract class AbstractDicomAttributeIndex implements DicomAttributeIndex {

    /**
     * The tag at which a path enters the object: its first element.
     * <p>
     * That is the only element a stop tag gates. Reading a top-level sequence brings its items with it whatever
     * the stop tag says, so a tag nested inside one needs no wider a window than the sequence itself -- and a
     * nested tag sorting above its own sequence would otherwise widen every read for nothing.
     *
     * @return the entry tag, or 0 for a path that names none.
     */
    protected static int entryTagOf(final int... path) {
        return 0 == path.length ? 0 : path[0];
    }

    /**
     * As above. A null entry is a sequence wildcard rather than a tag, so a path beginning with one names no
     * entry tag.
     */
    protected static int entryTagOf(final Integer... path) {
        return 0 == path.length || null == path[0] ? 0 : path[0];
    }

    /**
     * The highest {@link #entryTagOf entry tag} among these paths, compared unsigned.
     * <p>
     * A max across paths, not along one: the paths are alternative places the same attribute may sit, and the
     * read has to reach whichever the object turns out to use.
     */
    protected static int highestEntryTag(final Collection<Integer[]> paths) {
        int highest = 0;
        for (final Integer[] path : paths) {
            final int entry = entryTagOf(path);
            if (Integer.compareUnsigned(entry, highest) > 0) {
                highest = entry;
            }
        }
        return highest;
    }
    protected static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static int compare(final Integer[] a0, final Integer[] a1) {
        for (int i = 0; i < a0.length && i < a1.length; i++) {
            if (a0[i] != a1[i]) {
                if (null == a0[i]) {
                    return 1;
                } else if (null == a1[i]) {
                    return -1;
                } else {
                    return a0[i] - a1[i];
                }
            }
        }
        return a0.length - a1.length;
    }

    public static String pathToString(final Attributes context, final Integer[] path) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length - 1; i += 2) {
            sb.append(DicomUtils.getAttributeName(context, path[i]));
            sb.append("[");
            sb.append(null == path[i + 1] ? "*" : path[i + 1]);
            sb.append("]:");
        }
        sb.append(DicomUtils.getAttributeName(context, path[path.length - 1]));
        return sb.toString();
    }

    public final int compareTo(final DicomAttributeIndex other, final Attributes context) {
        return compare(getPath(context), other.getPath(context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttributeName(final Attributes attributes) {
        final Integer[] path = getPath(attributes);
        return DicomUtils.getAttributeName(attributes, path[path.length - 1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(final Attributes attributes) throws ConversionFailureException {
        return DicomUtils.combineValues(getStrings(attributes));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(final Attributes attributes, final String defaultValue) throws ConversionFailureException {
        final String[] vs = getStrings(attributes);
        return vs.length > 0 ? DicomUtils.combineValues(getStrings(attributes)) : defaultValue;
    }

    private static List<Attributes> collectNestedContexts(
            final Attributes dataset,
            final Integer[] path,
            final int starti,
            final List<Attributes> contexts
    ) {
        if (null == dataset) {
            return contexts;
        }
        final int last = path.length - 1;
        if (last == starti) {   // down to value tag, return context if present
            if (dataset.contains(path[starti])) {
                contexts.add(dataset);
            }
            return contexts;
        }
        final int sqtag = path[starti];
        final Sequence sq = dataset.getSequence(sqtag);
        if (null == sq) {
            return contexts;
        }
        final Integer index = path[starti+1];
        if (null == index) {
            sq.forEach(context ->
                    collectNestedContexts(context, path, starti+2, contexts)
            );
        } else {
            collectNestedContexts(sq.get(index), path, starti+2, contexts);
        }
        return contexts;
    }

    /**
     * Retrieve all Attributes objects in which the final value tag of the given path can be
     * evaluated.
     * If path is a single tag, or a fully resolved tag:index:tag:index:tag sequence,
     * then this will return at most one Attributes object--and maybe zero, if that last value
     * tag is not present. If path includes index wildcards, than this could return multiple
     * Attribute objects, one for every matching path to the final value tag.
     * @param dataset root attribute to start resolving path
     * @param path path [tag:index]:tag to value attributes, possibly including null index
     *             values that act as wildcards
     * @return Attributes where the final value tag can be evaluated
     */
    protected static List<Attributes> getNestedContexts(
            final Attributes dataset,
            final Integer[] path) {
        return collectNestedContexts(dataset, path, 0, new ArrayList<>());
    }
}
