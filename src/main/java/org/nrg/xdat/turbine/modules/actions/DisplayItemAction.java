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
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.StringUtils;

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

