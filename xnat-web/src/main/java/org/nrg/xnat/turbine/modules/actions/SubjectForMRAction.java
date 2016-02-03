/*
 * org.nrg.xnat.turbine.modules.actions.SubjectForMRAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;

/**
 * @author Tim
 *
 */
public class SubjectForMRAction extends SecureAction {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) throws Exception {
        String s = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("part_id",data));
        CriteriaCollection cc = new CriteriaCollection("OR");
        cc.addClause("xnat:subjectData/ID",s);
        
        String destination = (String)TurbineUtils.GetPassedParameter("destination", data,"XDATScreen_edit_xnat_mrSessionData.vm");
                
//        CriteriaCollection sub = new CriteriaCollection("AND");
//        sub.addClause("",XNATUtils.MAP_COLUMN_NAME);
//        sub.addClause("xnat:subjectData/addID/addID",s);
//        cc.add(sub);
//
//        sub = new CriteriaCollection("AND");
//        sub.addClause("xnat:subjectData/addID/name",XNATUtils.LAB_COLUMN_NAME);
//        sub.addClause("xnat:subjectData/addID/addID",s);
//        cc.add(sub);


        if (TurbineUtils.HasPassedParameter("tag", data)){
            context.put("tag", TurbineUtils.GetPassedParameter("tag", data));
        }
        
        ItemCollection items = ItemSearch.GetItems(cc,TurbineUtils.getUser(data),false);
        if (items.size()>0)
        {
            ItemI item = items.getFirst();
            boolean confirmed = false;
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("confirmed",data))!=null)
            {
                if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("confirmed",data)).equalsIgnoreCase("true"))
                {
                    confirmed = true;
                }
            }
            
            if (!confirmed)
            {
                TurbineUtils.setDataItem(data,item);
                context.put("destination", destination);
                data.setScreenTemplate("VerifySubjectForExperiment.vm");
            }else{
                data.getParameters().add("part_id",item.getStringProperty("ID"));
                data.setScreenTemplate(destination);
            }
        }else{
            if (s!=null){
                data.setMessage("Invalid Subject Id");
                ValidationResults vr = new ValidationResults();
                vr.addResult(null,"Invalid Subject Id","part_id",(String)null);
                context.put("vr",vr);
                context.put("part_id",s);
            }
            data.setScreenTemplate("XDATScreen_add_experiment.vm");
        }
    }

}
