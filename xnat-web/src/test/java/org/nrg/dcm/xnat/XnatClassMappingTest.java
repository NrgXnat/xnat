/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.XnatClassMappingTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.xnat;

import org.junit.Test;
import org.nrg.xdat.bean.*;
import org.nrg.xdat.bean.base.BaseElement;

import static org.junit.Assert.assertEquals;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class XnatClassMappingTest {

    /**
     * Test method for {@link org.nrg.dcm.xnat.XnatClassMapping#getBeanClass(java.lang.String)}.
     */
    @Test
    public void testgetBeanClass() throws ClassNotFoundException {
        assertEquals(XnatMrscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("mrScanData"));
        assertEquals(XnatCrscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("crScanData"));
        assertEquals(XnatCtscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("ctScanData"));
        assertEquals(XnatDx3dcraniofacialscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("dx3DCraniofacialScanData"));
        assertEquals(XnatEcgscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("ecgScanData"));
        assertEquals(XnatEpsscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("epsScanData"));
        assertEquals(XnatEsvscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("esvScanData"));
        assertEquals(XnatGmvscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("gmvScanData"));
        assertEquals(XnatHdscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("hdScanData"));
        assertEquals(XnatIoscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("ioScanData"));
        assertEquals(XnatMgscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("mgScanData"));
        assertEquals(XnatNmscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("nmScanData"));
        assertEquals(XnatOpscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("opScanData"));
        assertEquals(XnatMgscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("mgScanData"));
        assertEquals(XnatOptscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("optScanData"));
        assertEquals(XnatOtherdicomscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("otherDicomScanData"));
        assertEquals(XnatPetscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("petScanData"));
        assertEquals(XnatRfscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("rfScanData"));
        assertEquals(XnatRtimagescandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("rtImageScanData"));
        assertEquals(XnatScscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("scScanData"));
        assertEquals(XnatUsscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("usScanData"));
        assertEquals(XnatXascandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("xaScanData"));
        assertEquals(XnatXcvscandataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("xcvScanData"));

        assertEquals(XnatMrsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("mrSessionData"));
        assertEquals(XnatCrsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("crSessionData"));
        assertEquals(XnatCtsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("ctSessionData"));
        assertEquals(XnatDx3dcraniofacialsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("dx3DCraniofacialSessionData"));
        assertEquals(XnatEcgsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("ecgSessionData"));
        assertEquals(XnatEpssessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("epsSessionData"));
        assertEquals(XnatEsvsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("esvSessionData"));
        assertEquals(XnatGmvsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("gmvSessionData"));
        assertEquals(XnatHdsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("hdSessionData"));
        assertEquals(XnatIosessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("ioSessionData"));
        assertEquals(XnatMgsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("mgSessionData"));
        assertEquals(XnatNmsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("nmSessionData"));
        assertEquals(XnatOpsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("opSessionData"));
        assertEquals(XnatMgsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("mgSessionData"));
        assertEquals(XnatOptsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("optSessionData"));
        assertEquals(XnatOtherdicomsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("otherDicomSessionData"));
        assertEquals(XnatPetsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("petSessionData"));
        assertEquals(XnatRfsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("rfSessionData"));
        assertEquals(XnatRtsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("rtSessionData"));
        assertEquals(XnatUssessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("usSessionData"));
        assertEquals(XnatXasessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("xaSessionData"));
        assertEquals(XnatXcvsessiondataBean.class, XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("xcvSessionData"));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testNoSuchBeanClass() throws ClassNotFoundException {
        XnatClassMapping.forBaseClass(BaseElement.class).getBeanClass("scSessionData");
    }
}
