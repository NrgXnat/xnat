//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Dec 2, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;

/**
 * @author Tim
 *
 */
public class EmailReportAction extends EmailAction {
    static Logger logger = Logger.getLogger(EmailReportAction.class);
    
    public void sendMessage(RunData data, Context context) {
        if (!StringUtils.isBlank(toAddress) | !StringUtils.isBlank(ccAddress) || !StringUtils.isBlank(bccAddress)) {	
			if (AdminUtils.GetPageEmail()) {
				if (StringUtils.isBlank(bccAddress)) {
					bccAddress = AdminUtils.getAdminEmailId();
				} else {
					bccAddress += ", " + AdminUtils.getAdminEmailId();
				}
			}

			// Split each string on commas and whitespace.
			String[] tos = StringUtils.split(toAddress == null ? "" : toAddress, ", ");
			String[] ccs = StringUtils.split(ccAddress == null ? "" : ccAddress, ", ");
			String[] bccs = StringUtils.split(bccAddress == null ? "" : bccAddress, ", ");
			
			String subject = getSubject(data,context);
			String message = getHtmlMessage(data,context);
			String text = getTxtMessage(data,context);

			try {
				XDAT.getMailService().sendHtmlMessage(AdminUtils.getAdminEmailId(), tos, ccs, bccs, subject, message, text);
			    data.setMessage("Message sent.");
			} catch (Exception e) {
			    logger.error("Unable to send mail");
			    data.setMessage("Unable to send mail.");
			}
            
            try {
				SchemaElement se=SchemaElement.GetElement(data.getParameters().getString("search_element"));
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
        if (data.getParameters().get("txtmessage")==null)
        {
            try {
                XDATUser user = TurbineUtils.getUser(data);
                
                StringBuffer sb = new StringBuffer();
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <" +TurbineUtils.GetFullServerPath() + "/app/action/DisplayItemAction");
                sb.append("/search_element/").append(data.getParameters().getString("search_element"));
                sb.append("/search_field/").append(data.getParameters().getString("search_field"));
                sb.append("/search_value/").append(data.getParameters().getString("search_value"));
                sb.append(">this link to view the data.\n\n");
                
                sb.append("Message from sender:\n");
                sb.append(data.getParameters().getString("message"));
                sb.append("\n\nThis email was sent by the <" +TurbineUtils.GetFullServerPath() + ">XNAT data management system on ").append(Calendar.getInstance().getTime()).append(".");
                sb.append("  If you have questions or concerns, please contact the <" + org.nrg.xft.XFT.GetAdminEmail() + ">CNDA administrator.");
                return sb.toString();
            } catch (Exception e) {
                logger.error("",e);
                return "error";
            }
        }else{
            return data.getParameters().getString("txtmessage");
        }

    }
    
    public String getHtmlMessage(RunData data, Context context)
    {
        if (data.getParameters().get("htmlmessage")==null)
        {
            try {
                XDATUser user = TurbineUtils.getUser(data);
                
                StringBuffer sb = new StringBuffer();
                sb.append("<html>");
                sb.append("<body>");
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <A HREF=\"" +TurbineUtils.GetFullServerPath() + "/app/action/DisplayItemAction");
                sb.append("/search_element/").append(data.getParameters().getString("search_element"));
                sb.append("/search_field/").append(data.getParameters().getString("search_field"));
                sb.append("/search_value/").append(data.getParameters().getString("search_value"));
                sb.append("\">this link</A> to view the data.<BR><BR>");
                
                sb.append("Message from sender:<BR>");
                sb.append(data.getParameters().getString("message"));
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
            return data.getParameters().getString("htmlmessage");
        }

    }
}
