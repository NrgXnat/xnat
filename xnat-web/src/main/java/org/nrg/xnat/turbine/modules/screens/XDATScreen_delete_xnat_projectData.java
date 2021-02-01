/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_delete_xnat_projectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatAbstractprojectasset;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xft.security.UserI;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class XDATScreen_delete_xnat_projectData extends SecureReport {
    @Override
    public void finalProcessing(final RunData data, final Context context) {
        final UserI                          user = XDAT.getUserDetails();
        final XnatProjectdata                om   = (XnatProjectdata) context.get("om");
        final List<XnatAbstractprojectasset> projectAssets;
        if (om != null) {
            projectAssets = XnatAbstractprojectasset.getXnatAbstractprojectassetsByField(XnatAbstractprojectasset.SCHEMA_ELEMENT_NAME + "/project", om.getId(), user, false);
        } else {
            projectAssets = Collections.emptyList();
        }
        context.put("projectAssets", projectAssets);
    }
}
