/*
 * dicomtools: org.nrg.dcm.ChainedDicomAttributeIndex
 * XNAT http://www.xnat.org
 * Copyright (c) 2017,2026 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.collect.Lists;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.nrg.dicomtools.utilities.DicomUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * Attribute index that takes a list of tag paths, and returns the value(s) from the first of those paths
 * that can be evaluated. Generally used for a "is this attribute present? no, then try this one"
 * chain of possibilities.
 */
public final class ChainedDicomAttributeIndex extends AbstractDicomAttributeIndex
        implements DicomAttributeIndex {
    private static final Integer[] NO_PATH = {};

    // Attributes that have multiple values (VM>1) because each value is a component shouldn't be merged.
    // There are likely more than these in the standard but these are the ones we know we care about.
    private static final Set<Integer> unmergeableAttributes = Set.of(Tag.ImageOrientationPatient, Tag.PixelSpacing);

    // There's no sensible way to concatenate values with these VRs
    private static final Set<VR> unmergeableVRs = Set.of(VR.AE, VR.AS, VR.AT, VR.DA,
            VR.DT, VR.OB, VR.OF, VR.OW, VR.PN, VR.SQ, VR.TM, VR.UN, VR.UT);

    private final String          id;
    private final List<Integer[]> paths;

    public ChainedDicomAttributeIndex(final String id, final Iterable<Integer[]> paths) {
        this.id = id;
        this.paths = Lists.newArrayList(paths);
    }

    public ChainedDicomAttributeIndex(final String id, final Integer[]... paths) {
        this(id, Arrays.asList(paths));
    }

    /**
     * In order to know how to handle merging attributes with multiple values, we need to
     * know the tag and VR. In many such cases, we can combine by concatenating the multiple
     * values; in others, concatenation produces weird results so we don't.
     * @param tag tag of the attribute containing these values
     * @param vr VR of the attribute
     * @param value value of the attribute, combined if VM>1
     */
    private record ValueInContext(int tag, VR vr, String value) {
        ValueInContext(final Attributes o, final int tag) {
            this(tag, o.getVR(tag), DicomUtils.combineValues(o.getStrings(tag)));
        }

        boolean isMergeable() {
            return !unmergeableVRs.contains(vr) && !unmergeableAttributes.contains(tag);
        }
    }

    /**
     * Get all values for this attribute from the provided dataset. The logic for combining multiple matches is a little
     * complicated: attributes that naturally have VM>1 because they represent a vector value (e.g., Pixel Spacing)
     * can't be sensibly combined, so if there are multiple values, that's an extraction failure, and we return no value.
     * Some VRs can't be sensibly combined. For everything else, return all distinct values so the caller can combine.
     * This is a reimplementation of logic in MergedDicomElement from older, dcm4che2-based versions.
     * @param o Attributes on which to evaluate values
     * @return stream of distinct values to be combined
     */
    private Stream<String[]> getAllValues(final Attributes o){
        return paths.stream()
                .map(path -> {
                    final int lastTag = path[path.length - 1];
                    final List<ValueInContext> values = getNestedContexts(o, path).stream()
                            .map(context -> new ValueInContext(context, lastTag))
                            .toList();
                    final String[] distinct = values.stream().map(ValueInContext::value).distinct().toArray(String[]::new);
                    if (values.stream().allMatch(ValueInContext::isMergeable)) {
                        return distinct;
                    } else {
                        // can't be merged so return one value or none
                        return distinct.length == 1 ? distinct : new String[0];
                    }
                })
                .filter(vals -> vals.length > 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttributeName(final Attributes attributes) {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName() {
        return id;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Integer[] getPath(final Attributes context) {
        return paths.stream()
                .filter(path -> !getNestedContexts(context, path).isEmpty())
                .findFirst()
                .orElse(NO_PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(final Attributes o) {
        return getString(o, null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(final Attributes o, final String defaultValue) {
        return getAllValues(o).map(DicomUtils::combineValues).findFirst().orElse(defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getStrings(final Attributes o) {
        return getAllValues(o).findFirst().orElseGet(() -> new String[]{});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final Attributes o = new Attributes();
        return paths.stream().reduce(new StringJoiner(",", "Chained[", "]"),
                        (j, p) -> j.add(pathToString(o, p)),
                        StringJoiner::merge)
                .toString();
    }
}
