/*
 * org.nrg.xnat.helpers.prearchive.FileSystemSessionTrawler
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

/**
 * Retrieve the session data from the filesystem
 * @author aditya
 *
 */
public final class FileSystemSessionTrawler implements SessionDataProducerI {
	private final Logger logger = LoggerFactory.getLogger(FileSystemSessionTrawler.class);
	private String prearcPath;
	
	public FileSystemSessionTrawler (String prearcPath) {
		this.prearcPath = prearcPath;
	}

	static List<String> hidden = new ArrayList<String>() {{ add(PrearcUtils.TEMP_UNPACK); }};
	
	static FileFilter doesNotContainDot = new FileFilter() {
		public boolean accept(File pathname){
			return pathname.getName().indexOf('.') == -1; 
		}
	};
	static FileFilter hiddenAndDatabaseFileFilter=new FileFilter(){
		public boolean accept(File pathname) {
			return !hidden.contains(pathname.getName()) && doesNotContainDot.accept(pathname) && pathname.isDirectory();
		}
	};

	@Override
	public Collection<SessionData> get() throws IllegalStateException {
		SortedMap<java.util.Date, Collection<PrearcTableBuilder.Session>> sessions;
		ArrayList<SessionData> sds = new ArrayList<SessionData>();
		File[] files = new File(this.prearcPath).listFiles(hiddenAndDatabaseFileFilter);
        List<String> invalidProjects = new ArrayList<String>();
        if (files!=null) {
            for (final File tsdir : files) {
                final String projectId = tsdir.getName();
                if (!PrearcUtils.isTimestampDirectory.accept(tsdir)) {
                    XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectId, null, false);
                    if (project == null) {
                        invalidProjects.add(projectId);
                    }
                }
                if (!invalidProjects.contains(projectId)) {
                    try {
                        sessions = new PrearcTableBuilder().getPrearcSessions(tsdir);
                    } catch (IOException e) {
                        logger.error("Error getting prearchive sessions from the filesystem" , e);
                        throw new IllegalStateException();
                    } catch (SAXException e) {
                        logger.error("Error getting prearchive sessions from the filesystem" , e);
                        throw new IllegalStateException();
                    }
                    for (final Collection<PrearcTableBuilder.Session> ss : sessions.values()) {
                        for (PrearcTableBuilder.Session s : ss) {
                            SessionData _s = s.getSessionData(StringUtils.join(new String[]{PrearcDatabase.projectPath(s.getProject())}));
                            sds.add(_s);
                        }
                    }
                }
            }
        }
        if (invalidProjects.size() > 0) {
            boolean isFirst = true;
            StringBuilder buffer = new StringBuilder();
            for (final String invalidProject : invalidProjects) {
                if (!isFirst) {
                    buffer.append(", ");
                } else {
                    isFirst = false;
                }
                buffer.append(invalidProject);
            }
            final String intro = "During a prearchive rebuild operation, the system found sessions in the prearchive storage (" + prearcPath + ") that are associated with invalid project IDs: ";
            AdminUtils.sendAdminEmail("Invalid projects in the prearchive", "<p>" + intro + "</p><ul><li>" + buffer.toString().replaceAll(", ", "</li><li>") + "</li></ul>");
            logger.error(intro + buffer.toString());
        }
		return sds;
	}
}
