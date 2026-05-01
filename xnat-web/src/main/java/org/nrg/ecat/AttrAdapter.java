/*
 * ecat4xnat: org.nrg.ecat.AttrAdapter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Set;

import org.nrg.attr.AbstractAttrAdapter;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.AttrDefs;
import org.nrg.attr.MutableAttrDefs;

public final class AttrAdapter extends AbstractAttrAdapter<Variable, Object> {
    private final Set<SortedMap<Variable, ?>> ds;

    private AttrAdapter(AttrDefs<Variable>... attrs) {
        super(new MutableAttrDefs<Variable>(), attrs);
        this.ds = new LinkedHashSet<>();
    }

    /**
     * Constructs an AttrAdapter for attributes in a single ECAT subheader (frame)
     *
     * @param header The header.
     * @param attrs  Attributes to store.
     */
    public AttrAdapter(final Header header, final AttrDefs<Variable>... attrs) {
        this(attrs);
        this.ds.add(header.getValues());
    }

    public AttrAdapter(final MatrixDataFile f, final AttrDefs<Variable>... attrs) {
        this(attrs);
        this.ds.addAll(f.getScanValues());
    }

    public AttrAdapter(final FileSet es, final AttrDefs<Variable>... attrs) {
        this(attrs);
        for (final MatrixDataFile f : es.getFiles()) {
            this.ds.addAll(f.getScanValues());
        }
    }

    @Override
    public Collection<Map<Variable, Object>> getUniqueCombinationsGivenValues(Map<Variable, Object> given,
                                                                              Collection<Variable> attrs,
                                                                              Map<Variable, ConversionFailureException> failures) {
        List<Map<Variable, Object>> combs = new ArrayList<>();

        if (attrs.size() == 0) {    // special case for empty external attributes
            return combs;
        }

        // Each map in the dataset is the set of variable values for a single scan.
        // For each set that matches the constraints, make a copy including only the requested attribute.
        combination_loop:
        for (final SortedMap<Variable, ?> comb : ds) {
            // If any of the constraints don't match, move on to the next combination
            for (final Map.Entry<Variable, Object> e : given.entrySet()) {
                final Variable v = e.getKey();
                if (!comb.containsKey(v) || !comb.get(v).equals(e.getValue())) {
                    continue combination_loop;
                }
            }

            // Passed all tests, make a copy of the values including only the requested attributes
            // and add it to the return set.
            final SortedMap<Variable, Object> pruned = new TreeMap<>(comb);
            pruned.keySet().retainAll(attrs);
            combs.add(pruned);
        }

        return combs;
    }
}
