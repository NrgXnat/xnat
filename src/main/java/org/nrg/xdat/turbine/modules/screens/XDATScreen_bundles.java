/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_bundles
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import java.util.Hashtable;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;

/**
 * @author Tim
 *
 */
public class XDATScreen_bundles extends AdminScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
//      TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		XDATUser user = TurbineUtils.getUser(data);	
		try {
		    DisplaySearch search = user.getSearch("xdat:stored_search","listing");
			search.setSortBy("xdat:stored_search.ID");
			search.execute(new org.nrg.xdat.presentation.HTMLPresenter(TurbineUtils.GetContext(),false),TurbineUtils.getUser(data).getLogin());
			
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
