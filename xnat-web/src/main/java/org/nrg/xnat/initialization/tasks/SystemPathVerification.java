/*
 * org.nrg.xnat.initialization.tasks.SystemPathVerification
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Created:
 * Author: Justin Cleveland (clevelandj@wustl.edu)
 */
package org.nrg.xnat.initialization.tasks;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.nrg.mail.services.MailService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.services.XnatAppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

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

    private final static List<String> pathErrors       = Lists.newArrayList();
    public static String pathErrorWarning = null;

    @Override
    public void run() {
        if (_appInfo.isInitialized()) {
            try {
            	final Integer resourceCount = _template.queryForObject("SELECT COUNT(xnat_abstractresource_id) AS COUNT FROM xnat_abstractresource", Integer.class);
                
            	validatePath(_config.getArchivePath(), "Archive", (resourceCount>0));
                validatePath(_config.getCachePath(), "Cache", false);
                validatePath(_config.getPipelinePath(), "Pipeline", false);
                validatePath(_config.getBuildPath(), "Build", false);
                validatePath(_config.getPrearchivePath(), "Prearchive", false);

                if (pathErrors.size() > 0) {
                    // Send warning email to admin and issue browser notification
                    notifyOfPathErrors(resourceCount);
                } else {
                    _config.setPathErrorWarning("");
                }
                complete();
            } catch (Throwable e) {
                logger.error("An error occurred trying to retrieve the values for the system paths.", e);
            }
        }
    }

    private boolean validatePath(final String path, final String displayName, final boolean checkForFiles) throws SQLException {
        File filePath = new File(path);
        if (!filePath.exists()) {
            pathErrors.add(displayName + " path \"" + path + "\" does not exist.");
            return false;
        } else if (!filePath.isDirectory()) {
            pathErrors.add(displayName + " path \"" + path + "\" is not a directory.");
            return false;
        } else if (checkForFiles) {
            File[] files = filePath.listFiles();
            final String noFiles = displayName + " files do not exist under \"" + path + "\".";
            if (files == null) {
                pathErrors.add(noFiles);
                return false;
            }
            if (files.length < 1) {
                pathErrors.add(noFiles);
                return false;
            }
        }
        return true;
    }

    private void notifyOfPathErrors(int numResources) {
        int i = 1;
        String adminEmail = _config.getAdminEmail();
        String sysName = _config.getSiteId();
        String emailSubj = sysName + " " + this.getTaskName() + " Failure";
        StringBuilder sb = new StringBuilder();
        String singPlurl = " has";
        if (pathErrors.size() > 1) {
            singPlurl = "s have";
        }
        sb.append("The following system path error");
        sb.append(singPlurl);
        sb.append(" been discovered:");
        for (String err : pathErrors) {
            sb.append("\n\t");
            sb.append(i++);
            sb.append(". ");
            sb.append(err);
        }
        _config.setPathErrorWarning(sb.toString().replace("\n", "<br>"));
        pathErrorWarning = sb.insert(0, emailSubj + ": ").toString();
        logger.error(pathErrorWarning);
        
        if (numResources > 0) {
        	//only send an email if the system is supposed to have resources
            try {
                _mailService.sendHtmlMessage(adminEmail, adminEmail, emailSubj, pathErrorWarning);
            } catch (Throwable e) {
                logger.error("", e);
            }
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SystemPathVerification.class);

    private final JdbcTemplate          _template;
    private final MailService           _mailService;
    private final SiteConfigPreferences _config;
    private final XnatAppInfo           _appInfo;
}
