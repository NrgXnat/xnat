/*
 * web: org.nrg.xnat.turbine.modules.actions.ReportIssue
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.context.Context;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.api.NotificationType;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.FileUtils;

public class ReportIssue extends SecureAction {
    private static final Logger logger = Logger.getLogger(ReportIssue.class);

    private static final String HTTP_USER_AGENT = "User-Agent";
    private static final String HTTP_HOST = "Host";
    private static final String JAVA_VENDOR = "java.vendor";
    private static final String JAVA_VERSION = "java.version";
    private static final String JAVA_OS_VERSION = "os.version";
    private static final String JAVA_OS_ARCH = "os.arch";
    private static final String JAVA_OS_NAME = "os.name";

    @Override
    public void doPerform(RunData data, Context context) throws Exception {

        final String adminEmail = XDAT.getSiteConfigPreferences().getAdminEmail();

        final UserI user = TurbineUtils.getUser(data);
        final ParameterParser parameters = data.getParameters();
        final String body = emailBody(user, parameters, data, context, false);
        final String htmlBody = emailBody(user, parameters, data, context, true);

        String subject = TurbineUtils.GetSystemName() + " Issue Report from " + user.getLogin();
        // TODO: Need to figure out how to handle attachments in notifications.
        Map<String, File> attachments = getAttachmentMap(data.getSession().getId(), parameters);

        // XDAT.getMailService().sendHtmlMessage(adminEmail, new String[] { adminEmail }, null, null, subject, body, null, attachments);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(MailMessage.PROP_FROM, adminEmail);
        properties.put(MailMessage.PROP_SUBJECT, subject);
        properties.put(MailMessage.PROP_HTML, htmlBody);
        properties.put(MailMessage.PROP_TEXT, body);
        if (attachments != null && attachments.size() > 0) {
            properties.put(MailMessage.PROP_ATTACHMENTS, attachments);
        }
        XDAT.verifyNotificationType(NotificationType.Issue);
        XDAT.getNotificationService().createNotification(NotificationType.Issue.toString(), properties);
    }

    private Map<String, File> getAttachmentMap(String sessionId, ParameterParser parameters) {
        Map<String, File> attachmentMap = new HashMap<String, File>();

        final FileItem fi = parameters.getFileItem("upload");
        if (fi != null) {
            final String cachePath = location(ArcSpecManager.GetInstance().getGlobalCachePath(), "issuereports", sessionId);
            checkFolder(cachePath);

            final File file = new File(location(cachePath, fi.getName()));
            try {
                fi.write(file);
                attachmentMap.put(fi.getName(), file);
            } catch (Exception exception) {
                logger.warn("Could not attach file, " + file.getAbsolutePath(), exception);
            }
        }

        return attachmentMap;
    }

    private String emailBody(UserI user, ParameterParser parameters, RunData data, Context context, boolean html) throws Exception {
        if(html){
            context.put("html", "html");
            context.put("htmlDescription", parameters.get("description").replaceAll("\n", "<br/>"));
        } else {
            context.put("description", parameters.get("description"));
        }
        context.put("summary", parameters.get("summary"));
        context.put("time", (new Date()).toString());
        context.put("user_agent", data.getRequest().getHeader(HTTP_USER_AGENT));
        context.put("xnat_host", data.getRequest().getHeader(HTTP_HOST));
        context.put("remote_addr", AccessLogger.GetRequestIp(data.getRequest()));
        context.put("server_info", data.getServletContext().getServerInfo());
        context.put("os_name", System.getProperty(JAVA_OS_NAME));
        context.put("os_arch", System.getProperty(JAVA_OS_ARCH));
        context.put("os_version", System.getProperty(JAVA_OS_VERSION));
        context.put("java_version", System.getProperty(JAVA_VERSION));
        context.put("java_vendor", System.getProperty(JAVA_VENDOR));
        context.put("xnat_version", FileUtils.getXNATVersion());
        context.put("user", user);
        context.put("postgres_version", PoolDBUtils.ReturnStatisticQuery("SELECT version();", "version", user.getDBName(), user.getLogin()));
        context.put("siteLogoPath", XDAT.getSiteLogoPath());

        if(html){
            context.put("html", "html");
            return AdminUtils.populateVmTemplate(context, "/screens/email/html_issue_report.vm");
        } else {
            return AdminUtils.populateVmTemplate(context, "/screens/email/issue_report.vm");
        }
    }

    private void checkFolder(String path) {
        final File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    private String location(String... pathParts) {
        return StringUtils.join(pathParts, File.separator);
    }
}
