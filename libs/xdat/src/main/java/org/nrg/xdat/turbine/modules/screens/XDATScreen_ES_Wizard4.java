/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_ES_Wizard4
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
public class XDATScreen_ES_Wizard4 extends AdminEditScreenA {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getElementName() {
        return ElementSecurity.SCHEMA_ELEMENT_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalProcessing(RunData data, Context context) {
        context.put("fields", getAllDefinedFieldsForSecurityElement());
    }
}
