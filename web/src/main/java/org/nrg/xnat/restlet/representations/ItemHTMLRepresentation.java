/*
 * web: org.nrg.xnat.restlet.representations.ItemHTMLRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.representations;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.turbine.util.TurbineException;
import org.nrg.xdat.turbine.modules.actions.DisplayItemAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.restlet.data.MediaType;
import org.restlet.data.Request;

public class ItemHTMLRepresentation extends TurbineScreenRepresentation {
	static org.apache.log4j.Logger logger = Logger.getLogger(ItemHTMLRepresentation.class);
	private final String screen;
	
	public ItemHTMLRepresentation(XFTItem i,MediaType mt,Request request,UserI _user,Map<String,Object> params) throws TurbineException,ElementNotFoundException {
		super(mt,request,_user,params);
		
		TurbineUtils.setDataItem(data, i);
		 
		try {
			if(i.getProperty("project")!=null){
				data.getParameters().setString("project", i.getStringProperty("project"));
			}
		} catch (Throwable e1) {
			logger.error("",e1);
		}
		
		
		screen = DisplayItemAction.GetReportScreen(i.getItem().getGenericSchemaElement());
	}
	
	public ItemHTMLRepresentation(XFTItem i,MediaType mt,Request request,UserI _user,String requested_screen,Map<String,Object> params) throws TurbineException,ElementNotFoundException {
		super(mt,request,_user,params);
		
		TurbineUtils.setDataItem(data, i);
		 
		try {
			if(i.getProperty("project")!=null){
				data.getParameters().setString("project", i.getStringProperty("project"));
			}
		} catch (Throwable e1) {
			logger.error("",e1);
		}
		
		
		if(requested_screen==null){
			screen = DisplayItemAction.GetReportScreen(i.getItem().getGenericSchemaElement());
		}else{
			if(!requested_screen.endsWith(".vm"))requested_screen+=".vm";
			screen = requested_screen;
		}
	}

	@Override
	public String getScreen() {
		return screen;
	}
	
}
