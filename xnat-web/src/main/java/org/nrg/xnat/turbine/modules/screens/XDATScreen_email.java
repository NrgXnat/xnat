/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_edit_xnat_projectData
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
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.search.ItemSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author XDAT
 *
 */
public class XDATScreen_email extends SecureScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        ArrayList al = ItemSearch.GetAllItems("xdat:user",null,false).items();
        Collections.sort(al, new nameComparator());
        context.put("users",al);

        String s= (String) TurbineUtils.GetPassedParameter("emailTo",data,"");
        context.put("emailTo",s);
    }
    public class nameComparator implements Comparator<XFTItem> {
        public int compare(XFTItem object1, XFTItem object2) {
            String fn1 = (String) object1.getField("firstname");
            String ln1 = (String) object1.getField("lastname");
            String fn2 = (String) object2.getField("firstname");
            String ln2 = (String) object2.getField("lastname");
            if(fn1==null){
                return 1;
            }
            if(fn2==null){
                return -1;
            }
            int c = fn1.toUpperCase().compareTo(fn2.toUpperCase());
            if(c!=0){
                return c;
            }
            if(ln1==null){
                return 1;
            }
            if(ln2==null){
                return -1;
            }
            return ln1.toUpperCase().compareTo(ln2.toUpperCase());
        }
    }
}
