/*
 * org.nrg.xdat.turbine.modules.actions.CSVUpload1
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
 */


package org.nrg.xdat.turbine.modules.actions;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.utils.FieldMapping;

public class CSVUpload1 extends SecureAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        preserveVariables(data,context);
        String fm_id = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("fm_id",data));
        File f = TurbineUtils.getUser(data).getCachedFile("csv/" + fm_id + ".xml");
        FieldMapping fm = new FieldMapping(f);
        String[] fields = ((String[])org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedObjects("fields",data));
        if (fields==null)
        {
            context.put("fm_id",fm_id);
            context.put("fm",fm);
            data.setScreenTemplate("XDATScreen_uploadCSV1.vm");
        }else{
            List fieldList =java.util.Arrays.asList(fields);
            for(Object field: fieldList){
                fm.getFields().add((String)field);
            }

            fm.saveToFile(f);
            
            context.put("fm_id",fm_id);
            context.put("fm",fm);
            data.setScreenTemplate("XDATScreen_uploadCSV2.vm");
        }
    }

    public void doPrep(RunData data, Context context) throws Exception {
        preserveVariables(data,context);
        
        String root = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("root_data_type",data));
        String title = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("title",data));
        if(root==null || root.equals("BAD") || title==null || title=="")
        {
            data.setMessage("Select a root data type.");
            data.setScreenTemplate("XDATScreen_uploadCSV.vm");
        }else{
            data.setScreenTemplate("XDATScreen_uploadCSV1.vm");
            String id = "" + Calendar.getInstance().getTimeInMillis();
            FieldMapping fm = new FieldMapping();
            fm.setElementName(root);
            fm.setTitle(title);
            fm.setID(id);
            File f = TurbineUtils.getUser(data).getCachedFile("csv/" + id + ".xml");
            f.getParentFile().mkdirs();
            fm.saveToFile(f);
            
            context.put("fm", fm);
            context.put("fm_id", id);
        }
    }
    
    
}
