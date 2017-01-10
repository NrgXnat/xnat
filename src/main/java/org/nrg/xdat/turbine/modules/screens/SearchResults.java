/*
 * core: org.nrg.xdat.turbine.modules.screens.SearchResults
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

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

import java.util.Calendar;
import java.util.Hashtable;
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
		
		context.put("search",search);
		
		context.put("listName",search.getTitle());

		context.put("numPages", search.getPages());
		context.put("currentPage", search.getCurrentPageNum());
		context.put("totalRecords", search.getNumRows());
		context.put("numToDisplay", search.getRowsPerPage());
		Hashtable<String, String> tableProps = new Hashtable<>();
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
        } catch (Exception ignored) {
        }
		
		if (search.isSuperSearch()){
		    context.put("searchType","none");
			String legend = "<DIV ALIGN='left'><TABLE><TR>";
			SchemaElement root = search.getRootElement();
			DisplayVersion dv = root.getDisplay().getVersion(search.getDisplay(),"default");
			legend += "<TD bgcolor='" + dv.getLightColor() + "'>" + dv.getBriefDescription() + "</TD>";
			for (final Object o : search.getAdditionalViews()) {
				String[] key = (String[]) o;
				try {
					SchemaElement sub = SchemaElement.GetElement(key[0]);
					if (!sub.getFullXMLName().equalsIgnoreCase(root.getFullXMLName())) {
						DisplayVersion subDv = sub.getDisplay().getVersion(key[1], "brief");
						legend += "<TD bgcolor='" + subDv.getLightColor() + "'>" + subDv.getBriefDescription() + "</TD>";
					}
				} catch (XFTInitException | ElementNotFoundException ignored) {
				}
			}
		
			legend += "</TR></TABLE>";
			context.put("legend",legend);
		}else if (search.isStoredSearch()){
		    context.put("searchType","none");
		}else{
		    String templateName = "/screens/" + search.getRootElement().getFormattedName() + "_search.vm";

		    logger.debug("looking for: " + templateName);
		    if (Velocity.resourceExists(templateName))
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
            if (TurbineUtils.GetPassedParameter("results_time",data) !=null){
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

