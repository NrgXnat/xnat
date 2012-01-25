//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author Tim
 *
 */
public abstract class SecureScreen extends VelocitySecureScreen
{
	public final static Logger logger = Logger.getLogger(SecureScreen.class);
    private static Pattern _pattern = Pattern.compile("\\A<!-- ([A-z_]+?): (.+) -->\\Z");


    protected void error(Exception e,RunData data){
        logger.error("",e);
        data.setScreenTemplate("Error.vm");
        data.getParameters().setString("exception", e.toString());
    }

    protected void preserveVariables(RunData data, Context context){
        if (data.getParameters().containsKey("project")){
        	if(XFT.VERBOSE)System.out.println(this.getClass().getName() + ": maintaining project '" + data.getParameters().getString("project") +"'");
            context.put("project", data.getParameters().getString("project"));
        }
    }

	/**
     * This method overrides the method in VelocitySecureScreen to
     * perform a security check first and store the popup status in the context.
     *
     * @param data Turbine information.
     * @exception Exception, a generic exception.
     */
	protected void doBuildTemplate(RunData data)
    throws Exception
	{
	    try {
            if (isAuthorized(data))
            {
                Context c = TurbineVelocity.getContext(data);
                if (data.getParameters().getString("popup")!=null)
                {
                    if(data.getParameters().getString("popup").equalsIgnoreCase("true"))
                    {
                        c.put("popup","true");
                    }else{
                        c.put("popup","false");
                    }
                }else{
                    c.put("popup","false");
                }

                String systemName = TurbineUtils.GetSystemName();
                c.put("turbineUtils",TurbineUtils.GetInstance());
                c.put("systemName",systemName);
                preserveVariables(data,c);
                doBuildTemplate(data, c);
            }else{
                Context c = TurbineVelocity.getContext(data);
                if (data.getParameters().getString("popup")!=null)
                {
                    if(data.getParameters().getString("popup").equalsIgnoreCase("true"))
                    {
                        c.put("popup","true");
                    }else{
                        c.put("popup","false");
                    }
                }else{
                    c.put("popup","false");
                }
                String systemName = TurbineUtils.GetSystemName();
                c.put("turbineUtils",TurbineUtils.GetInstance());
                c.put("systemName",systemName);
                preserveVariables(data,c);

                if (XFT.GetRequireLogin())
                {
                }else{
                    data.setScreenTemplate("Login.vm");
                }
            }
        } catch (RuntimeException e) {
            logger.error("",e);
            data.setScreenTemplate("Error.vm");
            return;
        }
	}
	/**
	 * Overide this method to perform the security check needed.
	 *
	 * @param data Turbine information.
	 * @return True if the user is authorized to access the screen.
	 * @exception Exception, a generic exception.
	 */
	protected boolean isAuthorized( RunData data )  throws Exception
	{
	    //TurbineUtils.OutputDataParameters(data);
	    if (XFT.GetRequireLogin() || TurbineUtils.HasPassedParameter("par", data))
		{
	        logger.debug("isAuthorized() Login Required:true");
            TurbineVelocity.getContext(data).put("logout","true");
			data.getParameters().setString("logout","true");
			boolean isAuthorized = false;

			XDATUser user = TurbineUtils.getUser(data);
			if (user == null)
			{
		        //logger.debug("isAuthorized() Login Required:true user:null");
				String Destination = data.getTemplateInfo().getScreenTemplate();
				data.getParameters().add("nextPage", Destination);
				if (!data.getAction().equalsIgnoreCase(""))
					data.getParameters().add("nextAction",data.getAction());
				else
					data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login"));
				//System.out.println("nextPage::" + data.getParameters().getString("nextPage") + "::nextAction" + data.getParameters().getString("nextAction") + "\n");
				doRedirect(data,org.apache.turbine.Turbine.getConfiguration().getString("template.login"));

			}else
			{

		        //logger.debug("isAuthorized() Login Required:true user:found");
				isAuthorized = true;
				if (data.getParameters().getString("popup") != null)
				{
					if (data.getParameters().getString("popup").equalsIgnoreCase("true"))
					{
						data.getTemplateInfo().setLayoutTemplate("/Popup.vm");
					}
				}else
				{
					data.getParameters().setString("popup","false");
				}

				logAccess(data);
			}

			return isAuthorized;
		}else{
            boolean isAuthorized = true;
	        logger.debug("isAuthorized() Login Required:false");
			XDATUser user = TurbineUtils.getUser(data);
			if (user ==null)
			{
                if (!allowGuestAccess())isAuthorized=false;

                HttpSession session = data.getSession();
                session.removeAttribute("loggedin");
				ItemSearch search = new ItemSearch();
				SchemaElementI e = SchemaElement.GetElement(XDATUser.USER_ELEMENT);
				search.setElement(e.getGenericXFTElement());
				search.addCriteria(XDATUser.USER_ELEMENT +"/login", "guest");
				ItemCollection items = search.exec(true);
				if (items.size() > 0)
				{
                    Iterator iter = items.iterator();
                    while (iter.hasNext()){
                        ItemI o = (ItemI)iter.next();
                        XDATUser temp = new XDATUser(o);
                        if (temp.getUsername().equalsIgnoreCase("guest"))
                        {
                            user = temp;
                        }
                    }
                    if (user == null){
                        ItemI o = items.getFirst();
                        user = new XDATUser(o);
                    }
					TurbineUtils.setUser(data,user);

                    String Destination = data.getTemplateInfo().getScreenTemplate();
                    data.getParameters().add("nextPage", Destination);
                    if (!data.getAction().equalsIgnoreCase(""))
                        data.getParameters().add("nextAction",data.getAction());
                    else
                        data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login"));
                    //System.out.println("nextPage::" + data.getParameters().getString("nextPage") + "::nextAction" + data.getParameters().getString("nextAction") + "\n");

				}
			}else{
			    if (!allowGuestAccess() && user.getLogin().equals("guest")){
                    isAuthorized=false;
                }
            }

            if (data.getParameters().getString("popup") != null)
            {
                if (data.getParameters().getString("popup").equalsIgnoreCase("true"))
                {
                    data.getTemplateInfo().setLayoutTemplate("/Popup.vm");
                }
            }else
            {
                data.getParameters().setString("popup","false");
            }

            logAccess(data);

            if (!isAuthorized){
                String Destination = data.getTemplateInfo().getScreenTemplate();
                data.getParameters().add("nextPage", Destination);
                if (!data.getAction().equalsIgnoreCase(""))
                    data.getParameters().add("nextAction",data.getAction());
                else
                    data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login"));
                //System.out.println("nextPage::" + data.getParameters().getString("nextPage") + "::nextAction" + data.getParameters().getString("nextAction") + "\n");
                doRedirect(data,org.apache.turbine.Turbine.getConfiguration().getString("template.login"));

            }
			return isAuthorized;
		}
	}

