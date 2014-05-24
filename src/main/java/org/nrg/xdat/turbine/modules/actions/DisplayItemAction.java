//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 18, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.schema.design.SchemaElementI;
/**
 * @author Tim
 *
 */
public class DisplayItemAction extends SecureAction {
	static Logger logger = Logger.getLogger(DisplayItemAction.class);
	public void doPerform(RunData data, Context context) throws Exception
	{
        preserveVariables(data,context);
//		try {
		    try {
                logger.debug("BEGIN DisplayItemAction");
                SchemaElementI se = TurbineUtils.GetSchemaElementBySearch(data);
                
                if (se != null)
                {
                	String templateName = GetReportScreen(se);
                	data.setScreenTemplate(templateName);
                	logger.info("Routing request to '" + templateName + "'");
                }else{
                	logger.error("No Element Found. ");
                	TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
    	            data.setMessage("No Item Found.");
    	          	data.setScreenTemplate("Error.vm");
                }
            } catch (Exception e) {
                logger.error("",e);
	            TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
	            data.setMessage(e.getMessage());
	          	data.setScreenTemplate("Error.vm");
            }
//        } catch (Exception e) {
//            logger.error("",e);
//            TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
//            data.setMessage(e.getMessage());
//        	data.setScreenTemplate("DefaultReport.vm");
//        }
	    logger.debug("END DisplayItemAction");
	}
	
	public static String GetReportScreen(SchemaElementI se)
	{
		String templateName = "/screens/XDATScreen_report_" + se.getSQLName() + ".vm";
		if (Velocity.templateExists(templateName))
		{
			templateName= "XDATScreen_report_" + se.getSQLName() + ".vm";
		}else
		{
			templateName="DefaultReport.vm";
		}
		return templateName;
	}
}

