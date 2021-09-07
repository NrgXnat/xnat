/*
 * core: org.nrg.xdat.ajax.EmailCustomSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.ajax;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class EmailCustomSearch {
    private static final Logger logger = LoggerFactory.getLogger(EmailCustomSearch.class);

    public void send(HttpServletRequest req, HttpServletResponse response, ServletConfig sc) throws IOException {
        String xmlString = req.getParameter("search_xml");

        UserI user = XDAT.getUserDetails();

        String _return = "<DIV class=\"error\">Unknown Exception</DIV>";
        if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
            if (user != null) {
                String toAddress = req.getParameter("toAddress");
                String ccAddress = req.getParameter("ccAddress");
                String bccAddress = req.getParameter("bccAddress");
                String subject = req.getParameter("subject");
                String message = req.getParameter("message");

                if (XDAT.getNotificationsPreferences().getCopyAdminOnPageEmails()) {
                    if (StringUtils.isBlank(bccAddress)) {
                        bccAddress = XDAT.getSiteConfigPreferences().getAdminEmail();
                    } else {
                        bccAddress += ", " + XDAT.getSiteConfigPreferences().getAdminEmail();
                    }
                }

                // Split each string on commas and whitespace.
                String[] tos = StringUtils.split(toAddress, ", ");
                String[] ccs = StringUtils.split(ccAddress, ", ");
                String[] bccs = StringUtils.split(bccAddress, ", ");

                if (toAddress != null || ccAddress != null || bccAddress != null) {
                    try {
                        Object search_id = PoolDBUtils.LogCustomSearch(user.getUsername(), xmlString, user.getDBName());

                        if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
                            String formattedMessage = formatHtmlMessage(req, user, message, search_id);
                            XDAT.getMailService().sendHtmlMessage(user.getEmail(), tos, ccs, bccs, subject, formattedMessage);
                            _return = ("<DIV class=\"warning\">Message sent.</DIV>");
                        } else {
                            _return = ("<DIV class=\"error\">Unable to send mail.</DIV>");
                        }
                    } catch (Exception e) {
                        logger.error("", e);
                        _return = ("<DIV class=\"error\">Unable to send mail.</DIV>");
                    }
                }
            } else {
                _return = "<DIV class=\"error\">Missing User Account</DIV>";
            }
        } else {
            _return = "<DIV class=\"error\">SMTP disabled.</DIV>";
        }

        response.setContentType("text/html");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(_return);
    }

	public String getTxtMessage(HttpServletRequest req, UserI user, String msg, Object search_id) {
		if (req.getParameter("txtmessage") == null) {
            try {

                return user.getFirstname() + " " + user.getLastname() +
                       " thought you might be interested in a data set contained in the " + TurbineUtils.GetSystemName() + "." +
                       " Please follow <" + TurbineUtils.GetFullServerPath() + "/app/action/DisplaySearchAction" +
                       "/search_id/" + search_id +
                       ">this link to view the data.\n\n" +
                       "Message from sender:\n" +
                       msg +
                       "\n\nThis email was sent by the <" + TurbineUtils.GetFullServerPath() + ">XNAT data management system on " + Calendar.getInstance().getTime() + "." +
                       "  If you have questions or concerns, please contact the <" + XDAT.getNotificationsPreferences().getHelpContactInfo() + ">CNDA administrator.";
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
                String body = XDAT.getNotificationsPreferences().getEmailMessageDataAlertCustom();
                body = body.replaceAll("USER_FIRSTNAME", user.getFirstname());
                body = body.replaceAll("USER_LASTNAME", user.getLastname());
                body = body.replaceAll("SITE_NAME", TurbineUtils.GetSystemName());
                String requestLink = TurbineUtils.GetFullServerPath() + "/app/action/DisplaySearchAction" + "/search_id/" + search_id;
                String requestLinkFull = "<a href=\"" + requestLink + "\">this link</a>";

                body = body.replaceAll("REQUEST_LINK", requestLinkFull);
                body = body.replaceAll("SENDER_MESSAGE", msg);
                String siteLink = "<a href=\"" + TurbineUtils.GetFullServerPath() + "\">" + TurbineUtils.GetSystemName() + "</a>";
                body = body.replaceAll("SITE_URL", siteLink);
                Date date = Calendar.getInstance().getTime();
                String dateString = TurbineUtils.GetInstance().formatDateTime(date);
                body = body.replaceAll("TIME_SENT", dateString);
                String helpContact = "<a href=\"mailto:" + XDAT.getNotificationsPreferences().getHelpContactInfo() + "\">" + TurbineUtils.GetSystemName()+ " help</a>";
                body = body.replaceAll("HELP_CONTACT", helpContact);

                return body;
            } catch (Exception e) {
                logger.error("",e);
                return "error";
            }
        }else{
            return req.getParameter("htmlmessage");
        }

    }
}