	public void logAccess(RunData data)
	{
		AccessLogger.LogScreenAccess(data);
	}

    public void logAccess(RunData data,String message)
    {
        AccessLogger.LogScreenAccess(data,message);
    }

    public boolean allowGuestAccess(){
        return true;
    }

    protected List<Properties> findTabs(String subfolder) throws FileNotFoundException {
        List<Properties> tabs = new ArrayList<Properties>();
        File tabsFolder = XDAT.getScreenTemplatesSubfolder(subfolder);
        if (tabsFolder.exists()) {
            File[] files = tabsFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File folder, String name) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Testing the name: " + name + " in folder: " + folder.getAbsolutePath());
                    }
                    return name.endsWith(".vm");
                }
            });

            for (File file: files) {
                String fileName = file.getName();
                String divName = fileName.substring(0, fileName.length() - 3);
                Properties metadata = new Properties();

                // Set default divName and title properties to start. These can be overridden during mix-in processing.
                metadata.setProperty("fileName", fileName);
                metadata.setProperty("divName", divName);
                metadata.setProperty("title", divName);

                boolean include = true;

                Scanner scanner = new Scanner(file);
                try {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        Matcher matcher = _pattern.matcher(line);
                        if (matcher.matches()) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);
                            if (key.equalsIgnoreCase("ignore") && value.equalsIgnoreCase("true")) {
                                if (_log.isDebugEnabled()) {
                                    _log.debug("Found ignore = true in file: " + file.getName());
                                }
                                include = false;
                                break;
                            }
                            metadata.setProperty(key, value);
                            if (_log.isDebugEnabled()) {
                                _log.debug("Came up with " + key + "[" + value + "] from file: " + file.getName());
                            }
                        }
                    }
                } finally {
                    if (scanner != null) {
                        scanner.close();
                    }
}

                if (include) {
                    tabs.add(metadata);
                }
            }
        }
        return tabs;
    }

    /**
     * Searches for the parameters contained in the <b>parameters</b> array. If the parameter is present with any of the
     * names in the array, it will be pulled and stored in the context using the first parameter name in the array.
     *
     * @param data          The run data.
     * @param context       The Velocity context object.
     * @param parameters    An array of parameter names to be evaluated.
     * @return <b>true</b> if the parameter was found in the run data, <b>false</b> otherwise.
     */
    protected static boolean storeParameterIfPresent(final RunData data, final Context context, final String... parameters) {
        for (String parameter : parameters) {
            if (TurbineUtils.HasPassedParameter(parameter, data)) {
                context.put(parameters[0], TurbineUtils.GetPassedParameter(parameter, data));
                return true;
            }
        }
        return false;
    }

    private static final Log _log = LogFactory.getLog(SecureScreen.class);
}

