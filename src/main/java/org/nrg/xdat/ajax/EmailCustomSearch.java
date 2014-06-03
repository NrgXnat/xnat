//Copyright 2007 Washington University School of Medicine All Rights Reserved
/*
 * Created on Sep 19, 2007
 *
 */
package org.nrg.xdat.ajax;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;

public class EmailCustomSearch {
    static org.apache.log4j.Logger logger = Logger.getLogger(EmailCustomSearch.class);

    public void send(HttpServletRequest req, HttpServletResponse response,ServletConfig sc) throws IOException{
        String xmlString = req.getParameter("search_xml");
        
        HttpSession session = req.getSession();
        UserI user = ((UserI)session.getAttribute("user"));        

        String _return ="<DIV class=\"error\">Unknown Exception</DIV>";
        if (user!=null){
            String toAddress = req.getParameter("toAddress");
            String ccAddress = req.getParameter("ccAddress");
            String bccAddress = req.getParameter("bccAddress");
            String subject = req.getParameter("subject");
            String message = req.getParameter("message");
            
			if (AdminUtils.GetPageEmail()) {
				if (StringUtils.isBlank(bccAddress)) {
					bccAddress = AdminUtils.getAdminEmailId();
				} else {
					bccAddress += ", " + AdminUtils.getAdminEmailId();
                        }
                    }
                    
			// Split each string on commas and whitespace.
			String[] tos = StringUtils.split(toAddress, ", ");
			String[] ccs = StringUtils.split(ccAddress, ", ");
			String[] bccs = StringUtils.split(bccAddress, ", ");

			if (toAddress != null || ccAddress != null || bccAddress != null) {
				try {
                    Object search_id=PoolDBUtils.LogCustomSearch(user.getUsername(), xmlString, user.getDBName());
					String formattedMessage = formatHtmlMessage(req, user, message, search_id);
					XDAT.getMailService().sendHtmlMessage(user.getEmail(), tos, ccs, bccs, subject, formattedMessage);
                        _return=("<DIV class=\"warning\">Message sent.</DIV>");
                    } catch (Exception e) {
                        logger.error("",e);
                        _return=("<DIV class=\"error\">Unable to send mail.</DIV>");
                    }
            }
        }else{
            _return= "<DIV class=\"error\">Missing User Account</DIV>";
        }
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(_return);
    }

	public String getTxtMessage(HttpServletRequest req, UserI user, String msg, Object search_id) {
		if (req.getParameter("txtmessage") == null) {
            try {                
                StringBuffer sb = new StringBuffer();
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <" +TurbineUtils.GetFullServerPath() + "/app/action/DisplaySearchAction");
                sb.append("/search_id/").append(search_id);

                sb.append(">this link to view the data.\n\n");
                
                sb.append("Message from sender:\n");
                sb.append(msg);
                sb.append("\n\nThis email was sent by the <" +TurbineUtils.GetFullServerPath() + ">XNAT data management system on ").append(Calendar.getInstance().getTime()).append(".");
                sb.append("  If you have questions or concerns, please contact the <" + org.nrg.xft.XFT.GetAdminEmail() + ">CNDA administrator.");
                return sb.toString();
            } catch (Exception e) {
                logger.error("",e);
                return "error";
            }
        }else{
            return req.getParameter("txtmessage");
        }

    }
    
	private String formatHtmlMessage(HttpServletRequest req, UserI user, String msg, Object search_id) {
		if (req.getParameter("htmlmessage") == null) {
            try {
                StringBuffer sb = new StringBuffer();
                sb.append("<html>");
                sb.append("<body>");
                sb.append(user.getFirstname()).append(" ").append(user.getLastname());
                sb.append(" thought you might be interested in a data set contained in the ").append(TurbineUtils.GetSystemName()).append(".");
                sb.append(" Please follow <A HREF=\"" +TurbineUtils.GetFullServerPath() + "/app/action/DisplaySearchAction");
                sb.append("/search_id/").append(search_id);
                                
                sb.append("\">this link</A> to view the data.<BR><BR>");
                
                sb.append("Message from sender:<BR>");
                sb.append(msg);
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
            return req.getParameter("htmlmessage");
        }

    }
}
