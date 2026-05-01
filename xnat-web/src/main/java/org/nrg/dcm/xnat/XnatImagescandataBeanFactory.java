/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.XnatImagesessiondataBeanFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.nrg.dcm.DicomMetadataStore;
import org.nrg.xdat.bean.XnatImagescandataBean;

/**
 * DICOMScanBuilder uses a chain of these Factories to build the scan bean.
 * The first one in the chain to succeed (i.e., return non-null) wins.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class XnatImagescandataBeanFactory extends XnatBeanFactory {

    /**
     * Attempt to create a XnatImagescandataBean, given the provided metadata.
     * @param series series containing the scan series metadata
     * @param store the whole dicom metadata store for the session
     * @return a XnatImagescandataBean (subclass) instance, of type determined by
     *  the can series metadata; or null if this factory does not create sessions matching
     *  the provided study.
     */
    abstract XnatImagescandataBean create(final Series series, final DicomMetadataStore store);
}
