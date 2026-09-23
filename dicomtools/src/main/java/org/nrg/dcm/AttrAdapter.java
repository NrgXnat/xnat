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
import com.google.common.collect.Sets;
import org.nrg.attr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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
            return store.getUniqueCombinationsGivenValues(constraints(given), attrs, failures);
        } catch (Throwable t) {
            for (final DicomAttributeIndex attr : attrs) {
                failures.put(attr, new ConversionFailureException(attr, null, "conversion failed", t));
            }
            return Collections.emptyList();
        }
    }

    /**
     * One store query for all of the definitions instead of one per definition: a scan's forty-odd
     * attribute lookups become a single SELECT. Each definition then sees the distinct combinations of
     * its own attributes projected from the shared rows, which is the same set the per-definition query
     * returns (the distinct projection of distinct rows). Definitions with no native attributes get
     * nothing, as the store gives them nothing. Should the shared query fail, the definitions are
     * queried one at a time as before, so a failure is reported exactly as it always was.
     */
    @Override
    protected Function<ExtAttrDef<DicomAttributeIndex>, Collection<Map<DicomAttributeIndex, String>>>
    combinationsFor(final Map<DicomAttributeIndex, String> given, final Map<DicomAttributeIndex, ConversionFailureException> failures) {
        final Set<DicomAttributeIndex> all = Sets.newLinkedHashSet();
        for (final ExtAttrDef<DicomAttributeIndex> ea : getDefs()) {
            all.addAll(ea.getAttrs());
        }
        if (all.isEmpty()) {
            return super.combinationsFor(given, failures);
        }
        final Collection<Map<DicomAttributeIndex, String>> rows;
        try {
            rows = store.getUniqueCombinationsGivenValues(constraints(given), all, failures);
        } catch (Throwable t) {
            logger.debug("Shared query for {} failed, querying each definition on its own", all, t);
            return super.combinationsFor(given, failures);
        }
        return ea -> {
            final Set<DicomAttributeIndex> wanted = ea.getAttrs();
            if (wanted.isEmpty()) {
                return Collections.emptyList();
            }
            final Set<Map<DicomAttributeIndex, String>> combinations = Sets.newHashSet();
            for (final Map<DicomAttributeIndex, String> row : rows) {
                final Map<DicomAttributeIndex, String> projection = Maps.newHashMap();
                for (final Map.Entry<DicomAttributeIndex, String> entry : row.entrySet()) {
                    if (wanted.contains(entry.getKey())) {
                        projection.put(entry.getKey(), entry.getValue());
                    }
                }
                combinations.add(projection);
            }
            return combinations;
        };
    }

    private Map<Object, String> constraints(final Map<DicomAttributeIndex, String> given) {
        final Map<Object, String> g = Maps.newLinkedHashMap(context);
        g.putAll(given);
        return g;
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
