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

import org.nrg.mail.services.MailService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class SystemPathVerification extends AbstractInitializingTask {
    @Override
    public String getTaskName() {
        return "System Path Verification";
    }

    private final static ArrayList<String> pathErrors = new ArrayList<>();
    public static String pathErrorWarning = null;

    @Override
    public void run() {
        try {
            validatePath(_config.getArchivePath(), "Archive", true);
            validatePath(_config.getCachePath(), "Archive", false);
            validatePath(_config.getPipelinePath(), "Archive", false);
            validatePath(_config.getPrearchivePath(), "Archive", false);

            final ProjectExtractor pe = new ProjectExtractor();
            final Map<String, String> projects = _template.query("SELECT id, name FROM xnat_projectdata", pe);
            if(pathErrors.size() > 0) {
                // Send warning email to admin and issue browser notification
                notifyOfPathErrors(projects.size());
            } else {
                _config.setPathErrorWarning("");
            }
            complete();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private boolean validatePath(final String path, final String displayName, final boolean checkForFiles) throws SQLException {
        File filePath = new File(path);
        if (!filePath.exists()) {
            pathErrors.add(displayName+" path \""+path+"\" does not exist.");
            return false;
        } else if (!filePath.isDirectory()){
            pathErrors.add(displayName+" path \""+path+"\" is not a directory.");
            return false;
        } else if (checkForFiles) {
            File[] files = filePath.listFiles();
            final String noFiles = displayName + " files do not exist under \"" + path + "\".";
            if(files == null) {
                pathErrors.add(noFiles);
                return false;
            }
            if(files.length < 1) {
                pathErrors.add(noFiles);
                return false;
            }
        }
        return true;
    }

    private static class ProjectExtractor implements ResultSetExtractor<Map<String, String>> {
        @Override
        public Map<String, String> extractData(final ResultSet results) throws SQLException, DataAccessException {
            final Map<String, String> projects = new HashMap<>();
            while (results.next()) {
                projects.put(results.getString(1), results.getString(2));
            }
            return projects;
        }
    }

    private void notifyOfPathErrors(int numProjects) {
        if(numProjects > 0) {
            int i = 1;
            String adminEmail = _config.getAdminEmail();
            String sysName = _config.getSiteId();
            String emailSubj = sysName + " " + this.getTaskName() + " Failure";
            StringBuffer sb = new StringBuffer();
            String singPlurl = " has";
            if (numProjects > 1) {
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
            _config.setPathErrorWarning(sb.toString().replace("\n","<br>"));
            pathErrorWarning = sb.insert(0, emailSubj+": ").toString();
            logger.error(pathErrorWarning);
            try {
                _mailService.sendHtmlMessage(adminEmail, adminEmail, emailSubj, pathErrorWarning);
            } catch (MessagingException e) {
                logger.error("", e);
            }
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SystemPathVerification.class);

    @Autowired
    @Lazy
    private JdbcTemplate _template;

    @Inject
    private MailService _mailService;

    @Autowired
    @Lazy
    private SiteConfigPreferences _config;
}
