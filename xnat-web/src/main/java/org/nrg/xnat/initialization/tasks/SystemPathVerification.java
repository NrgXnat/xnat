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

import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

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

    @Override
    public void run() {
        try {
            String archivePath = _siteConfigPreferences.getArchivePath();
            validatePath(archivePath, "Archive", true);
            final ProjectExtractor pe = new ProjectExtractor();
            final SubjectExtractor se = new SubjectExtractor();
            final Map<String, String> projects = _template.query("SELECT id, name FROM xnat_projectdata", pe);
            if(projects.size() > 0){
                final Map<String, String> subjects = _template.query("SELECT id, name FROM xnat_subjectdata", se);
                if(subjects.size() > 0){

                }
            }

            if(pathErrors.size() > 0) {

                // Send warning email to admin
System.out.println(pathErrors);

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
            // check for actual subdirectories and files existing here
            /*
            FileFilter directoryFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            };
            */
            File[] files = filePath.listFiles();
            //directoryFilter);
            final String noFiles = displayName + " files do not exist under \"" + path + "\".";
            if(files == null) {
                pathErrors.add(noFiles);
                return false;
            }
            if(files.length < 1) {
                pathErrors.add(noFiles);
                return false;
            }
/*
else {
    for (File file : files) {
        System.out.println(file);
    }
}
*/
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

    private static class SubjectExtractor implements ResultSetExtractor<Map<String, String>> {
        @Override
        public Map<String, String> extractData(final ResultSet results) throws SQLException, DataAccessException {
            final Map<String, String> subjects = new HashMap<>();
            while (results.next()) {
                subjects.put(results.getString(1), results.getString(2));
            }
            return subjects;
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SystemPathVerification.class);

    @Autowired
    @Lazy
    private JdbcTemplate _template;

    @Autowired
    @Lazy
    private SiteConfigPreferences _siteConfigPreferences;
}
