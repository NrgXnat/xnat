/*
 * org.nrg.xnat.turbine.modules.actions.CreateExperiment
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.actions;

import org.apache.commons.lang.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;

public class CreateExperiment extends SecureAction {
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CreateExperiment.class);

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        if (TurbineUtils.HasPassedParameter("destination", data)){
            context.put("destination", TurbineUtils.GetPassedParameter("destination", data));
        }

        if (TurbineUtils.HasPassedParameter("tag", data)){
            context.put("tag", TurbineUtils.GetPassedParameter("tag", data));
        }
        
        String part_id = null;
        String project= null;
        String parent_expt_id= null;
        String visit= null;
        String subtype= null;
        String visit_name= null;
        
        if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data))!=null)
        {
            if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)).equalsIgnoreCase("xnat:subjectData.ID"))
            {
                if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data))!=null)
                {
                    part_id= ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
                }
            }
        }
        
        if (part_id==null){
            if (TurbineUtils.HasPassedParameter("subject_id", data)){
                part_id= (String)TurbineUtils.GetPassedParameter("subject_id", data);
            }
            if (TurbineUtils.HasPassedParameter("part_id", data)){
                part_id= (String)TurbineUtils.GetPassedParameter("part_id", data);
            }
        }
        
        if (part_id!=null){
                context.put("part_id", part_id);
                data.getParameters().setString("part_id", part_id);
        }
        
        if (TurbineUtils.HasPassedParameter("parent_expt_id", data)){
            parent_expt_id= (String)TurbineUtils.GetPassedParameter("parent_expt_id", data);
            context.put("parent_expt_id", parent_expt_id);
        }
        
        if (TurbineUtils.HasPassedParameter("project", data)){
            project= (String)TurbineUtils.GetPassedParameter("project", data);
            context.put("project", project);
        }
        
        if (TurbineUtils.HasPassedParameter("visit", data)){
        	visit= (String)TurbineUtils.GetPassedParameter("visit", data);
        	context.put("visit", visit);
        }

        if (TurbineUtils.HasPassedParameter("subtype", data)){
            subtype= (String)TurbineUtils.GetPassedParameter("subtype", data);
            // if coming to this page from certain other pages, 'subtype' may be termed as 'protocol'
            if (subtype == null) subtype= (String)TurbineUtils.GetPassedParameter("protocol", data);
            context.put("subtype", subtype);
        }
        
        if (TurbineUtils.HasPassedParameter("visit_name", data)){
        	visit_name= (String)TurbineUtils.GetPassedParameter("visit_name", data);
        	context.put("visit_name", visit_name);
        }

        String dataType = (String)TurbineUtils.GetPassedParameter("data_type", data);
        if (StringUtils.isBlank(dataType)) {
            final String page = String.format("%s/app/template/XDATScreen_add_experiment.vm", data.getContextPath());
            data.getResponse().sendRedirect(page);
        } else {
            SchemaElement se = SchemaElement.GetElement(dataType);
            data.setScreenTemplate("XDATScreen_edit_" + se.getFormattedName() + ".vm");
        }
    }

}
