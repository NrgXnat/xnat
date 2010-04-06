//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Apr 21, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.schema.design.SchemaElementI;

/**
 * @author Tim
 *
 */
public class EditItemAction extends SecureAction {

    static Logger logger = Logger.getLogger(EditItemAction.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
        preserveVariables(data,context);
		try {
            ItemI o = TurbineUtils.GetItemBySearch(data);
            
            if (o != null)
            {
            	TurbineUtils.SetEditItem(o,data);
            	
            	SchemaElementI se = SchemaElement.GetElement(o.getXSIType());
            	
            	String templateName = GetEditScreen(se);
            	data.setScreenTemplate(templateName);
            	 
            	
            	logger.info("Routing request to '" + templateName + "'");
            }else{
            	logger.error("No Item Found.");
            	TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
            	data.setScreenTemplate("Index.vm");
            }
        } catch (Exception e) {
            logger.error("",e);
            TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
            data.setMessage(e.getMessage());
        	data.setScreenTemplate("Index.vm");
        }
	}
	
	public static String GetEditScreen(SchemaElementI se)
	{
		String templateName = "/screens/XDATScreen_edit_" + se.getSQLName() + ".vm";
		if (Velocity.templateExists(templateName))
		{
			templateName= "XDATScreen_edit_" + se.getSQLName() + ".vm";
		}else
		{
			templateName="XDATScreen_edit.vm";
		}
		return templateName;
	}
}
