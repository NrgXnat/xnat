package org.nrg.dcm.xnat;

import org.nrg.dcm.AttrDefs;
import org.nrg.xdat.bean.XnatImagesessiondataBean;

public abstract class DICOMImageSessionAttributes {
    private Class<? extends XnatImagesessiondataBean> imageSessionDataBeanClass;

    protected DICOMImageSessionAttributes(Class<? extends XnatImagesessiondataBean> imageSessionDataBeanClass) {
        this.imageSessionDataBeanClass = imageSessionDataBeanClass;
    }

    Class<? extends XnatImagesessiondataBean> getImageSessionDataBeanClass() {
        return imageSessionDataBeanClass;
    }

    abstract AttrDefs get();
}
