//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 3, 2005
 *
 */
package org.nrg.xdat.turbine.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.session.Session;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.turbine.services.session.TurbineSession;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.utils.FileUtils;

/**
 * @author Tim
 *
 */
public class AccessLogger {
    private static final String REQUEST_HISTORY = "request_history";
	static Logger logger = Logger.getLogger(AccessLogger.class);
    
    private static Boolean TRACKING_SESSIONS=null;
    public static boolean TrackingSessions(){
        if(TRACKING_SESSIONS==null){
            TRACKING_SESSIONS=Boolean.FALSE;
            try {
                Collection<?> col = TurbineSession.getActiveSessions();
                if(col.size()>0)
                    TRACKING_SESSIONS=Boolean.TRUE;
            } catch (Throwable e) {
               
            }
        }
        return TRACKING_SESSIONS.booleanValue();
    }
    
    @SuppressWarnings("unchecked")
    public static void LogScreenAccess(RunData data)
	{
        if (!data.getScreen().equalsIgnoreCase(""))
        {
    	    String text= TurbineUtils.getUser(data).getUsername() + " " + data.getRemoteAddr() + " SCREEN: " + data.getScreen();

		    if(TurbineUtils.HasPassedParameter("search_element", data)){
		    	text+=" "+TurbineUtils.GetPassedParameter("search_element", data);
		    }
		    if(TurbineUtils.HasPassedParameter("search_value", data)){
		    	text+=" "+TurbineUtils.GetPassedParameter("search_value", data);
		    }
    		try {
    		    logger.error(text);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
        
        if(TrackingSessions()){
            try {
                if(data.getSession().getAttribute(REQUEST_HISTORY)==null)
                {
                    data.getSession().setAttribute(REQUEST_HISTORY, new ArrayList<String>());
                }
                
                ((List<String>)data.getSession().getAttribute(REQUEST_HISTORY)).add(data.getRequest().getRequestURI());
            } catch (Throwable e) {
                logger.error("",e);
            }
        }
	}
    
    @SuppressWarnings("unchecked")
    public static void LogActionAccess(RunData data)
	{
        if (!data.getAction().equalsIgnoreCase(""))
        {
		    String text= TurbineUtils.getUser(data).getUsername() + " " + data.getRemoteAddr() + " ACTION: " + data.getAction();

		    if(TurbineUtils.HasPassedParameter("xdataction", data)){
		    	text+=" "+TurbineUtils.GetPassedParameter("xdataction", data);
		    }
		    
		    if(TurbineUtils.HasPassedParameter("search_element", data)){
		    	text+=" "+TurbineUtils.GetPassedParameter("search_element", data);
		    }
		    if(TurbineUtils.HasPassedParameter("search_value", data)){
		    	text+=" "+TurbineUtils.GetPassedParameter("search_value", data);
		    }
			try {
			    logger.error(text);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        
        if(TrackingSessions()){
            try {
                if(data.getSession().getAttribute(REQUEST_HISTORY)==null)
                {
                    data.getSession().setAttribute(REQUEST_HISTORY, new ArrayList<String>());
                }
                
                ((List<String>)data.getSession().getAttribute(REQUEST_HISTORY)).add(data.getRequest().getRequestURI());
            } catch (Throwable e) {
                logger.error("",e);
            }
        }
	}
    @SuppressWarnings("unchecked")
    public static void LogScreenAccess(RunData data,String message)
	{
        if (!data.getScreen().equalsIgnoreCase(""))
        {
    	    String text= TurbineUtils.getUser(data).getUsername() + " " + data.getRemoteAddr() + " SCREEN: " + data.getScreen() + " " + message;
    	    
    		try {
    		    logger.error(text);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
        
        if(TrackingSessions()){
            try {
                if(data.getSession().getAttribute(REQUEST_HISTORY)==null)
                {
                    data.getSession().setAttribute(REQUEST_HISTORY, new ArrayList<String>());
                }
                
                ((List<String>)data.getSession().getAttribute(REQUEST_HISTORY)).add(data.getRequest().getRequestURI());
            } catch (Throwable e) {
                logger.error("",e);
            }
        }
	}
    
    @SuppressWarnings("unchecked")
    public static void LogActionAccess(RunData data,String message)
	{
        if (!data.getAction().equalsIgnoreCase(""))
        {
		    XDATUser user=TurbineUtils.getUser(data);
        	String text= ((user!=null)?user.getUsername():"NULL") + " " + data.getRemoteAddr() + " ACTION: " + data.getAction() + " " + message;
		    
			try {
			    logger.error(text);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        
        if(TrackingSessions()){
            try {
                if(data.getSession().getAttribute(REQUEST_HISTORY)==null)
                {
                    data.getSession().setAttribute(REQUEST_HISTORY, new ArrayList<String>());
                }
                
                ((List<String>)data.getSession().getAttribute(REQUEST_HISTORY)).add(data.getRequest().getRequestURI());
            } catch (Throwable e) {
                logger.error("",e);
            }
        }
	}
    
    public static void LogServiceAccess(String user,String address, String service,String message)
	{
        String text= user + " " + address + " " + service + " " + message;
		    
		try {
		    logger.error(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        if(TrackingSessions()){
            try {
                MessageContext mc = AxisEngine.getCurrentMessageContext();
                Session session =mc.getSession();
                
                if(session.get(REQUEST_HISTORY)==null){
                    session.set(REQUEST_HISTORY, new ArrayList<String>());
                }
                
                ((List<String>)session.get(REQUEST_HISTORY)).add(service + " " + message);
            } catch (Throwable e) {
                logger.error("",e);
            }
        }
	}
    
    public static String getAccessLogDirectory(){
        String dir = "";
        Enumeration<?> e2 = logger.getAllAppenders();
        while (e2.hasMoreElements())
        {
            Appender a = (Appender)e2.nextElement();
            if (a instanceof FileAppender)
            {
                FileAppender fa = (FileAppender)a;
                String s = fa.getFile();
                if (s != null)
                {
                    File f = new File(s);
                    dir = f.getParentFile().getAbsolutePath();
                    break;
                }
            }
        }
        
        dir=FileUtils.AppendSlash(dir);
        //System.out.println(dir);
        return dir;
    }
}
