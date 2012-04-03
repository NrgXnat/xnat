//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT ï¿½ Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 20, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.design.SchemaElementI;
/**
 * @author Tim
 *
 */
public class XDATActionRouter extends SecureAction
{
	static Logger logger = Logger.getLogger(XDATActionRouter.class);
   public void doPerform(RunData data, Context context){
       preserveVariables(data,context);
   		String action = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("xdataction",data));
   		if (action != null)
   		{
   			String elementName = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data));
   			if (elementName != null)
   			{
   				try {
					SchemaElementI se = SchemaElement.GetElement(elementName);
					String templateName = "/screens/XDATScreen_" + action  + "_" + se.getFormattedName() + ".vm";
					logger.debug("looking for: " + templateName);
					if (Velocity.templateExists(templateName))
					{
						data.setScreenTemplate("XDATScreen_" + action  + "_" + se.getFormattedName() + ".vm");
					}else
					{
					    templateName = "/screens/XDATScreen_" + action + ".vm";

					    logger.debug("looking for: " + templateName);
					    if (Velocity.templateExists(templateName))
						{
					        data.setScreenTemplate("XDATScreen_" + action + ".vm");
						}else
						{
						    templateName = "/screens/" + action   + "_" + se.getFormattedName() + ".vm";

						    logger.debug("looking for: " + templateName);
						    if (Velocity.templateExists(templateName))
							{
						        data.setScreenTemplate(action   + "_" + se.getFormattedName() + ".vm");
							}else
							{
							    templateName = "/screens/" + action   + ".vm";

							    logger.debug("looking for: " + templateName);
							    if (Velocity.templateExists(templateName))
								{
							        data.setScreenTemplate(action   + ".vm");
								}else
								{
								    data.setScreen("XDATScreen_" + action);
								}
							}
						}
					}
					} catch (XFTInitException e) {
						data.setScreenTemplate("XDATScreen_" + action + ".vm");
					} catch (ElementNotFoundException e) {
					data.setScreenTemplate("XDATScreen_" + action + ".vm");
				}
   			}else{
   			    String templateName = "/screens/XDATScreen_" + action   + ".vm";

			    logger.debug("looking for: " + templateName);
			    if (Velocity.templateExists(templateName))
				{
			        data.setScreenTemplate("XDATScreen_" + action   + ".vm");
				}else
				{
				    data.setScreenTemplate(action   + ".vm");
				}
   			}
   		}
   }
}
