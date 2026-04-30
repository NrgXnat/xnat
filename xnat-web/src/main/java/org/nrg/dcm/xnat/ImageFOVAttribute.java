/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ImageFOVAttribute
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.nrg.dcm.NamedAttributes.Cols;
import static org.nrg.dcm.NamedAttributes.Rows;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.nrg.attr.BasicExtAttrValue;
import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.dcm.DicomAttributeIndex;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
final class ImageFOVAttribute extends XnatAttrDef.Abstract<Map<?,Integer>> {
    ImageFOVAttribute(final String name) {
        super(name, Rows, Cols);
    }

    enum Fields { ROWS, COLS };

    public Map<?,Integer> start() { return Maps.newEnumMap(Fields.class); }

    @SuppressWarnings("unchecked")
    public Map<?,Integer> foldl(final Map<?,Integer> a, final Map<? extends DicomAttributeIndex,? extends String> m)
            throws ExtAttrException {
        final String rows = m.get(Rows), columns = m.get(Cols);
        if (null == rows || null == columns) {
            return a;
        }
        final int x, y;
        try {
            y = Integer.parseInt(rows);
        } catch (NumberFormatException e) {
            throw new ConversionFailureException(Rows, rows, getName(), "not valid integer value");
        }
        try {
            x = Integer.parseInt(columns);
        } catch (NumberFormatException e) {
            throw new ConversionFailureException(Cols, columns, getName(), "not valid integer value");
        }
        final Map<Fields,Integer> v = ImmutableMap.of(Fields.ROWS, y, Fields.COLS, x);
        if (a.isEmpty() || a.equals(v)) {
            return v;
        } else {
            throw new NoUniqueValueException(getName(), Arrays.asList(a, v));
        }
    }

    public Iterable<ExtAttrValue> apply(final Map<?,Integer> a) throws NoUniqueValueException {
        if (a.containsKey(Fields.COLS) && a.containsKey(Fields.ROWS)) {
        return Collections.<ExtAttrValue>singletonList(new BasicExtAttrValue(getName(), null,
                ImmutableMap.of("x", Integer.toString(a.get(Fields.COLS)),
                        "y", Integer.toString(a.get(Fields.ROWS)))));
        } else {
            throw new NoUniqueValueException(getName(), a.entrySet());
        }
    }
}
