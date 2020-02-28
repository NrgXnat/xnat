/*
 * core: org.nrg.xdat.turbine.modules.actions.EmailReportAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import java.util.Calendar;

/**
 * @author Tim
 *
 */
public class EmailReportAction extends EmailAction {
    static Logger logger = Logger.getLogger(EmailReportAction.class);
    
    public void sendMessage(RunData data, Context context) {
        if (!StringUtils.isBlank(toAddress) | !StringUtils.isBlank(ccAddress) || !StringUtils.isBlank(bccAddress)) {	
			if (XDAT.getNotificationsPreferences().getCopyAdminOnPageEmails()) {
				if (StringUtils.isBlank(bccAddress)) {
					bccAddress = XDAT.getSiteConfigPreferences().getAdminEmail();
				} else {
					bccAddress += ", " + XDAT.getSiteConfigPreferences().getAdminEmail();
				}
			}

			// Split each string on commas and whitespace.
			String[] tos = StringUtils.split(toAddress == null ? "" : toAddress, ", ");
			String[] ccs = StringUtils.split(ccAddress == null ? "" : ccAddress, ", ");
			String[] bccs = StringUtils.split(bccAddress == null ? "" : bccAddress, ", ");
			
			String subject = getSubject(data,context).replace("&apos;", "'"); // standard email HTML doesn't use this tag;
			String message = getHtmlMessage(data,context).replace("&apos;", "'");
			String text = getTxtMessage(data,context).replace("&apos;", "'");

			try {
				XDAT.getMailService().sendHtmlMessage(XDAT.getSiteConfigPreferences().getAdminEmail(), tos, ccs, bccs, subject, message, text);
			    data.setMessage("Message sent.");
			    context.put("messageType", "success");
			} catch (Exception e) {
			    logger.error("Unable to send mail");
			    data.setMessage("Unable to send mail.");
			}
            
            try {
				SchemaElement se=SchemaElement.GetElement(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)));
				data.setScreenTemplate(DisplayItemAction.GetReportScreen(se));
			} catch (XFTInitException e) {
				logger.error(e);
			} catch (ElementNotFoundException e) {
				logger.error(e);
			}
		    
		}
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
                sb.append(" Please follow <" +TurbineUtils.GetFullServerPath() + "/app/action/DisplayItemAction");
                sb.append("/search_element/").append(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)));
                sb.append("/search_field/").append(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)));
                sb.append("/search_value/").append(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data)));
                sb.append(">this link to view the data.\n\n");
                
                sb.append("Message from sender:\n");
                sb.append(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("message",data)));
                sb.append("\n\nThis email was sent by the <" +TurbineUtils.GetFullServerPath() + ">XNAT data management system on ").append(Calendar.getInstance().getTime()).append(".");
                sb.append("  If you have questions or concerns, please contact the <" + XDAT.getNotificationsPreferences().getHelpContactInfo() + ">CNDA administrator.");
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
                sb.append(" Please follow <A HREF=\"").append(TurbineUtils.GetFullServerPath()).append("/app/action/DisplayItemAction");
                sb.append("/search_element/").append((String)TurbineUtils.GetPassedParameter("search_element", data));
                sb.append("/search_field/").append((String)TurbineUtils.GetPassedParameter("search_field",data));
                sb.append("/search_value/").append((String)TurbineUtils.GetPassedParameter("search_value", data));
                sb.append("\">this link</A> to view the data.<BR><BR>");
                
                sb.append("Message from sender:<BR>");
                sb.append(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("message",data)));
                sb.append("<BR><BR>This email was sent by the <A HREF=\"").append(TurbineUtils.GetFullServerPath()).append("\">").append(TurbineUtils.GetSystemName()).append("</A> data management system on ").append(Calendar.getInstance().getTime()).append(".");
                sb.append("  If you have questions or concerns, please contact <A HREF=\"mailto:").append(XDAT.getNotificationsPreferences().getHelpContactInfo()).append("\">").append(TurbineUtils.GetSystemName()).append(" help</A>.");
                
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
