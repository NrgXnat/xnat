//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jul 1, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.Authorizer;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.SaveItemHelper;

/**
 * @author Tim
 *
 */
public class ActivateAction extends SecureAction{
    static Logger logger = Logger.getLogger(ActivateAction.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
        preserveVariables(data,context);
	    ItemI item = activate(data,context);
	    if (item ==null)
	    {
	        data.setScreenTemplate("Error.vm");
	    }else{
		    String s = DisplayItemAction.GetReportScreen(new SchemaElement(((XFTItem)item).getGenericSchemaElement()));
		    data.setScreenTemplate(s);
		    
		    if (item.getXSIType().equalsIgnoreCase("xdat:user"))
		    {
			    data.setMessage("<p>User Authorized.</p>");
		    }else{
			    data.setMessage("<p>Item Activated.</p>");
		    }

		    TurbineUtils.SetSearchProperties(data,item);
		    TurbineUtils.setDataItem(data,item);
	    }
	}
	
	public static ItemI activate(RunData data, Context context) throws Exception
	{
	    ItemI o = null;
	    try {
			o = TurbineUtils.GetItemBySearch(data,true);
			if (o != null)
			{		  
				Authorizer.getInstance().authorizeSave(o.getItem().getGenericSchemaElement(), TurbineUtils.getUser(data));
				
				o.activate(TurbineUtils.getUser(data));
				SchemaElementI se = SchemaElement.GetElement(o.getXSIType());
				if (se.getFullXMLName().startsWith("xdat:"))
				{
					o = o.getCurrentDBVersion();
				}else{
				    o = o.getCurrentDBVersion();
				}
				return o;
			}else{
			  	logger.error("No Item Found.");
			  	TurbineUtils.OutputDataParameters(data);
			  	return null;
			}
		} catch (Exception e) {
			logger.error("Activation",e);
			return null;
		}
	}
}
