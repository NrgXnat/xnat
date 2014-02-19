/*
 * org.nrg.xdat.turbine.modules.actions.EmailBundleAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
 */


package org.nrg.xdat.turbine.modules.actions;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XdatStoredSearch;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;

/**
 * @author Tim
 *
 */
public class EmailBundleAction extends BundleAction {

	
	public String getScreenTemplate(RunData data)
	{
	    return "XDATScreen_email_stored_search.vm";
	}
    
    public boolean executeSearch()
    {
        return false;
    }

    public void doFinalProcessing(RunData data, Context context) throws Exception
    {
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("send",data))!=null)
        {
            EmailReportAction email = new EmailReportAction();
            data.getParameters().setString("txtMessage",getTxtMessage(data,context));
            data.getParameters().setString("htmlMessage",getHtmlMessage(data,context));
            email.execute(data,context);
            data.setScreenTemplate("ClosePage.vm");
        }
    }
    
    public String getTxtMessage(RunData data, Context context)
    {
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("txtmessage",data))==null)
        {
            try {
                XDATUser user = TurbineUtils.getUser(data);
                DisplaySearch ds = TurbineUtils.getSearch(data);
                XdatStoredSearch xss = ds.getStoredSearch();
                
                StringBuffer sb = new StringBuffer();
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <" +TurbineUtils.GetFullServerPath() + "/app/action/BundleAction");
                sb.append("/bundle/").append(xss.getId());
                
                Hashtable hash =ds.getWebFormValues();
                Enumeration enum1 = hash.keys();
                while(enum1.hasMoreElements())
                {
                    String s= (String)enum1.nextElement();
                    sb.append("/").append(s).append("/").append(hash.get(s));
                }
                
                if (ds.getSortBy() != null && !ds.getSortBy().equalsIgnoreCase(""))
                {
                    sb.append("/sortBy/").append(ds.getSortBy());

                    if (ds.getSortOrder() != null && !ds.getSortOrder().equalsIgnoreCase(""))
                    {
                        sb.append("/sortOrder/").append(ds.getSortOrder());
                    }
                }
                
                sb.append(">this link to view the data.\n\n");
                
                sb.append("Message from sender:\n");
                sb.append(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("message",data)));
                sb.append("\n\nThis email was sent by the <" +TurbineUtils.GetFullServerPath() + ">XNAT data management system on ").append(Calendar.getInstance().getTime()).append(".");
                sb.append("  If you have questions or concerns, please contact the <" + org.nrg.xft.XFT.GetAdminEmail() + ">CNDA administrator.");
                return sb.toString();
            } catch (Exception e) {
                logger.error("",e);
                return "error";
            }
        }else{
            return ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("txtmessage",data));
        }

    }
    
    public String getHtmlMessage(RunData data, Context context)
    {
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("htmlmessage",data))==null)
        {
            try {
                XDATUser user = TurbineUtils.getUser(data);

                DisplaySearch ds = TurbineUtils.getSearch(data);
                XdatStoredSearch xss = ds.getStoredSearch();
                
                StringBuffer sb = new StringBuffer();
                sb.append("<html>");
                sb.append("<body>");
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <A HREF=\"" +TurbineUtils.GetFullServerPath() + "/app/action/BundleAction");
                sb.append("/bundle/").append(xss.getId());
                
                Hashtable hash =ds.getWebFormValues();
                Enumeration enum1 = hash.keys();
                while(enum1.hasMoreElements())
                {
                    String s= (String)enum1.nextElement();
                    sb.append("/").append(s).append("/").append(hash.get(s));
                }

                if (ds.getSortBy() != null && !ds.getSortBy().equalsIgnoreCase(""))
                {
                    sb.append("/sortBy/").append(ds.getSortBy());

                    if (ds.getSortOrder() != null && !ds.getSortOrder().equalsIgnoreCase(""))
                    {
                        sb.append("/sortOrder/").append(ds.getSortOrder());
                    }
                }
                
                sb.append("\">this link</A> to view the data.<BR><BR>");
                
                sb.append("Message from sender:<BR>");
                sb.append(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("message",data)));
                sb.append("<BR><BR>This email was sent by the <A HREF=\"" +TurbineUtils.GetFullServerPath() + "\">XNAT</A> data management system on ").append(Calendar.getInstance().getTime()).append(".");
                sb.append("  If you have questions or concerns, please contact the <A HREF=\"mailto:" + org.nrg.xft.XFT.GetAdminEmail() + "\">").append(TurbineUtils.GetSystemName()).append(" administrator</A>.");
                
                sb.append("</body>");
                sb.append("</html>");
                
                return sb.toString();
            } catch (Exception e) {
                logger.error("",e);
                return "error";
            }
        }else{
            return ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("htmlmessage",data));
        }

    }
}
