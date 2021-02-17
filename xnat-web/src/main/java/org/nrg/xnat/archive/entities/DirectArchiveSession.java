package org.nrg.xnat.archive.entities;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;

import javax.persistence.*;
import java.util.Date;

@Slf4j
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"project", "name", "tag"}))
public class DirectArchiveSession extends AbstractHibernateEntity {
    private String project, subject, name, folderName, tag, visit, protocol, timeZone, location, source, timestampUid, message;
    private Date uploadDate;
    private Date lastBuiltDate;
    private PrearcUtils.PrearcStatus status;
    private Date scanDate;
    private String scanTime;

    public DirectArchiveSession(String project, String subject, String name, String timestampUid, String folderName,
                                String tag, String visit, String protocol, String timeZone, String location,
                                String source, Date uploadDate, Date lastBuiltDate, PrearcUtils.PrearcStatus status,
                                Date scanDate, String scanTime) {
        this.project = project;
        this.subject = subject;
        this.name = name;
        this.timestampUid = timestampUid;
        this.folderName = folderName;
        this.tag = tag;
        this.visit = visit;
        this.protocol = protocol;
        this.timeZone = timeZone;
        this.location = location;
        this.source = source;
        this.uploadDate = uploadDate;
        this.lastBuiltDate = lastBuiltDate;
        this.status = status;
        this.scanDate = scanDate;
        this.scanTime = scanTime;
    }

    public DirectArchiveSession() {}

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String session) {
        this.name = session;
    }

    public String getTimestampUid() {
        return timestampUid;
    }

    public void setTimestampUid(String timestampUid) {
        this.timestampUid = timestampUid;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getVisit() {
        return visit;
    }

    public void setVisit(String visit) {
        this.visit = visit;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Date getLastBuiltDate() {
        return lastBuiltDate;
    }

    public void setLastBuiltDate(Date lastBuiltDate) {
        this.lastBuiltDate = lastBuiltDate;
    }

    @Enumerated(EnumType.STRING)
    public PrearcUtils.PrearcStatus getStatus() {
        return status;
    }

    public void setStatus(PrearcUtils.PrearcStatus status) {
        this.status = status;
    }

    public Date getScanDate() {
        return scanDate;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SessionData toSessionData() {
        SessionData s = new SessionData();
        s.setProject(project);
        s.setSubject(subject);
        s.setName(name);
        s.setFolderName(folderName);
        s.setTimestamp(timestampUid);
        s.setUploadDate(uploadDate);
        s.setLastBuiltDate(lastBuiltDate);
        s.setStatus(status);
        s.setScan_date(scanDate);
        s.setScan_time(scanTime);
        s.setUrl(location);
        s.setTag(tag);
        s.setSource(source);
        s.setVisit(visit);
        s.setProtocol(protocol);
        s.setPreventAnon(false);
        s.setPreventAutoCommit(false);
        s.setId(getId());
        return s;
    }
}
