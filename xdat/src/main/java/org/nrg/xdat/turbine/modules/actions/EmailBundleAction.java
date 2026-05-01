/*
 * core: org.nrg.xdat.turbine.modules.actions.EmailBundleAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatStoredSearch;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 *
 */
@Slf4j
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
        if (TurbineUtils.GetPassedParameter("send", data) != null)
        {
            EmailReportAction email = new EmailReportAction();
            data.getParameters().setString("txtMessage",getTxtMessage(data,context));
            data.getParameters().setString("htmlMessage",getHtmlMessage(data,context));
            email.execute(data,context);
            data.setScreenTemplate("ClosePage.vm");
        }
    }
    
    public String getTxtMessage(RunData data, @SuppressWarnings("unused") Context context)
    {
        if (TurbineUtils.GetPassedParameter("txtmessage", data) == null)
        {
            try {
                UserI user = XDAT.getUserDetails();
                assert user != null;
                DisplaySearch ds = TurbineUtils.getSearch(data);
                XdatStoredSearch xss = ds.getStoredSearch();
                
                final StringBuilder sb = new StringBuilder();
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <").append(TurbineUtils.GetFullServerPath()).append("/app/action/BundleAction");
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
                sb.append(((String)TurbineUtils.GetPassedParameter("message",data)));
                sb.append("\n\nThis email was sent by the <").append(TurbineUtils.GetFullServerPath()).append(">XNAT data management system on ").append(Calendar.getInstance().getTime()).append(".");
                sb.append("  If you have questions or concerns, please contact the <").append(XDAT.getNotificationsPreferences().getHelpContactInfo()).append(">CNDA administrator.");
                return sb.toString();
            } catch (Exception e) {
                log.error("An unexpected exception occurred.", e);
                return "error";
            }
        }else{
            return ((String)TurbineUtils.GetPassedParameter("txtmessage",data));
        }

    }
    
    public String getHtmlMessage(RunData data, @SuppressWarnings("unused") Context context)
    {
        if (TurbineUtils.GetPassedParameter("htmlmessage", data) == null)
        {
            try {
                UserI user = XDAT.getUserDetails();
                assert user != null;

                DisplaySearch ds = TurbineUtils.getSearch(data);
                XdatStoredSearch xss = ds.getStoredSearch();
                
                final StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append("<body>");
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <A HREF=\"").append(TurbineUtils.GetFullServerPath()).append("/app/action/BundleAction");
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
                sb.append(((String)TurbineUtils.GetPassedParameter("message",data)));
                sb.append("<BR><BR>This email was sent by the <A HREF=\"").append(TurbineUtils.GetFullServerPath()).append("\">XNAT</A> data management system on ").append(Calendar.getInstance().getTime()).append(".");
                sb.append("  If you have questions or concerns, please contact the <A HREF=\"mailto:").append(XDAT.getNotificationsPreferences().getHelpContactInfo()).append("\">").append(TurbineUtils.GetSystemName()).append(" administrator</A>.");
                
                sb.append("</body>");
                sb.append("</html>");
                
                return sb.toString();
            } catch (Exception e) {
                log.error("An unexpected exception occurred.", e);
                return "error";
            }
        }else{
            return ((String)TurbineUtils.GetPassedParameter("htmlmessage",data));
        }

    }
}
