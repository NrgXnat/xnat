package org.nrg.dcm.id;

import org.nrg.dcm.Extractor;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;

/**
 * Provides a method to set the identifier that's containing this extractor. This can be used so that extractors can
 * find other values that the identifier may have.
 */
public interface ReferencingExtractor extends Extractor {
    /**
     * Sets the identifier object.
     * @param identifier    The identifier object.
     */
    void setIdentifier(DicomObjectIdentifier<XnatProjectdata> identifier);
}
