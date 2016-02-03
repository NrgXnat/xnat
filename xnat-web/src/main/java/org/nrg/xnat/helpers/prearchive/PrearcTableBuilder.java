/*
 * org.nrg.xnat.helpers.prearchive.PrearcTableBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/19/13 4:17 PM
 */

/**
 * 
 */
package org.nrg.xnat.helpers.prearchive;

import org.apache.commons.lang.StringUtils;
import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author timo
 *
 */
public class PrearcTableBuilder implements PrearcTableBuilderI {
	static Logger logger = LoggerFactory.getLogger(PrearcTableBuilder.class);

	public final static String[] PREARC_HEADERS = {"project".intern(),"last_mod".intern(),"uploaded".intern(),"scan_date".intern(),"scan_time".intern(),"subject".intern(),"session".intern(),"status".intern(),"url".intern(),"visit".intern(),"protocol".intern(),"TIMEZONE".intern(),"SOURCE".intern()};
	
	public static Object[] buildRow(final Session s,final String urlBase){
		Object[] row = new Object[PREARC_HEADERS.length];
		row[0]=s.getProject();
		row[1]=s.getLastBuiltDate();
		row[2]=s.getUploadDate();
		row[3]=s.getDate();
		row[4]=s.getTime();
		row[5]=PrearcTableBuilder.Session.pickSubjectName(s);
		row[6]=PrearcTableBuilder.Session.pickSessionName(s);
		row[7]=s.getStatus();
		row[8]=StringUtils.join(new String[]{urlBase,"/".intern(),s.getTimestamp(),"/".intern(),s.getFolderName()});
		row[9]=s.getVisit();
		row[10]=s.getProtocol();
		row[11]=s.getTimeZone();
		row[12]=s.getSource();
		
		return row;
	}

	
	public static XnatImagesessiondataBean parseSession(final File s) throws IOException, SAXException{
		XDATXMLReader parser = new XDATXMLReader();
		return (XnatImagesessiondataBean) parser.parse(s);
	}

	public class ProjectPrearchive implements ProjectPrearchiveI {
		private Date lastMod;
		private XFTTable content;
		
		public ProjectPrearchive(final Date l, final XFTTable c){
			lastMod=l;
			content=c;
		}

		/* (non-Javadoc)
		 * @see org.nrg.xnat.helpers.prearchive.ProjectPrearchiveI#getLastMod()
		 */
		@Override
		public Date getLastMod() {
			return lastMod;
		}

		/* (non-Javadoc)
		 * @see org.nrg.xnat.helpers.prearchive.ProjectPrearchiveI#getContent()
		 */
		@Override
		public XFTTable getContent() {
			return content;
		}
		
		
	}

	@Override
	public String[] getColumns() {
		return PREARC_HEADERS;
	}
	

	public static String printSession (Session s) {
		ArrayList<String> as = new ArrayList<String>();
		as.add("--Session--");
		as.add("Name : " + s.getFolderName());
		as.add("Status : " + s.getStatus());
		as.add("SubjectId: " + s.getSubjectId());
		as.add("Scan Time : " + s.getTimestamp());
		as.add("Uploaded : " + s.getUploadDate().toString());
		return StringUtils.join(as.toArray(new String[as.size()]), "\n");
	}

	public static class Session implements Comparable<Session> {
		private final File sessionXML;
		
		private XnatImagesessiondataI session=null;
		
		private SessionData data = new SessionData();

