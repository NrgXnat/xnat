//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jun 23, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.search.ItemSearch;

/**
 * @author Tim
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
            int c = fn1.compareTo(fn2);
            if(c!=0){
                return c;
            }
            return ln1.compareTo(ln2);
        }
    }
}
