package org.nrg.dcm.xnat;

import org.nrg.dcm.AttrDefs;
import org.nrg.xdat.bean.XnatImagescandataBean;

public abstract class DICOMImageScanAttributes {
    private Class<? extends XnatImagescandataBean> imageScanDataBeanClass;

    protected DICOMImageScanAttributes(Class<? extends XnatImagescandataBean> imageScanDataBeanClass) {
        this.imageScanDataBeanClass = imageScanDataBeanClass;
    }

    Class<? extends XnatImagescandataBean> getImageScanDataBeanClass() {
        return imageScanDataBeanClass;
    }

    abstract AttrDefs get();
}