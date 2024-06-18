/*
 * core: org.nrg.xdat.turbine.modules.actions.InitXDAT
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */



package org.nrg.xdat.turbine.modules.actions;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
public class InitXDAT extends VelocitySecureAction
{
	static Logger logger = Logger.getLogger(InitXDAT.class);

   public void doPerform(RunData data, Context context){
   	logger.debug("InitXDAT BEGIN");
   	try{
   		//XDAT.init("C:\\jakarta-tomcat-5.5.4\\webapps\\cnda4\\WEB-INF\\conf\\XDAT\\");
	    java.util.ArrayList l = org.nrg.xft.XFTTool.GetPossibleElements();
	    java.util.Iterator iter = l.iterator();
	    while (iter.hasNext())
	    {
	    	String name = (String)iter.next();
	    	logger.info(name);
	    }
	   }catch(Exception ex)
	   {
	   	logger.debug(ex.toString());
	   }
   	logger.debug("InitXDAT END");
   }
    protected boolean isAuthorized( RunData data ) throws Exception
    {
        return true;
    }
}

