//Copyright 2006 Harvard University / Washington University School of Medicine All Rights Reserved
/*
 * Created on Oct 17, 2006
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.utils.FieldMapping;

public class CSVUpload1 extends SecureAction {

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        preserveVariables(data,context);
        String fm_id = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("fm_id",data));
        File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv/" + fm_id + ".xml");
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
            File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv/" + id + ".xml");
            f.getParentFile().mkdirs();
            fm.saveToFile(f);
            
            context.put("fm", fm);
            context.put("fm_id", id);
        }
    }
    
    
}
