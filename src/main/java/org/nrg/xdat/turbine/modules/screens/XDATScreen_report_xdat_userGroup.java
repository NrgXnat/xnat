/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_report_xdat_userGroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class XDATScreen_report_xdat_userGroup extends SecureReport {

    @Override
    public void finalProcessing(RunData data, Context context) {
        XdatUsergroup group = (XdatUsergroup)om;
        try {
        	List<List<Object>> permisionItems = group.getPermissionItems(TurbineUtils.getUser(data).getUsername());
            boolean allSet = true;
            Iterator pis = permisionItems.iterator();
            while (pis.hasNext())
            {
                ArrayList al = (ArrayList) pis.next();
                if (!((Boolean)al.get(4)).booleanValue())
                {
                    allSet = false;
                }
            }
            context.put("allSet", new Boolean(allSet));
            context.put("allElements",permisionItems);
        } catch (Exception e) {
            logger.error("",e);
        }
    }

}
