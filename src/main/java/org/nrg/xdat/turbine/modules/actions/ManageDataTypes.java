//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Mar 14, 2008
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.ArrayList;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatElementSecurity;
import org.nrg.xdat.om.XdatSecurity;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.event.EventUtils;

public class ManageDataTypes extends AdminAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        XDATUser user=TurbineUtils.getUser(data);
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
                es.setBrowse("false");
            }
            
            SaveItemHelper.authorizedSave(es,user, false, false,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified Data-type (batch)" ));
        }
        
        ElementSecurity.refresh();
        
        data.setMessage("Data-Types modified.");
    }

}
