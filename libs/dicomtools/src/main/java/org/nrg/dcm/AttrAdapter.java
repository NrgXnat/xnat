/*
 * DicomDB: org.nrg.dcm.AttrAdapter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import org.nrg.attr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * For a given FileSet, generates external attributes from the corresponding
 * DICOM fields.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class AttrAdapter extends AbstractAttrAdapter<DicomAttributeIndex, String> {
    private static final Map<DicomAttributeIndex, String> EMPTY_CONTEXT = Collections.emptyMap();

    private final Logger logger = LoggerFactory.getLogger(AttrAdapter.class);
    private final DicomMetadataStore store;
    private final Map<?, String>     context;

    public AttrAdapter(final DicomMetadataStore store, final Map<?, String> context,
                       final AttrDefs... attrs) {
        super(new MutableAttrDefs(), attrs);
        this.store = store;
        this.context = ImmutableMap.copyOf(context);
    }

    /**
     * Creates a new attribute adapter for the given FileSet
     *
     * @param fileSet    The DICOM file set.
     * @param attributes Attribute sets for conversion.
     */
    public AttrAdapter(final DicomMetadataStore fileSet, final AttrDefs... attributes) {
        this(fileSet, EMPTY_CONTEXT, attributes);
    }

    public Collection<Map<DicomAttributeIndex, String>> getUniqueCombinationsGivenValues(final Map<DicomAttributeIndex, String> given,
                                                                                         final Collection<DicomAttributeIndex> attrs,
                                                                                         final Map<DicomAttributeIndex, ConversionFailureException> failures) {
        try {
            final Map<Object, String> g = Maps.newLinkedHashMap(context);
            g.putAll(given);
            return store.getUniqueCombinationsGivenValues(g, attrs, failures);
        } catch (Throwable t) {
            for (final DicomAttributeIndex attr : attrs) {
                failures.put(attr, new ConversionFailureException(attr, null, "conversion failed", t));
            }
            return Collections.emptyList();
        }
    }

    /**
     * For each file, returns the single value of each specified attribute
     *
     * @return map from each file to a list of external attribute values
     *
     * @throws IOException  When an error occurs reading or writing data.
     * @throws SQLException When an error occurs interacting with the database.
     */
    public ListMultimap<URI, ExtAttrValue> getValuesForResources()
            throws IOException, SQLException {
        final ListMultimap<URI, ExtAttrValue> values = ArrayListMultimap.create();
        for (final Map.Entry<URI, Map<DicomAttributeIndex, String>> fme
                : store.getValuesForResourcesMatching(getDefs().getNativeAttrs(), context).entrySet()) {
            final Iterable<Map<DicomAttributeIndex, String>> vals = Collections.singletonList(fme.getValue());
            for (final ExtAttrDef<DicomAttributeIndex> ea : getDefs()) {
                try {
                    @SuppressWarnings("unchecked")
                    final EvaluableAttrDef<DicomAttributeIndex, String, ?> def =
                            (EvaluableAttrDef<DicomAttributeIndex, String, ?>) ea;
                    values.putAll(fme.getKey(), def.foldl(vals));
                } catch (ExtAttrException e) {
                    // TODO: export this failure
                    logger.warn("Unable to build attribute " + ea + " from file " + fme.getKey(), e);
                }
            }
        }
        return values;
    }
}
