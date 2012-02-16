//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Dec 2, 2005
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.email.EmailUtils;
import org.nrg.xft.email.EmailerI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public class EmailReportAction extends EmailAction {
    static Logger logger = Logger.getLogger(EmailReportAction.class);
    
    public void sendMessage(RunData data, Context context) {
        if (!toAddress.equals("") || !ccAddress.equals("") || !bccAddress.equals("")) {	
        	try {
				EmailerI sm=EmailUtils.getEmailer();
				sm.setFrom(AdminUtils.getAdminEmailId());
				
				if (!toAddress.equals("")){ 
				    ArrayList al = StringUtils.CommaDelimitedStringToArrayList(toAddress.substring(0,toAddress.lastIndexOf(",")));
				    Iterator iter = al.iterator();
				    while (iter.hasNext())
				    {
				        sm.addTo((String)iter.next());
				    }
				}
				if (!ccAddress.equals("")){
				    ArrayList al = StringUtils.CommaDelimitedStringToArrayList(ccAddress.substring(0,ccAddress.lastIndexOf(",")));
				    Iterator iter = al.iterator();
				    while (iter.hasNext())
				    {
				        sm.addCc((String)iter.next());
				    }
				}
				if (!bccAddress.equals("")){
				    ArrayList al = StringUtils.CommaDelimitedStringToArrayList(bccAddress.substring(0,bccAddress.lastIndexOf(",")));
				    Iterator iter = al.iterator();
				    while (iter.hasNext())
				    {
				        sm.addBcc((String)iter.next());
				    }
				}
				
				if(AdminUtils.GetPageEmail()){
				    sm.addBcc(AdminUtils.getAdminEmailId());
				}
				
				sm.setSubject(getSubject(data,context));
				sm.setTextMsg(getTxtMessage(data,context));
				sm.setHtmlMsg(getHtmlMessage(data,context));
				
			    sm.send();
			    data.setMessage("Message sent.");
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
                XDATUser user = TurbineUtils.getUser(data);
                
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
