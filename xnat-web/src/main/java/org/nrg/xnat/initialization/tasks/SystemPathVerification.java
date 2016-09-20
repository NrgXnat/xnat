/*
 * web: org.nrg.xnat.initialization.tasks.SystemPathVerification
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.mail.services.MailService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.services.XnatAppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SystemPathVerification extends AbstractInitializingTask {
    @Autowired
    public SystemPathVerification(final JdbcTemplate template, final MailService mailService, final SiteConfigPreferences config, final XnatAppInfo appInfo) {
        _template = template;
        _mailService = mailService;
        _config = config;
        _appInfo = appInfo;
    }

    @Override
    public String getTaskName() {
        return "System Path Verification";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (!_appInfo.isInitialized()) {
            throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "The system is not yet initialized. Delaying system path verification until initialization is completed.");
        }

        try {
            final Integer resourceCount = _template.queryForObject("SELECT COUNT(xnat_abstractresource_id) AS COUNT FROM xnat_abstractresource", Integer.class);

            final List<String> errors = new ArrayList<>();
            errors.addAll(validatePath(_config.getArchivePath(), "Archive", (resourceCount > 0)));
            errors.addAll(validatePath(_config.getCachePath(), "Cache", false));
            errors.addAll(validatePath(_config.getPipelinePath(), "Pipeline", false));
            errors.addAll(validatePath(_config.getBuildPath(), "Build", false));
            errors.addAll(validatePath(_config.getPrearchivePath(), "Prearchive", false));

            if (errors.size() > 0) {
                // Send warning email to admin and issue browser notification
                notify(errors, resourceCount);
            } else {
                _config.setPathErrorWarning("");
            }
        } catch (SQLException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred accessing the database while verifying system paths.");
        }
    }

    private List<String> validatePath(final String path, final String displayName, final boolean checkForFiles) throws SQLException {
        final List<String> errors   = new ArrayList<>();
        final File         filePath = new File(path);
        if (!filePath.exists()) {
            errors.add(displayName + " path \"" + path + "\" does not exist.");
        } else if (!filePath.isDirectory()) {
            errors.add(displayName + " path \"" + path + "\" is not a directory.");
        } else if (checkForFiles) {
            final File[] files = filePath.listFiles();
            if (files == null || files.length < 1) {
                errors.add(displayName + " files do not exist under \"" + path + "\".");
            }
        }
        return errors;
    }

    private void notify(final List<String> errors, final int numResources) {
        final StringBuilder buffer     = new StringBuilder();
        buffer.append("The following system path error").append(errors.size() > 1 ? "s have" : " has").append(" been discovered:");

        int index = 1;
        for (final String error : errors) {
            buffer.append("\n\t");
            buffer.append(index++);
            buffer.append(". ");
            buffer.append(error);
        }

        final String        adminEmail = _config.getAdminEmail();
        final String        emailSubj  = _config.getSiteId() + " " + this.getTaskName() + " Failure";
        logger.error(emailSubj + ": " + buffer.toString());

        final String html = buffer.toString().replace("\n", "<br>");
        _config.setPathErrorWarning(html);

        if (numResources > 0) {
            //only send an email if the system is supposed to have resources
            try {
                _mailService.sendHtmlMessage(adminEmail, adminEmail, emailSubj, html);
            } catch (Throwable e) {
                logger.error("", e);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SystemPathVerification.class);

    private final JdbcTemplate          _template;
    private final MailService           _mailService;
    private final SiteConfigPreferences _config;
    private final XnatAppInfo           _appInfo;
}
