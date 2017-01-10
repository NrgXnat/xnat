/*
 * core: org.nrg.xdat.turbine.modules.actions.ManageDataTypes
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatElementSecurity;
import org.nrg.xdat.om.XdatSecurity;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;

import java.util.ArrayList;

public class ManageDataTypes extends AdminAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        UserI user=TurbineUtils.getUser(data);
        PopulateItem populater = PopulateItem.Populate(data,"xdat:security",true);
                
        XFTItem found = populater.getItem();
        XdatSecurity sec = new XdatSecurity(found);
        
        ArrayList<XdatElementSecurity> ess=sec.getElementSecuritySet_elementSecurity();
        
       
        for(XdatElementSecurity es : ess){
            if (es.getProperty("accessible")==null){
                es.setAccessible("false");
            }

            if (es.getProperty("secure")==null){
                es.setSecure("false");
            }

            if (es.getProperty("browse")==null){
                es.setBrowse("false");
            }

            if (es.getProperty("searchable")==null){
                es.setSearchable("false");
            }
            
            SaveItemHelper.authorizedSave(es,user, false, false,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified Data-type (batch)" ));
        }
        
        ElementSecurity.refresh();
        
        data.setMessage("Data-Types modified.");

        data.setScreenTemplate("XDATScreen_dataTypes.vm");
    }

}
