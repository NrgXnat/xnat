//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT ï¿½ Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayVersion;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
/**
 * @author Tim
 *
 */
public class SearchResults extends SecureScreen {
    private DisplaySearch _search = null;
    private long startTime = Calendar.getInstance().getTimeInMillis();
    
    public DisplaySearch getSearch(RunData data){
        if (_search==null)
        {
            _search = TurbineUtils.getSearch(data);
        }
        
        return _search;
    }
    
	/* (non-Javadoc)
	 * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	public void doBuildTemplate(RunData data, Context context)
	{
		DisplaySearch search = getSearch(data);
		XFTTableI table = search.getPresentedTable();
		
		if (table.size()==1 && !(search.isSuperSearch() || search.getFields().size()>0))
		{
		    
		}
        		
		context.put("search",search);
		
		context.put("listName",search.getTitle());

		context.put("numPages",new Integer(search.getPages()));
		context.put("currentPage",new Integer(search.getCurrentPageNum()));
		context.put("totalRecords",new Integer(search.getNumRows()));
		context.put("numToDisplay",new Integer(search.getRowsPerPage()));
		Hashtable tableProps = new Hashtable();
		tableProps.put("bgColor","white"); 
		tableProps.put("border","0"); 
		tableProps.put("cellPadding","0"); 
		tableProps.put("cellSpacing","0"); 
		tableProps.put("width","95%"); 
		context.put("dataTable",table.toHTML(false,"FFFFFF","DEDEDE",tableProps,(search.getCurrentPageNum() * search.getRowsPerPage())+ 1));
		
		context.put("schemaElement",search.getRootElement());
		
		try {
            Hashtable hash = ElementSecurity.GetDistinctIdValuesFor("Investigator","default",TurbineUtils.getUser(data).getLogin());
            context.put("investigators",hash);
        } catch (Exception e1) {
        }
		
		if (search.isSuperSearch()){
		    context.put("searchType","none");
			String legend = "<DIV ALIGN='left'><TABLE><TR>";
			SchemaElement root = search.getRootElement();
			DisplayVersion dv = root.getDisplay().getVersion(search.getDisplay(),"default");
			legend += "<TD bgcolor='" + dv.getLightColor() + "'>" + dv.getBriefDescription() + "</TD>";
			Iterator keys = search.getAdditionalViews().iterator();
			while (keys.hasNext())
			{
				String[] key = (String[])keys.next();
				try {
					SchemaElement sub = SchemaElement.GetElement(key[0]);
					if (!sub.getFullXMLName().equalsIgnoreCase(root.getFullXMLName()))
					{
						DisplayVersion subDv = sub.getDisplay().getVersion(key[1],"brief");
						legend += "<TD bgcolor='" + subDv.getLightColor() + "'>" + subDv.getBriefDescription() + "</TD>";
					}
				} catch (XFTInitException e) {
				} catch (ElementNotFoundException e) {
				}
			}
		
			legend += "</TR></TABLE>";
			context.put("legend",legend);
		}else if (search.isStoredSearch()){
		    context.put("searchType","none");
		}else{
		    String templateName = "/screens/" + search.getRootElement().getFormattedName() + "_search.vm";

		    logger.debug("looking for: " + templateName);
		    if (Velocity.templateExists(templateName))
			{
		        context.put("searchType",templateName);
			}else
			{
		        context.put("searchType","generate");
			}
		}
        
        if (TurbineUtils.getUser(data).getLogin().equals("tolsen"))
        {
            long results_time = 0;
            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("results_time",data))!=null){
                results_time += ((Long)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("results_time",data));
            }
            if (context.get("results_time")!=null){
                results_time += (Long)context.get("results_time");
            }
            long localTime=Calendar.getInstance().getTimeInMillis()-startTime;
            context.put("results_time", results_time + localTime);
        }
	}
    
    public void logAccess(RunData data)
    {
        String message = "";
        try {
            DisplaySearch search = getSearch(data);
            if (search!=null){
                message = search.getTitle() + " (" + search.getCurrentPageNum() + ")";
            }
        } catch (Throwable e) {
            logger.error("",e);
        }
        AccessLogger.LogScreenAccess(data,message);
    }
}

