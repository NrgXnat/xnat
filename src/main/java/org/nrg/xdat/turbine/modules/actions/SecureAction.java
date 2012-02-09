//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;
import java.io.IOException;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;

/**
 * @author Tim
 *
 */
public abstract class SecureAction extends VelocitySecureAction
{
    static Logger logger = Logger.getLogger(SecureAction.class);


    protected void preserveVariables(RunData data, Context context){
        if (data.getParameters().containsKey("project")){
        	if(XFT.VERBOSE)System.out.println(this.getClass().getName() + ": maintaining project '" + data.getParameters().getString("project") +"'");
            context.put("project", data.getParameters().getString("project"));
        }
    }

    protected void error(Throwable e,RunData data){
        logger.error("",e);
        if(e instanceof InvalidPermissionException){
        	try {
        		AdminUtils.sendAdminEmail(TurbineUtils.getUser(data), "Possible Authorization Bypass Attempt", "User attempted to access or modify protected content at action: " + data.getAction() + "; " + e.getMessage());
				data.getResponse().sendError(403);
			} catch (IOException e1) {
			}
        }
        data.setScreenTemplate("Error.vm");
        data.getParameters().setString("exception", e.toString());
    }

    final static String encoding="ISO-8859-1";

    public void redirectToReportScreen(String report,ItemI item,RunData data)
    {
        data = TurbineUtils.SetSearchProperties(data,item);
        try {
			String path = TurbineUtils.GetRelativeServerPath(data)+ "/app/template/" + URLEncoder.encode(report,encoding) + "/search_field/" + URLEncoder.encode(data.getParameters().get("search_field"),encoding) +  "/search_value/" +  URLEncoder.encode(data.getParameters().get("search_value"),encoding)  + "/search_element/" +  URLEncoder.encode(data.getParameters().get("search_element"),encoding);
        if (data.getParameters().getString("popup")!=null){
			    path += "/popup/" + URLEncoder.encode(data.getParameters().getString("popup"),encoding);
        }
        if (data.getParameters().getString("project")!=null){
			    path += "/project/" + URLEncoder.encode(data.getParameters().getString("project"),encoding);
        }
        if (data.getParameters().getString("params")!=null){
			    path += URLEncoder.encode(data.getParameters().getString("params"),encoding);
        }
        data.setRedirectURI(path);
		} catch (UnsupportedEncodingException e) {
			logger.error("",e);
		}
    }

    public void redirectToScreen(String report,RunData data)
    {
        try {
			String path = TurbineUtils.GetRelativeServerPath(data)+ "/app/template/" + URLEncoder.encode(report,encoding);
        if (data.getParameters().getString("popup")!=null){
			    path += "/popup/" + URLEncoder.encode(data.getParameters().getString("popup"),encoding);
        }
        if (data.getParameters().getString("project")!=null){
			    path += "/project/" + URLEncoder.encode(data.getParameters().getString("project"),encoding);
        }
        data.setRedirectURI(path);
		} catch (UnsupportedEncodingException e) {
			logger.error("",e);
		}
    }

    public void redirectToReportScreen(ItemI item,RunData data)
    {
        try {
            SchemaElement se = SchemaElement.GetElement(item.getXSIType());
            this.redirectToReportScreen(DisplayItemAction.GetReportScreen(se), item, data);
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
    }

    protected boolean isAuthorized( RunData data )  throws Exception
    {
        if (XFT.GetRequireLogin() || TurbineUtils.HasPassedParameter("par", data))
        {
            TurbineVelocity.getContext(data).put("logout","true");
            data.getParameters().setString("logout","true");
            boolean isAuthorized = false;

            XDATUser user = TurbineUtils.getUser(data);
            if (user == null)
            {
                String Destination = data.getTemplateInfo().getScreenTemplate();
                data.getParameters().add("nextPage", Destination);
                if (!data.getAction().equalsIgnoreCase(""))
                    data.getParameters().add("nextAction",data.getAction());
                else 
                    data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login"));	

            }else
            {
                AccessLogger.LogActionAccess(data);
                isAuthorized = true;
            }	

            return isAuthorized;
        }else{
            boolean isAuthorized = true;
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

            data.getParameters().add("new_session", "TRUE");
            AccessLogger.LogActionAccess(data);

            if (!isAuthorized){
                String Destination = data.getTemplateInfo().getScreenTemplate();
                data.getParameters().add("nextPage", Destination);
                if (!data.getAction().equalsIgnoreCase(""))
                    data.getParameters().add("nextAction",data.getAction());
                else 
                    data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login")); 
                //System.out.println("nextPage::" + data.getParameters().getString("nextPage") + "::nextAction" + data.getParameters().getString("nextAction") + "\n"); 
            }
            return isAuthorized;
        }
    }

    public boolean allowGuestAccess(){
        return true;
    }
	
	public void notifyAdmin(XDATUser authenticatedUser, RunData data, int code, String subject, String message) throws IOException{
		AdminUtils.sendAdminEmail(authenticatedUser, subject,message);
		data.getResponse().sendError(code);
	}
}

