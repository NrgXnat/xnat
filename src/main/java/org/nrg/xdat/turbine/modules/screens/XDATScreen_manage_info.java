/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_manage_info
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
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.security.UserI;

public class XDATScreen_manage_info extends AdminScreen {

    @Override
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        UserI user = TurbineUtils.getUser(data); 
        try {
            DisplaySearch search = UserHelper.getSearchHelperService().getSearchForUser(TurbineUtils.getUser(data),"xdat:infoEntry","listing");
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
