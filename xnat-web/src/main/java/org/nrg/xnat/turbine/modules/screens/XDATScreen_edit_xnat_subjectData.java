/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_edit_xnat_subjectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.turbine.modules.screens.EditScreenA;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;

/**
 * @author Tim
 *
 */
public class XDATScreen_edit_xnat_subjectData extends EditScreenA {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#getElementName()
     */
    public String getElementName() {
        return "xnat:subjectData";
    }
    
    public ItemI getEmptyItem(RunData data) throws Exception
	{
	    String s = getElementName();
		ItemI temp =  XFTItem.NewItem(s,TurbineUtils.getUser(data));
		return temp;
	}

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.EditScreenA#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void finalProcessing(RunData data, Context context) {
        try {

            XnatSubjectdata subject = new XnatSubjectdata(item);
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
            
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data))!=null){
                context.put("project", ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data)));
            }
        } catch (Exception e) {
        }
    }

}
