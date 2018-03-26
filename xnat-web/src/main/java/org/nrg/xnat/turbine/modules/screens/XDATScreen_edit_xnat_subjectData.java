/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_edit_xnat_subjectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.TurbineUtils;

/**
 * @author Tim
 *
 */
@SuppressWarnings("unused")
public class XDATScreen_edit_xnat_subjectData extends EditScreenA {
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
     */
    public String getElementName() {
        return XnatSubjectdata.SCHEMA_ELEMENT_NAME;
    }
    
    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        try {
            XnatSubjectdata subject = new XnatSubjectdata(getEditItem());
            context.put("subject",subject);
            if (TurbineUtils.HasPassedParameter("destination", data)){
                context.put("destination", TurbineUtils.GetPassedParameter("destination", data));
            }
            
            if (subject.getProperty("ID")==null)
            {
		context.put("page_title", "Enter a new "
			+ DisplayManager.GetInstance().getSingularDisplayNameForSubject().toLowerCase());
            }else{
		context.put("page_title", "Edit an existing "
			+ DisplayManager.GetInstance().getSingularDisplayNameForSubject().toLowerCase());
            }
            
            if (TurbineUtils.GetPassedParameter("project",data) != null){
                context.put("project", TurbineUtils.GetPassedParameter("project", data));
            }
        } catch (Exception ignored) {
        }
    }

}
