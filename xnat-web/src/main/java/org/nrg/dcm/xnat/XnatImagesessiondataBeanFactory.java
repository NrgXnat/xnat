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
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.slf4j.Logger;

import java.util.Map;

/**
 * DICOMSessionBuilder uses a chain of these Factories to build the scan bean.
 * The first one in the chain to succeed (i.e., return non-null) wins.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class XnatImagesessiondataBeanFactory extends XnatBeanFactory {
    /**
     * Attempt to create a XnatImagesessiondataBean, given the provided metadata.
     * @param store DicomMetadataStore containing the study metadata
     * @param studyInstanceUID study to be translated to a session
     * @return a XnatImagesessiondataBean (subclass) instance, of type determined by
     *  the study metadata; or null if this factory does not create sessions matching
     *  the provided study.
     */
    abstract XnatImagesessiondataBean create(DicomMetadataStore store, String studyInstanceUID);

    /**
     * Attempt to create a XnatImagesessiondataBean, given the provided metadata.
     * @param store            DicomMetadataStore containing the study metadata
     * @param studyInstanceUID Study to be translated to a session
     * @param parameters       Parameters to be used for the create operation.
     * @return a XnatImagesessiondataBean (subclass) instance, of type determined by
     *  the study metadata; or null if this factory does not create sessions matching
     *  the provided study.
     */
    abstract XnatImagesessiondataBean create(DicomMetadataStore store, String studyInstanceUID, Map<String, String> parameters);
}
