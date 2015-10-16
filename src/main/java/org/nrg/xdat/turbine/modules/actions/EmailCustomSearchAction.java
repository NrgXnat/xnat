/*
 * org.nrg.xdat.turbine.modules.actions.EmailCustomSearchAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/9/13 1:06 PM
 */


package org.nrg.xdat.turbine.modules.actions;

import java.net.URLEncoder;
import java.util.Calendar;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

public class EmailCustomSearchAction extends SecureAction{
    public String getScreenTemplate(RunData data)
    {
        return "XDATScreen_email_stored_search.vm";
    }
    
    public boolean executeSearch()
    {
        return false;
    }
    
    String search_xml = null;
    

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocitySecureAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_xml",data))!=null){
            data.getSession().setAttribute("search_xml",((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_xml",data)));
        }
        
        if (data.getSession().getAttribute("search_xml")!=null){
            search_xml = URLEncoder.encode(StringUtils.ReplaceStr(((String) data.getSession().getAttribute("search_xml")),"/",".close."),"UTF-8");
            if(XFT.VERBOSE)System.out.println("URL LENGTH:" + search_xml.length());
        }
        
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("send",data))!=null)
        {
            EmailReportAction email = new EmailReportAction();
            data.getParameters().setString("txtMessage",getTxtMessage(data,context));
            data.getParameters().setString("htmlMessage",getHtmlMessage(data,context));
            email.execute(data,context);
            data.setScreenTemplate("ClosePage.vm");
        }else{
            data.setScreenTemplate(getScreenTemplate(data));
        }
        
        context.put("destination", "EmailCustomSearchAction");
    }
    
    public String getTxtMessage(RunData data, Context context)
    {
        if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("txtmessage",data))==null)
        {
            try {
                UserI user = TurbineUtils.getUser(data);
                
                StringBuffer sb = new StringBuffer();
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <" +TurbineUtils.GetFullServerPath() + "/app/action/DisplaySearchAction");
                sb.append("/search_xml/").append(search_xml);

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
                UserI user = TurbineUtils.getUser(data);

                StringBuffer sb = new StringBuffer();
                sb.append("<html>");
                sb.append("<body>");
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <A HREF=\"").append(TurbineUtils.GetFullServerPath()).append("/app/action/DisplaySearchAction");
                sb.append("/search_xml/").append(search_xml);
                                
                sb.append("\">this link</A> to view the data.<BR><BR>");
                
                sb.append("Message from sender:<BR>");
                sb.append(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("message",data)));
                sb.append("<BR><BR>This email was sent by the <A HREF=\"").append(TurbineUtils.GetFullServerPath()).append("\">").append(TurbineUtils.GetSystemName()).append("</A> data management system on ").append(Calendar.getInstance().getTime()).append(".");
                sb.append("  If you have questions or concerns, please contact the <A HREF=\"mailto:").append(XFT.GetAdminEmail()).append("\">").append(TurbineUtils.GetSystemName()).append(" administrator</A>.");
                
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
