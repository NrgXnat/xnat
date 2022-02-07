/*
 * web: org.nrg.xnat.turbine.modules.actions.ReportIssue
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
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
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.nrg.xnat.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ReportIssue extends SecureAction {
    private static final String HTTP_USER_AGENT = "User-Agent";
    private static final String HTTP_HOST       = "Host";
    private static final String JAVA_VENDOR     = "java.vendor";
    private static final String JAVA_VERSION    = "java.version";
    private static final String JAVA_OS_VERSION = "os.version";
    private static final String JAVA_OS_ARCH    = "os.arch";
    private static final String JAVA_OS_NAME    = "os.name";
    private static final String SUBJECT_FORMAT  = "%s Issue Report from %s";

    @Override
    public void doPerform(final RunData data, final Context context) throws Exception {
        final UserI           user       = XDAT.getUserDetails();
        final ParameterParser parameters = data.getParameters();

        final Map<String, Object> properties = new HashMap<>();

        String from;

        if (XDAT.getNotificationsPreferences().getUserEmailForReportProblem()) {
            from = user.getEmail();
        } else {
            from = XDAT.getSiteConfigPreferences().getAdminEmail();
        }
        String[] to = XDAT.getNotificationsPreferences().getEmailRecipientIssueReports().split(",");
        String subject = getSubject(user);
        String textBody = emailBody(user, parameters, data, context, true);
        String htmlBody = emailBody(user, parameters, data, context, false);

        // TODO: Need to figure out how to handle attachments in notifications.
        final Map<String, File> attachments = getAttachmentMap(data.getSession().getId(), parameters);


        if (!attachments.isEmpty()) {
            XDAT.getMailService().sendHtmlMessage(from, to, null, null, subject, htmlBody, textBody, attachments);
        } else {
            XDAT.getMailService().sendHtmlMessage(from, to, null, null, subject, htmlBody, textBody, null);
        }

        TurbineUtils.setBannerMessage(data, "Thanks for your feedback. The administrator(s) have been notified and will contact you when the issue is resolved or if more information is required.");
    }

    private Map<String, File> getAttachmentMap(final String sessionId, final ParameterParser parameters) {
        final FileItem fileItem = parameters.getFileItem("upload");
        if (fileItem == null || ((!(fileItem instanceof DiskFileItem) || !((DiskFileItem) fileItem).getStoreLocation().exists()) && fileItem.getSize() <= 0)) {
            return Collections.emptyMap();
        }

        final Path cachePath = Paths.get(ArcSpecManager.GetInstance().getGlobalCachePath(), "issuereports", sessionId);
        checkFolder(cachePath);

        final Map<String, File> attachments = new HashMap<>();
        final File              file         = cachePath.resolve(fileItem.getName()).toFile();
        try {
            fileItem.write(file);
            attachments.put(fileItem.getName(), file);
        } catch (Exception exception) {
            log.warn("Could not attach file {}", file.getAbsolutePath(), exception);
        }

        return attachments;
    }

    private String emailBody(final UserI user, final ParameterParser parameters, final RunData data, final Context context, final boolean html) throws Exception {
        if (html) {
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

        if (html) {
            context.put("html", "html");
            return AdminUtils.populateVmTemplate(context, "/screens/email/html_issue_report.vm");
        } else {
            return AdminUtils.populateVmTemplate(context, "/screens/email/issue_report.vm");
        }
    }

    private void checkFolder(final Path path) {
        final File file = path.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private static String getSubject(final UserI user) {
        return String.format(SUBJECT_FORMAT, TurbineUtils.GetSystemName(), user.getUsername());
    }
}
