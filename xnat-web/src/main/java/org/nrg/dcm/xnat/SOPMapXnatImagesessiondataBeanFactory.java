/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.SOPMapXnatImagesessiondataBeanFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.nrg.dcm.SOPModel;

import static org.nrg.dcm.Attributes.SOPClassUID;
import static org.nrg.dcm.SOPModel.LEAD_SOP_EXTRACTOR;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class SOPMapXnatImagesessiondataBeanFactory extends AttributeMapXnatImagesessiondataBeanFactory {
    /**
     * Creates a new instance of the factory class
     */
    public SOPMapXnatImagesessiondataBeanFactory() {
        super(SOPClassUID, SOPModel.getSessionTypesFromSOPMap(), LEAD_SOP_EXTRACTOR);
    }
}
