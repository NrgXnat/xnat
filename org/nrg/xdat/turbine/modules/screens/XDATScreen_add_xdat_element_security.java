//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 3, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.ElementSecurity;
import java.util.*;

/**
 * @author Tim
 *
 */
public class XDATScreen_add_xdat_element_security extends AdminScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        ArrayList allAbsent = ElementSecurity.GetAbsentElements();
        context.put("elements",allAbsent);
    }

}