		//passed in project should override what is in the session xml, if it exists
		Session(final File sessdir, final String project) {
			data.setFolderName(sessdir.getName());

			sessionXML = new File(sessdir.getPath() + ".xml");
			if (sessionXML.exists()) {
				data.setLastBuiltDate(new Date(sessionXML.lastModified()));
			} else {			
				data.setLastBuiltDate(new Date());
			}

			Date t_uploadDate;
			try {
				t_uploadDate = PrearcUtils.parseTimestampDirectory(sessdir.getParentFile().getName());
			} catch (final ParseException e) {
				logger.error("Unable to parse upload date from session parent " + sessdir.getParentFile(), e);
				t_uploadDate = null;
			}
			data.setUploadDate(t_uploadDate);

			
			data.setStatus(PrearcUtils.checkSessionStatus(sessionXML));

			if(!sessionXML.exists() || sessionXML.length() == 0){
				if(project!=null){
					session=new XnatImagesessiondataBean();
					session.setProject(project);
				}
				if(PrearcStatus.potentiallyReady(data.getStatus()))data.setStatus(PrearcStatus.RECEIVING);
			}else{
				try {
					session=parseSession(sessionXML);
					
					session.setProject(project);
					
					data.setTag(session.getUid());
					
					final String sessionID = session.getId();
					if (null == sessionID || "".equals(sessionID) || "NULL".equals(sessionID)) {
						data.setStatus(PrearcStatus.READY);
					} else {
						data.setStatus(PrearcStatus.ARCHIVING);
					}
				} catch (Exception e) {
					if(PrearcStatus.potentiallyReady(data.getStatus())){
						// The following accounts for the case where a project was passed in but the session XML is unparseable for some reason (eg. it is empty).
						// In that case use the project name passed in.
						if (project != null && (data.getProject() == null || !data.getProject().equals(project))) {
							data.setProject(project);
						}
						data.setStatus(PrearcStatus.ERROR);

		            	PrearcUtils.log(sessdir, e);
					}
				}
			}
		}
		
		public SessionData getSessionData (String urlBase) {
			//populate the rest of the session data object.
			data.setProject(this.getProject());
			data.setScan_date(this.getDate());
			data.setScan_time(this.getTime());
			data.setSubject(PrearcTableBuilder.Session.pickSubjectName(this));
			data.setName(PrearcTableBuilder.Session.pickSessionName(this));
			data.setFolderName(this.getFolderName());
			data.setTag(this.getTag());
			data.setAutoArchive(this.getPrearchiveCode());
			data.setUrl(PrearcUtils.makeUri(urlBase, data.getTimestamp(), data.getFolderName()));
			data.setVisit(this.getVisit());
			data.setProtocol(this.getProtocol());
			data.setTimeZone(this.getTimeZone());
			data.setSource(this.getSource());
			return this.data;
		}
		
		public static String pickSubjectName(final PrearcTableBuilder.Session s) {
			String ret = "";
			if (StringUtils.isNotEmpty(s.getSubjectId())) {
				ret = s.getSubjectId();
			}
			if (StringUtils.isEmpty(ret) && StringUtils.isNotEmpty(s.getPatientName())) {
				ret = s.getPatientName();
			}
			
			return ret;
		}
		
		public static String pickSessionName(final PrearcTableBuilder.Session s) {
			String ret = "";
			if (StringUtils.isNotEmpty(s.getLabel())) {
				ret = s.getLabel();
			}
			if (StringUtils.isEmpty(ret) && StringUtils.isNotEmpty(s.getPatientId())) {
				ret = s.getPatientId();
			}
			
			if (StringUtils.isEmpty(ret)){
				return s.getFolderName();
			}
			return ret;
		}

		public void setFolderName(String name) {
			this.data.setFolderName(name);
		}
		
		public String getFolderName() {
			return data.getFolderName();
		}

		public void setTag(String name) {
			this.data.setTag(name);
		}
		
		public String getTag() {
			return data.getTag();
		}

		public void setSessionName(String name) {
			this.data.setName(name);
		}
		
		public String getSessionName() {
			return data.getName();
		}
		
		public Date getLastBuiltDate() {
			return data.getLastBuiltDate();
		}
		public void setLastBuiltDate(Date lastBuiltDate) {
			data.setLastBuiltDate(lastBuiltDate);
		}

		public Date getUploadDate() {
			return data.getUploadDate();
		}

