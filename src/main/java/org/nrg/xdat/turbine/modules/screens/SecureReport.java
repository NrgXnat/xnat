//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 9, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;

/**
 * @author Tim
 *
 */
public abstract class SecureReport extends SecureScreen {
    protected ItemI item = null;
    protected ItemI om = null;

    public void preProcessing(RunData data, Context context)
    {

    }

    public abstract void finalProcessing(RunData data, Context context);
    public void noItemError(RunData data, Context context){
    	StringBuffer sb=new StringBuffer();
    	XDATUser u=TurbineUtils.getUser(data);
    	sb.append("We are unable to provide the requested data. Either you have encountered a link to erroneous data, or ");
    	if(u!=null){
        	sb.append("this user account (<b>" + u.getUsername() + "</b>) does not have access to this data.");
    	}else{
        	sb.append("this user account does not have access to this data.");
    	}

    	sb.append("<br><br>Please login with a different account or contact the source of the data to gain access.");

        data.setMessage(sb.toString());
        data.setScreenTemplate("UnauthorizedAccess.vm");
    }

    /**
     * Return null to use the defualt settings (which are configured in xdat:element_security).  Otherwise, true will force a pre-load of the item.
     * @return
     */
    public Boolean preLoad()
    {
        return null;
    }

	public void doBuildTemplate(RunData data, Context context)
	{
        preserveVariables(data,context);

	    logger.debug("BEGIN SECURE REPORT :" + this.getClass().getName());
	    preProcessing(data,context);

	    if(item==null){
	        item = TurbineUtils.getDataItem(data);
	    }

	    if (item== null)
		{
		    //System.out.println("No data item passed... looking for item passed by variables");
			try {
				ItemI temp = TurbineUtils.GetItemBySearch(data,preLoad());
			    item = temp;
			} catch (IllegalAccessException e1) {
                logger.error(e1);
			    data.setMessage(e1.getMessage());
				noItemError(data,context);
				return;
			} catch (Exception e1) {
                logger.error(e1);
                data.setMessage(e1.getMessage());
                data.setScreenTemplate("Error.vm");
                noItemError(data,context);
                return;
			}
		}

		if (item == null)
		{
			data.setMessage("Error: No item found.");
			noItemError(data,context);
		}else{
			try {
				if(XFT.VERBOSE)System.out.println("Creating report: " + getClass());;
			    context.put("item",item.getItem());
			    if(XFT.VERBOSE)System.out.println("Loaded item object (org.nrg.xft.ItemI) as context parameter 'item'.");
			    context.put("user",TurbineUtils.getUser(data));
			    if(XFT.VERBOSE)System.out.println("Loaded user object (org.nrg.xdat.security.XDATUser) as context parameter 'user'.");

            	context.put("element",org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));
            	if(data.getParameters().getString("search_element")!=null)
					context.put("search_element",data.getParameters().getString("search_element"));
				else
					context.put("search_element", item.getXSIType());

				if(data.getParameters().getString("search_field")!=null)
					context.put("search_field",data.getParameters().getString("search_field"));
				else
					context.put("search_field", item.getXSIType() + ".ID");

				if(data.getParameters().getString("search_value")!=null)
					context.put("search_value",data.getParameters().getString("search_value"));
				else
            		context.put("search_value", item.getProperty("ID"));

            	om = BaseElement.GetGeneratedItem(item);
            	context.put("om",om);

				finalProcessing(data,context);


			} catch (Exception e) {
				data.setMessage(e.getMessage());
				logger.error("",e);
			}
		}

	    logger.debug("END SECURE REPORT :" + this.getClass().getName());
	}

	public void logAccess(RunData data)
	{
	    String message="";
        try {
            message = data.getParameters().getString("search_element");
            message +=" " + data.getParameters().getString("search_value");
        } catch (Exception e) {
            logger.error("",e);
        }
		AccessLogger.LogScreenAccess(data,message);
	}
}

