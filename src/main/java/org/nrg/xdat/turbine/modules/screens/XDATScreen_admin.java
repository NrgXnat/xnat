//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 21, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.util.Hashtable;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTableI;
/**
 * @author Tim
 *
 */
public class XDATScreen_admin extends AdminScreen {
	public void doBuildTemplate(RunData data, Context context)
	{
		//TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		XDATUser user = TurbineUtils.getUser(data);	
		try {
			DisplaySearch search = user.getSearch(org.nrg.xft.XFT.PREFIX + ":user","listing");
			search.execute(new org.nrg.xdat.presentation.HTMLPresenter(TurbineUtils.GetContext(),false),TurbineUtils.getUser(data).getLogin());
			
			TurbineUtils.setSearch(data,search);
			
			XFTTableI table = search.getPresentedTable();

			Hashtable tableProps = new Hashtable();
			tableProps.put("bgColor","white"); 
			tableProps.put("border","0"); 
			tableProps.put("cellPadding","0"); 
			tableProps.put("cellSpacing","0"); 
			tableProps.put("width","95%"); 
			context.put("userTable",table.toHTML(false,"FFFFFF","DEDEDE",tableProps,(search.getCurrentPageNum() * search.getRowsPerPage())+ 1));
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

