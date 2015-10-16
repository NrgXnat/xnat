/*
 * org.nrg.xdat.turbine.modules.actions.DisplayItemAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/21/14 9:52 AM
 */


package org.nrg.xdat.turbine.modules.actions;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.navigation.DefaultReportIdentifier;
import org.nrg.xdat.navigation.DefaultReportIdentifierI;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
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

        try{
            String className = XDAT.getSiteConfigurationProperty("UI.defaultReportIdentifier","org.nrg.xdat.navigation.DefaultReportIdentifier");
            Class clazz = Class.forName(className);
            Object o = clazz.newInstance();
            if(o instanceof DefaultReportIdentifierI){
                String templateName = ((DefaultReportIdentifier)o).identifyReport(data, context);
                data.setScreenTemplate(templateName);
            }
        }catch (Throwable e){
            logger.error("",e);
            TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
            data.setMessage(e.getMessage());
            data.setScreenTemplate("Error.vm");
        }

	}
	
	public static String GetReportScreen(SchemaElementI se)
	{
		String templateName = "/screens/XDATScreen_report_" + se.getSQLName() + ".vm";
		if (Velocity.resourceExists(templateName))
		{
			templateName= "XDATScreen_report_" + se.getSQLName() + ".vm";
		}else
		{
			templateName="DefaultReport.vm";
		}
		return templateName;
	}
	
	public static String GetReportScreen(String elementName) throws XFTInitException, ElementNotFoundException
	{
		return GetReportScreen(SchemaElement.GetElement(elementName));
	}

}

