/*
 * core: org.nrg.xdat.turbine.modules.screens.FieldPopup
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tim
 *
 */
public class FieldPopup extends SecureScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(PipelineData pipelineData, Context context)
            throws Exception {
        final RunData data = pipelineData.getRunData();
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
            ElementDisplay           ed       = se.getDisplay();
            final List<DisplayField> displays = ed.getSortedFields();
            final List<ArrayList> dfs = displays.stream().map(df -> {
                String id      = df.getId();
                String summary = df.getSummary();
                String header  = df.getHeader();
                String type    = df.getDataType();

                ArrayList sub = new ArrayList();
                sub.add(id);
                sub.add(header);
                sub.add(summary);
                sub.add(type);
                return sub;
            }).collect(Collectors.toList());

            context.put("fields",dfs);
        }
    }

}
