/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_mr_super_search
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;

import java.util.Hashtable;

/**
 * @author Tim
 *
 */
public class XDATScreen_mr_super_search extends SecureScreen {

    /* (non-Javadoc)
     * @see org.cnl.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doBuildTemplate(RunData data, Context context) {
        try {
            Hashtable hash = ElementSecurity.GetDistinctIdValuesFor("xnat:investigatorData","default",TurbineUtils.getUser(data).getLogin());
            context.put("investigators",hash);
            SchemaElement se = SchemaElement.GetElement("xnat:mrSessionData");
            context.put("sessionElement",se);
            SchemaElement subject = SchemaElement.GetElement("xnat:subjectData");
            context.put("subjectElement",subject);
        } catch (Exception e) {
        }
    }

}
