/*
 * web: org.nrg.dcm.id.ReferencingExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import org.nrg.dcm.Extractor;
import org.nrg.xnat.DicomObjectIdentifier;

/**
 * Provides a method to set the session extractor set on the {@link DicomObjectIdentifier} containing this extractor.
 * This can be used so that extractors can reference values that the related extractor may have.
 *
 * @see SubjectReferencingExtractor
 */
public interface SessionReferencingExtractor extends Extractor {
    /**
     * Sets the session extractor instance.
     *
     * @param sessionExtractor The session extractor instance.
     */
    void setSessionExtractor(final Extractor sessionExtractor);
}
