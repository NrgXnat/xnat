package org.nrg.xnat.archive;

import org.nrg.xdat.om.base.BaseXnatExperimentdata;

/**
 * Created by jordan on 7/6/17.
 */
public interface CurrentArcIdentifierI {
    public String identify(BaseXnatExperimentdata expt);
}

// XDAT.getContextService().getBeansOfType(Object.class)[3].getValue().getBeansFactory().getAllBeanNamesByType()[15]
// XDAT.getContextService().getBeansOfType(Object.class).get("org.springframework.context.annotation.internalRequiredAnnotationProcessor").beanFactory.allBeanNamesByType
//org.springframework.context.annotation.internalRequiredAnnotationProcessor=org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor@564419d0/

// ((org.springframework.beans.factory.support.DefaultListableBeanFactory)XDAT.getContextService().getBeansOfType(Object.class).get("org.springframework.context.annotation.internalRequiredAnnotationProcessor").beanFactory).allBeanNamesByType.get("interface org.nrg.xnat.archive.Curren)

//org.nrg.xnat.archive.CurrentArcIdentifierI.class