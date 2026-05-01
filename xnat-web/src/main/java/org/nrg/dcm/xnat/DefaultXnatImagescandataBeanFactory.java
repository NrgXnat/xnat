/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ModalityMapXnatImagesessiondataBeanFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import org.nrg.dcm.DicomMetadataStore;
import org.nrg.dcm.SOPModel;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.bean.XnatOtherdicomscandataBean;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class DefaultXnatImagescandataBeanFactory extends XnatImagescandataBeanFactory {

    @Override
    public XnatImagescandataBean create(Series series, DicomMetadataStore store) {
        final String scanSOPClass = SOPModel.getScanType(series.getSOPClasses());
        if (scanSOPClass == null) {
            return new XnatOtherdicomscandataBean();
        }
        try {
            return XnatClassMapping.forBaseClass(XnatImagescandataBean.class).create(scanSOPClass);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            return new XnatOtherdicomscandataBean();
        }
    }
}
