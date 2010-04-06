//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jul 1, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.util.List;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;

/**
 * @author Tim
 *
 */
public class XDATScreen_activate_xdat_user extends AdminScreen {

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.SecureReport#finalProcessing(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doBuildTemplate(RunData data, Context context)
	{
	    try {
            ItemI item = TurbineUtils.GetItemBySearch(data);
            if (item == null)
            {
            	data.setMessage("Error: No item found.");
            }else{
            	try {
            	    context.put("item",item);
            	    context.put("user",TurbineUtils.getUser(data));
            	    
                	context.put("element",org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));
                	context.put("search_element",data.getParameters().getString("search_element"));
                	context.put("search_field",data.getParameters().getString("search_field"));
                	context.put("search_value",data.getParameters().getString("search_value"));

                	XDATUser tempUser = new XDATUser(item);
        			context.put("userObject",tempUser);
        			List<List<Object>> permisionItems = tempUser.getPermissionItems();
        			context.put("allElements",permisionItems);
            	} catch (Exception e) {
            		data.setMessage(e.toString());
            	}
            }
        } catch (Exception e) {
            logger.error("",e);
        	data.setMessage("Error: No item found.");
        }
		
	}

}
