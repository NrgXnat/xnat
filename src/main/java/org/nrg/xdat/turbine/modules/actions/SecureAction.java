//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT  Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.BrowserType;

import org.apache.commons.lang.StringUtils;
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
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
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
        	if(XFT.VERBOSE)System.out.println(this.getClass().getName() + ": maintaining project '" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data)) +"'");
            context.put("project", TurbineUtils.escapeParam(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data))));
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
			String path = TurbineUtils.GetRelativeServerPath(data)+ "/app/template/" + URLEncoder.encode(report,encoding) + "/search_field/" + URLEncoder.encode(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)),encoding) +  "/search_value/" +  URLEncoder.encode(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data)),encoding)  + "/search_element/" +  URLEncoder.encode(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)),encoding);
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data))!=null){
			    path += "/popup/" + URLEncoder.encode(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data)),encoding);
        }
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data))!=null){
			    path += "/project/" + URLEncoder.encode(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data)),encoding);
        }
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("params",data))!=null){
			    path += URLEncoder.encode(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("params",data)),encoding);
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
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data))!=null){
			    path += "/popup/" + URLEncoder.encode(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data)),encoding);
        }
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data))!=null){
			    path += "/project/" + URLEncoder.encode(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data)),encoding);
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

    public static String csrfTokenErrorMessage(HttpServletRequest request){
		StringBuffer errorMessage = new StringBuffer();
		errorMessage.append(request.getMethod()).append(" on URL: ").append(request.getRequestURL()).append(" from ").append(request.getRemoteAddr()).append(" (").append(request.getRemotePort()).append(") user: ").append(request.getRemoteHost()).append("\n");
		errorMessage.append("Headers:\n");
		Enumeration<String> headerNames =  request.getHeaderNames();
		while(headerNames.hasMoreElements()){
			String hName = headerNames.nextElement();
			errorMessage.append(hName).append(": ").append(request.getHeader(hName)).append("\n");
		}
		errorMessage.append("\n Cookies:\n");
		
		Cookie [] cookies = request.getCookies();
		for(int i = 0; i<cookies.length;i++){
			errorMessage.append(cookies[i].getName()).append(" ").append(cookies[i].getValue()).append(" ").append(cookies[i].getMaxAge()).append(" ").append(cookies[i].getDomain()).append("\n");
		}
		return errorMessage.toString();
    }
    
  //just a wrapper for isCsrfTokenOk(request, token)
    public static boolean isCsrfTokenOk(RunData runData) throws Exception {
    	//occasionally, (really, only on "actions" that inherit off securescreen instead of secure action like report issue) 
    	//the HTTPServletRequest parameters magically get cleared. that's why this method is here.
    	String clientToken = TurbineUtils.escapeParam(runData.getParameters().get("XNAT_CSRF"));
    	return isCsrfTokenOk(runData.getRequest(), clientToken,true);
    }
    
    //just a wrapper for isCsrfTokenOk(request, token)
    public static boolean isCsrfTokenOk(HttpServletRequest request,boolean strict) throws Exception {
    	return isCsrfTokenOk(request, request.getParameter("XNAT_CSRF"),strict);
    }
    
    //this is a little silly in that it either returns true or throws an exception...
    //if you change that behavior, look at every place this is used to be sure it actually
    //checks for true/false. I know for a fact it doesn't in XnatSecureGuard.	
    public static boolean isCsrfTokenOk(HttpServletRequest request, String clientToken, boolean strict) throws Exception {
    	//let anyone using something other than a browser ignore the token.
    	String userAgent = request.getHeader("User-Agent");
    	if(!strict && userAgent==null){
    		return true;
    	}else if(!strict){
    		 Browser b=Browser.parseUserAgentString(userAgent);
    		 if((!(b.getBrowserType().equals(BrowserType.MOBILE_BROWSER) || b.getBrowserType().equals(BrowserType.WEB_BROWSER))) || userAgent.toUpperCase().contains("JAVA")){
    			 return true;
    		 }
    	}
    	
    	HttpSession session = request.getSession();
    	String serverToken = (String)session.getAttribute("XNAT_CSRF");

    	if(serverToken == null){
    		String errorMessage = csrfTokenErrorMessage(request);
    		AdminUtils.sendAdminEmail("Possible CSRF Attempt", "XNAT_CSRF token was not properly set in the session.\n" + errorMessage);
    		throw new Exception("Invalid submit value (" + errorMessage + ")");
    	}
    	
    	String method = request.getMethod();
    	if("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)){
    		//pull the token out of the parameter
    		
    		if(serverToken.equalsIgnoreCase(clientToken)){
    			return true;
    		} else {
    			String errorMessage = csrfTokenErrorMessage(request);
    			AdminUtils.sendAdminEmail("Possible CSRF Attempt", errorMessage);
	    		throw new Exception("Invalid submit value (" + errorMessage + ")");
    		}
    			
    	} else {
    		return true;
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
            if(isAuthorized){
            	return isCsrfTokenOk(data);
            } else {
            	return isAuthorized;
            }
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
                    session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());

                    String Destination = data.getTemplateInfo().getScreenTemplate();
                    data.getParameters().add("nextPage", Destination);
                    if (!data.getAction().equalsIgnoreCase(""))
                        data.getParameters().add("nextAction",data.getAction());
                    else 
                        data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login")); 
                    //System.out.println("nextPage::" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextAction",data)) + "\n"); 
                    
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
                //System.out.println("nextPage::" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextAction",data)) + "\n"); 
            }
            if(isAuthorized){
            	return isCsrfTokenOk(data);
            } else {
            	return isAuthorized;
            }
        }
    }

    public boolean allowGuestAccess(){
        return true;
    }

	
	public static EventUtils.TYPE getEventType(RunData data){
		final String id=(String)TurbineUtils.GetPassedParameter(EventUtils.EVENT_TYPE, data);
		if(id!=null){
			return EventUtils.getType(id, EventUtils.TYPE.WEB_FORM);
		}else{
			return EventUtils.TYPE.WEB_FORM;
		}
	}

    public static String getReason(RunData data){
    	return (String)TurbineUtils.GetPassedParameter(EventUtils.EVENT_REASON, data);
    }
	
	public static String getAction(RunData data){
		return (String)TurbineUtils.GetPassedParameter(EventUtils.EVENT_ACTION,data);
	}
	
	public static String getComment(RunData data){
		return (String)TurbineUtils.GetPassedParameter(EventUtils.EVENT_COMMENT,data);
	}
	
	public static EventDetails newEventInstance(RunData data,EventUtils.CATEGORY cat){
		return EventUtils.newEventInstance(cat, getEventType(data), getAction(data), getReason(data), getComment(data));
	}
	
	public static EventDetails newEventInstance(RunData data,EventUtils.CATEGORY cat,String action){
		return EventUtils.newEventInstance(cat, getEventType(data), (getAction(data)!=null)?getAction(data):action, getReason(data), getComment(data));
	}

    public void handleException(RunData data,XFTItem first,Throwable error, String itemIdentifier){
        data.getSession().setAttribute(itemIdentifier,first);
        data.addMessage(error.getMessage());
        if (data.getParameters().getString("edit_screen") !=null)
        {
            data.setScreenTemplate(data.getParameters().getString("edit_screen"));
        }else{
            data.setScreenTemplate("Index.vm");
        }
        return;
    }
	
	public void notifyAdmin(XDATUser authenticatedUser, RunData data, int code, String subject, String message) throws IOException{
		AdminUtils.sendAdminEmail(authenticatedUser, subject,message);
		data.getResponse().sendError(code);
	}
}