		public String getTimestamp() {
			return data.getTimestamp();
		}

		public void setTimestamp(String timestamp) {
			this.data.setTimestamp(timestamp);
		}

		public String getProject(){
			// Get the project specified in the session.xml.
			// If the session.xml file couldn't be parsed return the project field in the the local SessionData object
			// which holds the optional project name passed to the constructor in case of an unparseable session.xml.
			if (session != null) {
				return session.getProject();
			}
			else {
				if (data != null && data.getProject() != null) {
					return data.getProject();
				}
				else {
					return null;
				}
			}
		}

		public Object getDate(){
			return (session!=null)?session.getDate():null;
		}

		public Object getTime(){
			return (session!=null)?session.getTime():null;
		}

		public String getSubjectId(){
			return (session!=null)?session.getSubjectId():null;
		}

		public String getLabel(){
			return (session!=null)?session.getLabel():null;
			
		}
		public String getVisit(){
			return (session!=null)?session.getVisit():null;
			
		}
		public String getProtocol(){
			return (session!=null)?session.getProtocol():null;
			
		}
		public String getTimeZone(){
			//no need to keep timezone in the image session.
			//return (session!=null)?session.getTimeZone():null;
			return null;
		}
		public String getSource(){
			return null;
			
		}
		public String getPatientId() {
			return (session!=null)?session.getDcmpatientid():null;
		}
		
		public String getPatientName() {
			return (session!=null)?session.getDcmpatientname():null;
		}
		
		public PrearcStatus getStatus(){
			return data.getStatus();
		}
		
		public File getSessionXML(){
			return this.sessionXML;
		}
		
		public PrearchiveCode getPrearchiveCode() {
            final String project = this.getProject();
            if (project == null || project.equals(PrearcUtils.COMMON)) {
                logger.info("Found null or unassigned project, returning prearchive code of Manual");
                return PrearchiveCode.Manual;  // Unassigned projects will not have a known prearchive code
            }
            final Integer prearchiveCode = ArcSpecManager.GetInstance().getPrearchiveCodeForProject(project);
            if (prearchiveCode == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Found a prearchive entry " + this.getFolderName() + " with a project that didn't return an archive code: " + project);
                }
                return PrearchiveCode.Manual;
            }
            return PrearchiveCode.code(prearchiveCode);
        }
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(final Session other) {
			return getLastBuiltDate().compareTo(other.getLastBuiltDate());
		}
	}

	public SortedMap<Date, Collection<Session>> getPrearcSessions(final File prearcDir) throws IOException, SAXException {
		final SortedMap<Date, Collection<Session>> sessions = new TreeMap<Date, Collection<Session>>();
		if(PrearcUtils.isTimestampDirectory.accept(prearcDir)){
			for (final File sessdir : prearcDir.listFiles(PrearcUtils.isDirectory)) {
				final Session session = buildSessionObject(sessdir,prearcDir.getName(),null);
				
				final Date builtDate = session.getLastBuiltDate();
				
				if (!sessions.containsKey(builtDate)) {
					sessions.put(builtDate, new ArrayList<Session>(1));
				}
				sessions.get(builtDate).add(session);
			}
		}else{		
		for (final File tsdir : prearcDir.listFiles(PrearcUtils.isTimestampDirectory)) {
			for (final File sessdir : tsdir.listFiles(PrearcUtils.isDirectory)) {
				final Session session = buildSessionObject(sessdir,tsdir.getName(),prearcDir.getName());

				final Date builtDate = session.getLastBuiltDate();
				if (!sessions.containsKey(builtDate)) {
					sessions.put(builtDate, new ArrayList<Session>(1));
				}
				sessions.get(builtDate).add(session);
			}
		}
		}
		return sessions;
	}
	
	public static Session buildSessionObject(final File sessdir,final String timestamp, final String project){
		final Session session = new Session(sessdir,project);		
		session.setTimestamp(timestamp);
		return session;
	}
}
