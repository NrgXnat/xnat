/*
 * web: org.nrg.dcm.id.DelegateDicomIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import org.dcm4che2.data.DicomObject;

import java.util.SortedSet;

/**
 * Wraps a {@link DicomDerivedString object}.
 */
public class DelegateDicomIdentifier implements DicomDerivedString {
    /**
     * Creates a delegate identifier from the submitted identifier.
     *
     * @param identifier The identifier for this delegate.
     */
    public DelegateDicomIdentifier(final DicomDerivedString identifier) {
        _identifier = identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final SortedSet<Integer> getTags() {
        return _identifier.getTags();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String apply(final DicomObject dicomObject) {
        return _identifier.apply(dicomObject);
    }

    /**
     * Gets the delegated identifier.
     *
     * @return The delegated identifier.
     */
    protected DicomDerivedString getIdentifier() {
        return _identifier;
    }

    private final DicomDerivedString _identifier;
}
