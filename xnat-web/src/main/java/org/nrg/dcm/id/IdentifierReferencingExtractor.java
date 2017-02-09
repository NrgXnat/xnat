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
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;

/**
 * Provides a method to set the identifier that's containing this extractor. This can be used so that extractors can
 * find other values that the identifier may have.
 */
public interface IdentifierReferencingExtractor extends Extractor {
    /**
     * Sets the DICOM object identifier instance.
     *
     * @param identifier The identifier instance.
     */
    void setIdentifier(DicomObjectIdentifier<XnatProjectdata> identifier);
}
