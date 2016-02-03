/*
 * org.nrg.xnat.helpers.prearchive.SessionData
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

import org.nrg.framework.constants.PrearchiveCode;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;

public final class SessionData implements Serializable {
    private static final long serialVersionUID = 7915299491932778685L;

	private Date uploadDate;
	private Date lastBuiltDate;
	private PrearcStatus status;
	private SessionDataTriple sessionTriple = new SessionDataTriple();
	private Date scan_date;
	private String scan_time, subject, url, session, tag, source, visit, protocol, timeZone;
	private PrearchiveCode autoArchive;
    private Boolean preventAnon = false;
    private Boolean preventAutoCommit = false;

	public SessionData() {
	}
	public String getFolderName() {
		return sessionTriple.getFolderName();
	}
	public SessionData setFolderName(String name) {
		this.sessionTriple.setFolderName(name);
		return this;
	}
	public SessionData setFolderName(Object o) {
		this.sessionTriple.setFolderName(o);
		return this;
	}
	public String getName() {
		return session;
	}
	public PrearchiveCode getAutoArchive() {
		return autoArchive;
	}
	public SessionData setName(String name) {
		this.session=name;
		return this;
	}
	public SessionData setName(Object o) {
		if (null != o) {
			this.setName((String)o);
		}
		return this;
	}
	
	public SessionData setAutoArchive(PrearchiveCode code){
		this.autoArchive = code;
		return this;
	}
	
	public SessionData setAutoArchive(Object object) {
		if (null != object) {
            PrearchiveCode code;
            if (object instanceof String) {
                code = PrearchiveCode.valueOf((String) object);
            } else if (object instanceof PrearchiveCode) {
                code = (PrearchiveCode) object;
            } else if (object instanceof Integer) {
                code = PrearchiveCode.code((Integer) object);
            } else {
                throw new ClassCastException("The object submitted for auto-archive must be a String, Integer, or PrearchiveCode; the submitted class is invalid for casting to PrearchiveCode: " + object.getClass());
            }
			this.setAutoArchive(code);
        } else {
			this.autoArchive = null;
		}
		return this;
	}
	
    public Boolean getPreventAnon() {
        return preventAnon;
    }

    public SessionData setPreventAnon(Boolean preventAnon){
        this.preventAnon = preventAnon;
        return this;
    }

    public SessionData setPreventAnon(Object o) {
        if (null != o) {
            this.setPreventAnon((Boolean) o);
        } else {
            this.preventAnon = null;
        }
        return this;
    }

    public Boolean getPreventAutoCommit() {
        return preventAutoCommit;
    }

    public SessionData setPreventAutoCommit(Boolean preventAutoCommit){
        this.preventAutoCommit = preventAutoCommit;
        return this;
    }

    public SessionData setPreventAutoCommit(Object o) {
        if (null != o) {
            this.setPreventAutoCommit((Boolean) o);
        } else {
            this.preventAutoCommit = null;
        }
        return this;
    }

    public String getSource() {
		return source;
	}

	public SessionData setSource(String t) {
		this.source=t;
		return this;
	}

	public SessionData setSource(Object o) {
		if (null != o) {
			this.setSource((String)o);
		}
		return this;
	}
	
	public String getTag() {
		return tag;
	}
	public SessionData setTag(String t) {
		this.tag=t;
		return this;
	}
	public SessionData setTag(Object o) {
		if (null != o) {
			this.setTag((String)o);
		}
		return this;
	}
	
	public String getTimestamp() {
		return sessionTriple.getTimestamp();
	}
	public SessionData setTimestamp(String timestamp) {
		this.sessionTriple.setTimestamp(timestamp);
		return this;
	}
	public SessionData setTimestamp(Object o) {
		this.sessionTriple.setTimestamp(o);
		return this;
	}
	
	public Date getUploadDate() {
		return uploadDate;
	}
	public SessionData setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
		return this;
	}
	
	public SessionData setUploadDate(Object o) {
		if (null != o) {
			java.sql.Timestamp ts = (java.sql.Timestamp)o;
			this.setUploadDate(PrearcUtils.timestamp2Date(ts));
		}
		return this;
	}
	
	public Date getLastBuiltDate() {
		return lastBuiltDate;
	}
	public SessionData setLastBuiltDate(Date lastBuiltDate) {
		this.lastBuiltDate = lastBuiltDate;
		return this;
	}
	public SessionData setLastBuiltDate(Object o) {
		if (null != o) {
			java.sql.Timestamp ts = (java.sql.Timestamp)o;
			this.setLastBuiltDate(PrearcUtils.timestamp2Date(ts));
		}
		return this;
	}
	
	public PrearcStatus getStatus() {
		return status;
	}
	public SessionData setStatus(PrearcStatus status) {
		this.status = status;
		return this;
	}
	public SessionData setStatus(Object o) {
		if (null != o) {
			this.setStatus((PrearcUtils.PrearcStatus)o);
		}
		return this;
	}
	public String getProject() {
		return sessionTriple.getProject();
	}
	public SessionData setProject(String project) {
		if (project != null) {
			this.sessionTriple.setProject(project);
        } else {
			this.sessionTriple.setProject(PrearcUtils.COMMON);
		}
		return this;
	}
	public SessionData setProject(Object o) {
		this.sessionTriple.setProject(o);
		return this;
	}
	
	public Date getScan_date() {
		return scan_date;
	}
	public SessionData setScan_date(Date scan_date) {
		this.scan_date = scan_date;
		return this;
	}
	public SessionData setScan_date(Object o) {
		if (null != o) {
			this.setScan_date((Date)o);
		}
		return this;
	}
	public String getScan_time() {
		return scan_time;
	}
	public SessionData setScan_time(String scan_time) {
		this.scan_time = scan_time;
		return this;
	}
	public SessionData setScan_time(Object o) {
		if (null != o) {
			this.setScan_time((String)o);
		}
		return this;
	}
	public String getSubject() {
		return subject;
	}
	public SessionData setSubject(String subject) {
		this.subject = subject;
		return this;
	}
	public SessionData setSubject(Object o) {
		if (null != o) {
			this.setSubject((String)o);
		}
		return this;
	}
	public String getExternalUrl() {
		return String.format("/prearchive/projects/%s/%s/%s",getProject(),getTimestamp(),getFolderName());
	}
	public String getUrl() {
		return url;
	}
	public SessionData setUrl(String url) {
		this.url = url;
		return this;
	}
	public SessionData setUrl(Object o) {
		if (null != o) {
			this.setUrl((String)o);
		}
		return this;
	}
	public String getVisit() {
		return visit;
	}
	public SessionData setVisit(String visit) {
		this.visit = visit;
		return this;
	}
	public SessionData setVisit(Object o) {
		if (null != o) {
			this.setVisit((String)o);
		}
		return this;
	}
	public String getProtocol() {
		return protocol;
	}
	public SessionData setProtocol(String protocol) {
		this.protocol = protocol;
		return this;
	}
	public SessionData setProtocol(Object o) {
		if (null != o) {
			this.setProtocol((String)o);
		}
		return this;
	}	
	public String getTimeZone() {
		return timeZone;
	}
	public SessionData setTimeZone(String timeZone) {
		this.timeZone = timeZone;
		return this;
	}
	public SessionData setTimeZone(Object o) {
		if (null != o) {
			this.setTimeZone((String)o);
		}
		return this;
	}
	public SessionDataTriple getSessionDataTriple(){
		return sessionTriple;
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
        for (Field f : SessionData.class.getDeclaredFields()) {
            sb.append(f.toString());
			sb.append(":");
			try {
                sb.append(f.get(this));
            } catch (IllegalAccessException e) {
				sb.append("<cannot access>");
			}
			sb.append("\n");
		}
		return sb.toString();		
	}
			}
