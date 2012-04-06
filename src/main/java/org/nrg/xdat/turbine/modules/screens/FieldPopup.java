//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Nov 30, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;

/**
 * @author Tim
 *
 */
public class FieldPopup extends SecureScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        String s = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("element_name",data));
        context.put("element_name",s);
        
        String textID = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("textID",data));
        context.put("textID",textID);
        
        String headerID = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("headerID",data));
        if (headerID != null)
            context.put("headerID",headerID);
        
        String typeID = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("typeID",data));
        if (typeID != null)
            context.put("typeID",typeID);
        
        if (s != null)
        {
            SchemaElement se = SchemaElement.GetElement(s);
            ElementDisplay ed = se.getDisplay();
            ArrayList displays = ed.getSortedFields();
            
            ArrayList dfs = new ArrayList();
            Iterator iter = displays.iterator();
            while (iter.hasNext())
            {
                DisplayField df = (DisplayField)iter.next();
               String id = df.getId();
               String summary = df.getSummary();
               String header = df.getHeader();
               String type = df.getDataType();
               
               ArrayList sub = new ArrayList();
               sub.add(id);
               sub.add(header);
               sub.add(summary);
               sub.add(type);
               dfs.add(sub);
            }
            
            context.put("fields",dfs);
        }else{
            
        }
    }

}
