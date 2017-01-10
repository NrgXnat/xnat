/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_investigators
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import java.util.Hashtable;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 *
 */
public class XDATScreen_investigators extends SecureScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        UserI user = TurbineUtils.getUser(data);	
		try {
		    DisplaySearch search = UserHelper.getSearchHelperService().getSearchForUser(TurbineUtils.getUser(data),"xnat:investigatorData","listing");
			search.execute(new org.nrg.xdat.presentation.HTMLPresenter(TurbineUtils.GetContext(),false),user.getLogin());
			
			TurbineUtils.setSearch(data,search);
			
			XFTTableI table = search.getPresentedTable();

			Hashtable tableProps = new Hashtable();
			tableProps.put("bgColor","white"); 
			tableProps.put("border","0"); 
			tableProps.put("cellPadding","0"); 
			tableProps.put("cellSpacing","0"); 
			tableProps.put("width","95%"); 
			context.put("dataTable",table.toHTML(false,"FFFFFF","DEDEDE",tableProps,(search.getCurrentPageNum() * search.getRowsPerPage())+ 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
